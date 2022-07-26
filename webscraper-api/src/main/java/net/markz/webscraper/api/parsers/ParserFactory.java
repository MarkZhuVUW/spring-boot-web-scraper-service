package net.markz.webscraper.api.parsers;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.model.OnlineShopDto;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class ParserFactory {
    private static final String ERROR_MSG = "Parsing this website is not supported yet: ";
    private final GoogleShoppingParser googleShoppingParser;


    public ISeleniumParser getSeleniumParser(final OnlineShopDto onlineShopDto) {
        return switch (onlineShopDto) {
            case GOOGLE_SHOPPING -> googleShoppingParser;
        };
    }

}
