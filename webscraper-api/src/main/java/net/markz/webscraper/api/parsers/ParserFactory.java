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
    private final CountdownParser countdownParser;
    private final PetCoParser petCoParser;
    private final TheWarehouseParser theWarehouseParser;

    public IHttpParser getHttpParser(final OnlineShopDto onlineShopDto) {
        return switch (onlineShopDto) {
            case COUNTDOWN -> countdownParser;
            default -> throw new UnsupportedOperationException(ERROR_MSG + onlineShopDto);
        };
    }

    public ISeleniumParser getSeleniumParser(final OnlineShopDto onlineShopDto) {
        return switch (onlineShopDto) {
            case PET_CO -> petCoParser;
            case THE_WAREHOUSE -> theWarehouseParser;
            case GOOGLE_SHOPPING -> googleShoppingParser;

            default -> throw new UnsupportedOperationException(ERROR_MSG + onlineShopDto);
        };
    }

}
