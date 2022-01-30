package net.markz.webscraper.api.services;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import net.markz.webscraper.api.daos.SearchDao;
import net.markz.webscraper.model.GetSearchResultsResponse;
import net.markz.webscraper.model.OnlineShopDto;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SearchService {

  private final SearchDao searchDao;

  public GetSearchResultsResponse getSearchResults(
      @NonNull final OnlineShopDto onlineShopDto, @NonNull final String searchString) {

    return new GetSearchResultsResponse()
        .data(searchDao.getSearchResults(onlineShopDto, searchString));
  }
}
