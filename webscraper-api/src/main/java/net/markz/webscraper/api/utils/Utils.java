package net.markz.webscraper.api.utils;

import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.exceptions.ExceptionHandler;
import net.markz.webscraper.api.services.SearchUrl;
import net.markz.webscraper.model.OnlineShopDto;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.openqa.selenium.NoSuchElementException;
import org.springframework.http.ResponseEntity;




@Slf4j
public record Utils() {

    public static <T> T translateWebElementException(ExceptionHandler<T> func) {
        try {
            return func.handle();
        } catch (NoSuchElementException e) { // swallow Element not found exception.
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

    public static String reflectionToString(Object obj) {
        if(obj == null) {
            return "null";
        }
        return ToStringBuilder.reflectionToString(obj, new RecursiveToStringStyle());

    }
}

