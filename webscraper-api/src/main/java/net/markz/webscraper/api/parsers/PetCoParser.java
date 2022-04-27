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
import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class PetCoParser implements ISeleniumParser {

    private String parsePrice(final String priceStr) {
        return priceStr
                .replace("Save up to $", "")
                .replace("From $", "")
                .split("\\n")[1];
    }
    @Override
    public List<OnlineShoppingItemDTO> parse(@NonNull WebDriver webDriver) {
        return Utils.translateWebElementException(
                () -> {
                    var items =
                            webDriver.findElement(By.className("products-with-divider")).findElements(By.className("product"));

                    if(items == null) {
                        log.debug("Looking for class name: {}, actual DOM: {}", "products-with-divider",  webDriver.getPageSource());
                        return new ArrayList<>(); // swallow WebDriver exceptions and assume no result found.
                    }

                    return items
                            .stream()
                            .filter(item -> item.findElement(By.className("price")).getAttribute("innerText").startsWith("Save up to "))
                            .map(item -> {
                                        final var name = item.findElement(By.className("desc")).getAttribute("innerText");
                                        return new OnlineShoppingItemDTO()
                                                .onlineShop(OnlineShopDto.PET_CO)
                                                .onlineShopName(OnlineShopDto.PET_CO.name())
                                                .name(name)
                                                .salePrice(parsePrice(item.findElement(By.className("price")).getAttribute("innerText")))
                                                .uuid(name)
                                                .imageUrl(item.findElement(By.tagName("a")).findElement(By.tagName("img")).getAttribute("src"))
                                                .href(item.findElement(By.tagName("a")).getAttribute("href"))
                                                // TODO: Implement database
                                                .isSaved(false);
                                    }
                            )
                            .toList();
                });
    }
}
