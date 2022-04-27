package net.markz.webscraper.api.controllers;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.SearchApiDelegate;
import net.markz.webscraper.api.services.SearchService;
import net.markz.webscraper.api.utils.Utils;
import net.markz.webscraper.model.GetSearchResultsResponse;
import net.markz.webscraper.model.OnlineShopDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SearchController implements SearchApiDelegate {

  private final SearchService searchService;

  @Override
  public ResponseEntity<GetSearchResultsResponse> getSearchResults(
      @PathVariable("onlineShopName") @NonNull final OnlineShopDto onlineShopName,
      @PathVariable("searchString") @NonNull final String searchString) {
    return Utils.translateException(
        () ->
            new GetSearchResultsResponse()
                .data(searchService.getSearchResults(onlineShopName, searchString)));
  }

}
