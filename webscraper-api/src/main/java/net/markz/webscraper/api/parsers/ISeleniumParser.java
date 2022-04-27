package net.markz.webscraper.api.parsers;

import net.markz.webscraper.model.OnlineShoppingItemDTO;
import org.openqa.selenium.WebDriver;

import java.util.List;

public interface ISeleniumParser {
    List<OnlineShoppingItemDTO> parse(WebDriver webDriver);
}
