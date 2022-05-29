package net.markz.webscraper.api.parsers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.utils.Utils;
import net.markz.webscraper.model.OnlineShopDto;
import net.markz.webscraper.model.OnlineShoppingItemDto;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@AllArgsConstructor
@Slf4j
public class CountdownParser implements ISeleniumParser {



    private String parseId(String str) {
        return str.split("-")[1];
    }

    @Override
    public List<OnlineShoppingItemDto> parse(final WebDriver webDriver) {
        return Utils.translateWebElementException(
                () -> {
                    var root =  Utils.translateWebElementException(
                            () -> webDriver.findElement(By.tagName("product-grid"))
                    );
                    if (root == null) {
                        log.info("Cannot find tag name: product-grid");
                        return new ArrayList<>(); // swallow WebDriver exceptions and assume no result found.
                    }
                    var items = Utils.translateWebElementException(
                            () -> root.findElements(By.tagName("cdx-card"))
                    );

                    if (items == null) {
                        log.info("Cannot find tag name: cdx-card");
                        return null; // swallow WebDriver exceptions and assume no result found.
                    }

                    return items.stream()
                            // Filter out dirty data.
                            .filter(item ->
                                    Utils.translateWebElementException(() -> item.findElement(By.tagName("h2"))) != null)
                            .limit(11) // Limit to only 10 items for now. We don't want to get into pagination yet.
                            .map(
                                    item ->
                                    {
                                        final var nameElement = Utils.translateWebElementException(
                                                () -> item.findElement(By.tagName("h2"))
                                        );
                                        if(nameElement == null) {
                                            return null;
                                        }
                                        final var priceElement = Utils.translateWebElementException(
                                                () -> item.findElement(By.tagName("h3"))
                                        );

                                        return new OnlineShoppingItemDto()
                                                .onlineShop(OnlineShopDto.COUNTDOWN)
                                                .onlineShopName(OnlineShopDto.COUNTDOWN.name())
                                                .name(nameElement.getText())
                                                .salePrice(priceElement.getText())
                                                .uuid(parseId(priceElement.getAttribute("id")))
                                                .imageUrl(item.findElement(By.tagName("img")).getAttribute("src"))
                                                .href(item.findElement(By.tagName("a")).getAttribute("href"))
                                                .userId("markz") // hardcoded until I implement api gateway + cognito.
                                                // TODO: Implement database
                                                .isSaved(false);
                                    }

                            )
                            .filter(Objects::nonNull)
                            .toList();
                });
    }
}
