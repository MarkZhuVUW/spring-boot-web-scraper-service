package net.markz.webscraper.api.utils;

import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.exceptions.Lazy;
import net.markz.webscraper.api.services.SearchUrl;
import net.markz.webscraper.model.OnlineShopDto;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.openqa.selenium.WebDriverException;


@Slf4j
public final record Utils() {


    public static <T> T translateWebElementException(Lazy<T> callback) {
        try {
            return callback.lazyDo();
        } catch (WebDriverException e) { // swallow all web driver exceptions including ElementNotFoundException.
            log.error("WebdriverException thrown: {}. Swallowing it.", e.toString());
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

    public static String reflectionToString(Object obj) {
        if(obj == null) {
            return "null";
        }
        return ToStringBuilder.reflectionToString(obj, new RecursiveToStringStyle());

    }

}

