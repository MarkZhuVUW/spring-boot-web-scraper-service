package net.markz.webscraper.api.parsers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.exceptions.WebscraperException;
import net.markz.webscraper.api.utils.Utils;
import net.markz.webscraper.model.OnlineShopDto;
import net.markz.webscraper.model.OnlineShoppingItemDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
@Slf4j
public class CountdownParser implements IHttpParser {

    @Override
    public List<OnlineShoppingItemDTO> parse(final HttpResponse<String> resp) {

        try {
            log.debug("parsing countdown response={}", Utils.reflectionToString(resp));
            @SuppressWarnings("unchecked") final var body = (Map<String, Map<String, Object>>)
                    (new ObjectMapper().readValue(resp.body(), Map.class).get("products"));
            @SuppressWarnings("unchecked") final var items = (List<Map<String, Object>>) body.get("items");
            return items
                    .stream()
                    .filter(item -> {
                        @SuppressWarnings("unchecked") Map<String, Object> price = (Map<String, Object>) item.get("price");
                        return (Double)price.get("savePrice") != 0;
                    })
                    .map(item -> {
                        @SuppressWarnings("unchecked") Map<String, Object> images = (Map<String, Object>) item.get("images");
                        @SuppressWarnings("unchecked") Map<String, Object> price = (Map<String, Object>) item.get("price");

                        return new OnlineShoppingItemDTO()
                                .onlineShop(OnlineShopDto.COUNTDOWN)
                                .onlineShopName(OnlineShopDto.COUNTDOWN.name())
                                .imageUrl((String) images.get("big"))
                                .name((String) item.get("name"))
                                .salePrice(Double.toString((Double)price.get("salePrice")))
                                .uuid((String) item.get("barcode"))
                                // TODO: Implement database
                                .isSaved(false);
                    })
                    .toList();
        } catch (final JsonProcessingException e) {

            log.info("Failed to parse json: {}", Utils.reflectionToString(resp.body()));
            throw new WebscraperException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

    }
}
