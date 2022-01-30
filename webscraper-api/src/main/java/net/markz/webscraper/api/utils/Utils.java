package net.markz.webscraper.api.utils;

import net.markz.webscraper.api.services.SearchUrl;
import net.markz.webscraper.model.OnlineShopDto;

public class Utils {
    public Utils() {

    }
    public static String getSearchUrl(final OnlineShopDto onlineShopDto, final String searchString) {
        final String searchUrl;
        switch (onlineShopDto) {
            case COUNTDOWN -> searchUrl = SearchUrl.COUNTDOWN.getSearchUrlWithPathParams(searchString);
            default -> throw new IllegalArgumentException("Invalid shop name.");
        }
        return searchUrl;
    }

}
