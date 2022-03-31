package net.markz.webscraper.api.parsers;

import net.markz.webscraper.model.OnlineShoppingItemDTO;

import java.net.http.HttpResponse;
import java.util.List;

public interface IHttpParser {
    List<OnlineShoppingItemDTO> parse(final HttpResponse<String> resp);
}
