package net.markz.webscraper.api.services;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.daos.ISearchDao;
import net.markz.webscraper.api.parsers.ParserFactory;
import net.markz.webscraper.api.utils.Utils;
import net.markz.webscraper.model.OnlineShopDto;
import net.markz.webscraper.model.OnlineShoppingItemDTO;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class SearchService {

  private final ISearchDao ISearchDao;
  private final SeleniumDriverService seleniumDriverService;
  private final ParserFactory parserFactory;

  public List<OnlineShoppingItemDTO> getSearchResults(
          @NonNull final OnlineShopDto onlineShopDto, @NonNull final String searchString) {

    WebDriver driver = null;
    var searchUrl = Utils.getSearchUrl(onlineShopDto);

    try {

      driver = seleniumDriverService.lazyLoadWebDriver();
      log.debug("Getting chrome driver with search url: {}", searchUrl);
      driver.get(searchUrl.getSearchUrlWithPathParams(searchString));
      log.debug("Got chrome driver with search url: {}", searchUrl);

      final var results = parserFactory
              .getSeleniumParser(onlineShopDto)
              .parse(driver);

      if(results == null) {
        return new ArrayList<>();
      }
      return results;

    } finally {
      if (driver != null) {
        final var openWindowTabs = driver.getWindowHandles();
        log.debug("Number of open tabs after scraping {}: {} ", searchUrl, openWindowTabs.size());
        if(openWindowTabs.size() > 1) {

          for (int i = 0; i < openWindowTabs.size() - 1; i++) {
            driver.close(); // close most tabs but leave one to ensure browser is still open for reusing.
          }
        }
        log.debug("Number of open tabs after cleaning: {} ", openWindowTabs.size());

//        driver.quit();

      }
    }
  }

}
