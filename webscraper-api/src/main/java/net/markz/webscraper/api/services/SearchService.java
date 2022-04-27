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

    WebDriver chromeDriver = null;

    try {
      var searchUrl = Utils.getSearchUrl(onlineShopDto);

      chromeDriver = getChromeDriver(searchUrl.getSearchUrlWithPathParams(searchString));
      return parserFactory
              .getSeleniumParser(onlineShopDto)
              .parse(chromeDriver);

    } finally {
      if (chromeDriver != null) {
        chromeDriver.quit(); // close browser session.
      }
    }
  }

  private WebDriver getChromeDriver(String searchUrl) {

    log.debug("Getting new chrome driver instance");
    WebDriver chromeDriver = seleniumDriverService.getNewChromeDriver();
    log.debug("Got new chrome driver instance");

    log.debug("Getting chrome driver with search url: {}", searchUrl);
    chromeDriver.get(searchUrl);
    log.debug("Got chrome driver with search url: {}", searchUrl);

    return chromeDriver;
  }
}
