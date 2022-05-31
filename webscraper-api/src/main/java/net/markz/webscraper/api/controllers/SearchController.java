package net.markz.webscraper.api.controllers;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.SearchApiDelegate;
import net.markz.webscraper.api.parsers.DtoDataParser;
import net.markz.webscraper.api.services.SearchService;
import net.markz.webscraper.api.utils.Utils;
import net.markz.webscraper.model.CreateOnlineShoppingItemsRequest;
import net.markz.webscraper.model.DeleteOnlineShoppingItemsRequest;
import net.markz.webscraper.model.GetOnlineShoppingItemResponse;
import net.markz.webscraper.model.GetOnlineShoppingItemsResponse;
import net.markz.webscraper.model.OnlineShopDto;
import net.markz.webscraper.model.OnlineShoppingItemDto;
import net.markz.webscraper.model.UpdateOnlineShoppingItemRequest;
import net.markz.webscraper.model.UpdateOnlineShoppingItemsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SearchController implements SearchApiDelegate {

    private final SearchService searchService;


    @Override
    public ResponseEntity<GetOnlineShoppingItemsResponse> scrapeSearchResults(
            @NonNull final OnlineShopDto onlineShopName,
            @NonNull final String searchString) {
        return Utils.translateException(
                () ->
                        new GetOnlineShoppingItemsResponse()
                                .data(searchService.scrapeSearchResults(onlineShopName, searchString))
        );
    }

    @Override
    public ResponseEntity<GetOnlineShoppingItemsResponse> getOnlineShoppingItems() {
        return Utils.translateException(
                () ->
                        new GetOnlineShoppingItemsResponse()
                                .data(searchService.getOnlineShoppingItems())
        );
    }

    @Override
    public ResponseEntity<Void> createOnlineShoppingItems(
            final CreateOnlineShoppingItemsRequest createOnlineShoppingItemsRequest) {
        return Utils.translateException(() -> {
            searchService.createOnlineShoppingItems(createOnlineShoppingItemsRequest.getData());
            return null;
        });
    }

    @Override
    public ResponseEntity<Void> updateOnlineShoppingItems(
            final UpdateOnlineShoppingItemsRequest updateOnlineShoppingItemsRequest) {
        return Utils.translateException(() -> {
            searchService.updateOnlineShoppingItems(updateOnlineShoppingItemsRequest.getData());
            return null;
        });
    }

    @Override
    public ResponseEntity<Void> deleteOnlineShoppingItems(
            final DeleteOnlineShoppingItemsRequest deleteOnlineShoppingItemsRequest) {
        return Utils.translateException(() -> {
            searchService.deleteOnlineShoppingItems(deleteOnlineShoppingItemsRequest.getData());
            return null;
        });
    }


    @Override
    public ResponseEntity<Void> updateOnlineShoppingItem(
            final String shopName,
            final String name,
            final UpdateOnlineShoppingItemRequest updateOnlineShoppingItemRequest
    ) {
    return Utils.translateException(
        () -> {
          searchService.updateOnlineShoppingItems(List.of(updateOnlineShoppingItemRequest.getData()));
          return null;
        });
    }

    @Override
    public ResponseEntity<GetOnlineShoppingItemResponse> getOnlineShoppingItem(
            final String shopName,
            final String name
    ) {
        return Utils.translateException(
                () ->
                        new GetOnlineShoppingItemResponse()
                                .data(searchService.getOnlineShoppingItem(DtoDataParser.parseDto(
                                        new OnlineShoppingItemDto()
                                                .onlineShopName(shopName)
                                                .name(name))
                                ))
        );
    }
}
