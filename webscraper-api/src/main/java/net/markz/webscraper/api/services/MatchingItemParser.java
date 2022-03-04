package net.markz.webscraper.api.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.exceptions.WebscraperException;
import net.markz.webscraper.api.utils.Utils;
import net.markz.webscraper.model.OnlineShopDto;
import net.markz.webscraper.model.OnlineShoppingItemDTO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.http.HttpStatus;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Look for the matching shopping item in dom/http response etc by some sort of unique identifier
 * and parse it to a {OnlineShoppingItem}
 */
@Slf4j
public record MatchingItemParser() {
    private static final String ERROR_MSG = "Parsing this website is not supported yet: ";
    private static final String ERROR_PRICE_MSG = "Parsing the price of this website is not supported yet: ";
    private static final String ERROR_INNERTEXT_MSG = "Parsing the innertext of this website is not supported yet: ";

    public static String parsePrice(final OnlineShopDto onlineShopDto, final String price) {
        return switch (onlineShopDto) {
            case COUNTDOWN -> price
                    .replace("\\n", "")
                    .replace("\\n", "");
            case PET_CO -> price.replace("Save up to $", "").replace("From $", "").split("\\n")[1]; // Get sale price.
            case THE_WAREHOUSE -> price.replace("||", ". ");
            default -> throw new UnsupportedOperationException(ERROR_PRICE_MSG + onlineShopDto);
        };
    }

    public static Map<String, String> parseInnerText(final OnlineShopDto onlineShopDto, final String str) {
        return switch (onlineShopDto) {
            case GOOGLE_SHOPPING -> {
                final String[] splitStr = str.split("\\n");

                Map<String, String> map = new HashMap<>();

                map.put("name", splitStr[0]);
                map.put("price", Arrays.stream(splitStr)
                        .filter(s -> s.contains("$"))
                        .reduce("", (s1, s2) -> String.format("%s+%s", s1, s2)));
                map.put("onlineShopName", splitStr[splitStr.length - 3]);
                yield map;
            }
            default -> throw new IllegalStateException(ERROR_INNERTEXT_MSG + onlineShopDto);
        };

    }

    public static List<OnlineShoppingItemDTO> parse(@NonNull final OnlineShopDto onlineShopDto, final HttpResponse<String> resp, final WebDriver webDriver) {
        return switch(onlineShopDto) {
            case PET_CO -> parsePetCoResp(webDriver);
            case COUNTDOWN -> parseCountdownResp(resp);
            case THE_WAREHOUSE -> parseTheWarehouse(webDriver);
            case GOOGLE_SHOPPING -> parseGoogleShopping(webDriver);
        };

    }

    public static List<OnlineShoppingItemDTO> parseCountdownResp(@NonNull final HttpResponse<String> resp) {

        try {
            final var onlineShoppingItemDTOS = new ArrayList<OnlineShoppingItemDTO>();

            @SuppressWarnings("unchecked") final var body = (Map<String, Map<String, Object>>)
                    (new ObjectMapper().readValue(resp.body(), Map.class).get("products"));
            @SuppressWarnings("unchecked") final var items = (List<Map<String, Object>>) body.get("items");

            items.forEach(
                    item -> {
                        @SuppressWarnings("unchecked") Map<String, Object> images = (Map<String, Object>) item.get("images");
                        @SuppressWarnings("unchecked") Map<String, Object> price = (Map<String, Object>) item.get("price");


                        if((Double)price.get("savePrice") == 0) {
                            return; // We care only about products on sale.
                        }

                        onlineShoppingItemDTOS.add(
                                new OnlineShoppingItemDTO()
                                        .onlineShop(OnlineShopDto.COUNTDOWN)
                                        .onlineShopName(OnlineShopDto.COUNTDOWN.name())
                                        .imageUrl((String) images.get("big"))
                                        .name((String) item.get("name"))
                                        .salePrice(Double.toString((Double)price.get("salePrice")))
                                        .uuid((String) item.get("barcode"))
                                        // TODO: Implement database
                                        .isSaved(false));
                    });
            return onlineShoppingItemDTOS;
        } catch (final JsonProcessingException e) {

            log.info("Failed to parse json: {}", resp.body());
            throw new WebscraperException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

    }

    public static List<OnlineShoppingItemDTO> parsePetCoResp(@NonNull final WebDriver webDriver) {

        var items = Utils.translateWebElementException(() ->
                webDriver.findElement(By.className("products-with-divider")).findElements(By.className("product")));

        if(items == null) {
            log.debug("Looking for class name: {}, actual DOM: {}", "products-with-divider", webDriver.getPageSource());
            return new ArrayList<>(); // swallow WebDriver exceptions and assume no result found.
        }

        return items
                .stream()
                .filter(item -> item.findElement(By.className("price")).getAttribute("innerText").startsWith("Save up to "))
                .map(item -> new OnlineShoppingItemDTO()
                        .onlineShop(OnlineShopDto.PET_CO)
                        .onlineShopName(OnlineShopDto.PET_CO.name())
                        .name(item.findElement(By.className("desc")).getAttribute("innerText"))
                        .salePrice(parsePrice(OnlineShopDto.PET_CO, item.findElement(By.className("price")).getAttribute("innerText")))
                        .uuid(UUID.randomUUID().toString())
                        .imageUrl(item.findElement(By.tagName("img")).getAttribute("src"))
                        // TODO: Implement database
                        .isSaved(false)
                )
                .toList();
    }

    public static List<OnlineShoppingItemDTO> parseGoogleShopping(@NonNull final WebDriver webDriver) {


        var root = Utils.translateWebElementException(() ->
                webDriver.findElement(By.className("sh-pr__product-results-grid")));
        if(root == null) {
            log.debug("Looking for class name: {}, actual DOM: {}", "sh-pr__product-results-grid", webDriver.getPageSource());
            return new ArrayList<>(); // swallow WebDriver exceptions and assume no result found.
        }
        var items =  Utils.translateWebElementException(() -> root.findElements(By.className("sh-dgr__content")));

        if(items == null) {
            log.debug("Looking for class name: {}, actual DOM: {}", "sh-dgr__content", webDriver.getPageSource());
            return new ArrayList<>(); // swallow WebDriver exceptions and assume no result found.
        }

        return items
                .stream()
                // Filter out dirty data structures
                .filter(item -> parseInnerText(OnlineShopDto.GOOGLE_SHOPPING, item.getAttribute("innerText")) != null)
                .map(item -> {
                    final var innerTextMap = parseInnerText(OnlineShopDto.GOOGLE_SHOPPING, item.getAttribute("innerText"));
                    final var imgElement = item.findElement(By.tagName("img"));

                    return new OnlineShoppingItemDTO()
                            .onlineShop(OnlineShopDto.GOOGLE_SHOPPING)
                            .onlineShopName(innerTextMap.get("onlineShopName"))
                            .name(innerTextMap.get("name"))
                            .salePrice(innerTextMap.get("price"))
                            .uuid(imgElement.getAttribute("id"))
                            .imageUrl(imgElement.getAttribute("src"))
                            // TODO: Implement database
                            .isSaved(false);
                })
                .toList();
    }



    public static List<OnlineShoppingItemDTO> parseTheWarehouse(@NonNull final WebDriver webDriver) {


        var root = Utils.translateWebElementException(() ->
                webDriver.findElement(By.className("product-grid-wrapper")));
        if(root == null) {
            return new ArrayList<>(); // swallow WebDriver exceptions and assume no result found.
        }
        var items =  Utils.translateWebElementException(() -> root.findElements(By.className("product-tile")));

        if(items == null) {
            return new ArrayList<>(); // swallow WebDriver exceptions and assume no result found.
        }
        return items
                .stream()
                .map(item -> {
                            String dataGtmProduct = item.getAttribute("data-gtm-product");

                            Map<String, String> body;

                            try {
                                //noinspection unchecked
                                body = (new ObjectMapper().readValue(dataGtmProduct, Map.class));
                                final double thenPrice = Double.parseDouble(body.get("productThenPrice"));
                                final double salePrice = Double.parseDouble(body.get("price"));
                                final WebElement specialImage = Utils.translateWebElementException(() ->
                                        item.findElement(By.cssSelector("img[class*='product-badge-image top left']")));
                                if(thenPrice >= salePrice && specialImage == null) {
                                    return null;
                                } // Omit non sales items.
                            } catch (JsonProcessingException e) {
                                log.info("Failed to parse json: {}", dataGtmProduct);
                                throw new WebscraperException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
                            }
                            return new OnlineShoppingItemDTO()
                                    .onlineShop(OnlineShopDto.THE_WAREHOUSE)
                                    .onlineShopName(OnlineShopDto.THE_WAREHOUSE.name())
                                    .name(body.get("name"))
                                    .salePrice(parsePrice(OnlineShopDto.THE_WAREHOUSE, body.get("price") + "||" + body.get("promotionCallOutMessage")))
                                    .uuid(body.get("id"))
                                    .imageUrl(item.findElement(By.className("tile-image")).getAttribute("src"))
                                    // TODO: Implement database
                                    .isSaved(false);
                        }
                )
                .filter(Objects::nonNull)
                .toList();
    }
}
