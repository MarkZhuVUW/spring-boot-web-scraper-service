package net.markz.webscraper.api.parsers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.utils.Utils;
import net.markz.webscraper.model.OnlineShopDto;
import net.markz.webscraper.model.OnlineShoppingItemDTO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class CountdownParser implements ISeleniumParser {



    private String parseId(String str) {
        return str.split("-")[1];
    }

    @Override
    public List<OnlineShoppingItemDTO> parse(final WebDriver webDriver) {
        return Utils.translateWebElementException(
                () -> {
                    log.info("parsing countdown response.");
                    var root =  webDriver.findElement(By.tagName("product-grid"));
                    if (root == null) {
                        return new ArrayList<>(); // swallow WebDriver exceptions and assume no result found.
                    }
                    var items =
                            Utils.translateWebElementException(() -> root.findElements(By.tagName("cdx-card")));

                    if (items == null) {
                        return new ArrayList<>(); // swallow WebDriver exceptions and assume no result found.
                    }
                    return items.stream()
                            .filter(item -> Utils.translateWebElementException(() -> item.findElement(By.tagName("h2"))) != null)
                            .map(
                                    item ->
                                            new OnlineShoppingItemDTO()
                                                    .onlineShop(OnlineShopDto.COUNTDOWN)
                                                    .onlineShopName(OnlineShopDto.COUNTDOWN.name())
                                                    .name(item.findElement(By.tagName("h2")).getText())
                                                    .salePrice(item.findElement(By.tagName("h3")).getText())
                                                    .uuid(parseId(item.findElement(By.tagName("h3")).getAttribute("id")))
                                                    .imageUrl(item.findElement(By.tagName("img")).getAttribute("src"))
                                                    .href(item.findElement(By.tagName("a")).getAttribute("href"))
                                                    // TODO: Implement database
                                                    .isSaved(false)

                            )
                            .toList();
                });
    }
}
