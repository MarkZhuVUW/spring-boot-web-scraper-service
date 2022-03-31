package net.markz.webscraper.api.parsers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.exceptions.WebscraperException;
import net.markz.webscraper.api.utils.Utils;
import net.markz.webscraper.model.OnlineShopDto;
import net.markz.webscraper.model.OnlineShoppingItemDTO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@AllArgsConstructor
@Slf4j
public class TheWarehouseParser implements ISeleniumParser {

    private String parsePrice(final String priceStr) {
        return priceStr.replace("||", ". ");
    }
    @Override
    public List<OnlineShoppingItemDTO> parse(final WebDriver webDriver) {
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
                                    .salePrice(parsePrice(body.get("price") + "||" + body.get("promotionCallOutMessage")))
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
