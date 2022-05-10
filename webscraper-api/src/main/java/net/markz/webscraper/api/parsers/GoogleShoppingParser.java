package net.markz.webscraper.api.parsers;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.utils.Utils;
import net.markz.webscraper.model.OnlineShopDto;
import net.markz.webscraper.model.OnlineShoppingItemDTO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
@Slf4j
public class GoogleShoppingParser implements ISeleniumParser {

    private Map<String, String> parseInnerText(final String str) {
        final String[] splitStr = str.split("\\n");

        Map<String, String> map = new HashMap<>();

        map.put("name", splitStr[0]);
        map.put("price", Arrays.stream(splitStr)
                .filter(s -> s.contains("$"))
                .reduce("", (s1, s2) -> String.format("%s %s", s1, s2)));
        map.put("onlineShopName", splitStr[splitStr.length - 3]);

        return map;
    }

    @Override
    public List<OnlineShoppingItemDTO> parse(@NonNull WebDriver webDriver) {
        return Utils.translateWebElementException(() ->
        {
            var root =
                    webDriver.findElement(By.className("sh-pr__product-results-grid"));
            if(root == null) {
                log.debug("Looking for class name: {}, actual DOM: {}", "sh-pr__product-results-grid", webDriver.getPageSource());
                return null; // swallow WebDriver exceptions and assume no result found.
            }
            var items =  root.findElements(By.className("sh-dgr__content"));

            if(items == null) {
                log.debug("Looking for class name: {}, actual DOM: {}", "sh-dgr__content", webDriver.getPageSource());
                return new ArrayList<>(); // swallow WebDriver exceptions and assume no result found.
            }

            return items
                    .stream()
                    // Filter out dirty data structures
                    .map(item -> {
                        final var innerTextMap = parseInnerText(item.getAttribute("innerText"));
                        final var imgElement = item.findElement(By.tagName("img"));

                        return new OnlineShoppingItemDTO()
                                .onlineShop(OnlineShopDto.GOOGLE_SHOPPING)
                                .onlineShopName(innerTextMap.get("onlineShopName"))
                                .name(innerTextMap.get("name"))
                                .salePrice(innerTextMap.get("price"))
                                .uuid(imgElement.getAttribute("id"))
                                .imageUrl(imgElement.getAttribute("src"))
                                .href(item.findElement(By.tagName("a")).getAttribute("href"))

                                // TODO: Implement database
                                .isSaved(false);
                    })
                    .toList();
        });
    }
}
