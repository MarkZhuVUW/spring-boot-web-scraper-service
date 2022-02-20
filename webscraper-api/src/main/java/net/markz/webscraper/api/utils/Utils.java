package net.markz.webscraper.api.utils;

import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.exceptions.ExceptionHandler;
import net.markz.webscraper.api.services.SearchUrl;
import net.markz.webscraper.model.OnlineShopDto;
import org.openqa.selenium.NoSuchElementException;
import org.springframework.http.ResponseEntity;

@Slf4j
public class Utils {

    public static <T> T translateWebElementException(ExceptionHandler<T> func) {
        try {
            return func.handle();
        } catch (NoSuchElementException e) { // We don't screw up our app just because an element cannot be found.
            log.error("NoSuchElementException thrown with message: {} and \n stack trace: {} \n exception: {}",
                    e.getMessage(), e.getStackTrace(), e);
            return null; // Indicate no element found.
        }
    }
    public static SearchUrl getSearchUrl(final OnlineShopDto onlineShopDto) {
        return switch (onlineShopDto) {
            case COUNTDOWN -> SearchUrl.COUNTDOWN;
            case PET_CO -> SearchUrl.PET_CO;
            case THE_WAREHOUSE -> SearchUrl.THE_WAREHOUSE;
            case GOOGLE_SHOPPING -> SearchUrl.GOOGLE_SHOPPING;
            default -> throw new UnsupportedOperationException();
        };
    }

    public static <T> ResponseEntity<T> translateException(ExceptionHandler<T> func) {
        try {
            return ResponseEntity.ok(func.handle());
        } catch (RuntimeException e) { // log all runtime exceptions thrown and re-throw it.
            log.error("RuntimeException thrown with message: {} \n stack trace: {} \n exception: {}",
                    e.getMessage(), e.getStackTrace(), e);
            throw e;
        }

    }
}

