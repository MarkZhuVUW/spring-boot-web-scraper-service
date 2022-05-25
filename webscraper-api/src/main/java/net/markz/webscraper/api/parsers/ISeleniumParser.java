package net.markz.webscraper.api.parsers;

import net.markz.webscraper.model.OnlineShoppingItemDto;
import org.openqa.selenium.WebDriver;

import java.util.List;

public interface ISeleniumParser {
    List<OnlineShoppingItemDto> parse(WebDriver webDriver);
}
