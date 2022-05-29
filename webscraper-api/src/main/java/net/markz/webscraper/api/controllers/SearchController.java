package net.markz.webscraper.api.controllers;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.SearchApiDelegate;
import net.markz.webscraper.api.services.SearchService;
import net.markz.webscraper.api.utils.Utils;
import net.markz.webscraper.model.CreateSearchResultsRequest;
import net.markz.webscraper.model.DeleteSearchResultsRequest;
import net.markz.webscraper.model.GetSearchResultsResponse;
import net.markz.webscraper.model.OnlineShopDto;
import net.markz.webscraper.model.UpdateSearchResultsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SearchController implements SearchApiDelegate {

  private final SearchService searchService;


    @Override
    public ResponseEntity<GetSearchResultsResponse> scrapeSearchResults(
            @NonNull final OnlineShopDto onlineShopName,
            @NonNull final String searchString) {
        return Utils.translateException(
                () ->
                        new GetSearchResultsResponse()
                                .data(searchService.scrapeSearchResults(onlineShopName, searchString))
        );
    }

  @Override
  public ResponseEntity<GetSearchResultsResponse> getSearchResults() {
    return Utils.translateException(
            () ->
                    new GetSearchResultsResponse()
                            .data(searchService.getOnlineShoppingItems())
    );
  }

  @Override
  public ResponseEntity<Void> createOnlineShoppingItems(
          final CreateSearchResultsRequest createSearchResultsRequest) {
    return Utils.translateException(() -> {
              searchService.createOnlineShoppingItems(createSearchResultsRequest.getData());
              return null;
            });
  }

    @Override
    public ResponseEntity<Void> updateOnlineShoppingItems(
            final UpdateSearchResultsRequest updateSearchResultsRequest) {
        return Utils.translateException(() -> {
            searchService.updateOnlineShoppingItems(updateSearchResultsRequest.getData());
            return null;
        });
    }

    @Override
    public ResponseEntity<Void> deleteOnlineShoppingItems(
            final DeleteSearchResultsRequest deleteSearchResultsRequest) {
        return Utils.translateException(() -> {
            searchService.deleteOnlineShoppingItems(deleteSearchResultsRequest.getData());
            return null;
        });
    }



}
