package net.markz.webscraper.api.services;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.daos.searchdao.SearchDao;
import net.markz.webscraper.api.exceptions.WebscraperException;
import net.markz.webscraper.api.parsers.DtoDataParser;
import net.markz.webscraper.api.parsers.ParserFactory;
import net.markz.webscraper.api.utils.Utils;
import net.markz.webscraper.model.OnlineShopDto;
import net.markz.webscraper.model.OnlineShoppingItemDto;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class SearchService {

  private final SearchDao searchDao;
  private final SeleniumDriverService seleniumDriverService;
  private final ParserFactory parserFactory;

  public void createOnlineShoppingItems(final List<OnlineShoppingItemDto> onlineShoppingItemDtos) {

    if(hasDuplicateItem(onlineShoppingItemDtos)) {
      throw new WebscraperException(HttpStatus.BAD_REQUEST, "Duplicate item detected. Aborting item creation.");
    }

    searchDao.upsertOnlineShoppingItems(onlineShoppingItemDtos.stream().map(DtoDataParser::parseDto).toList());
  }

  public OnlineShoppingItemDto getOnlineShoppingItem(final OnlineShoppingItemDto onlineShoppingItemDto) {

    final var userId = "markz";

    onlineShoppingItemDto.setUserId(userId);
    final var item = searchDao.getOnlineShoppingItemByPrimaryKey(DtoDataParser.parseDto(onlineShoppingItemDto));

    return DtoDataParser.parseData(
            item.orElseThrow(() -> new WebscraperException(HttpStatus.BAD_REQUEST, String.format("Cannot find item=%s", onlineShoppingItemDto)))
    );
  }

  public boolean hasDuplicateItem(final List<OnlineShoppingItemDto> onlineShoppingItemDtos) {
    final var duplicateItems = onlineShoppingItemDtos
            .stream()
            .filter(item -> {
      try {
       return getOnlineShoppingItem(item) != null;
      } catch (Exception e) {
        return false; // swallow exception if item not found.
      }
    })

            .toList();
    if(duplicateItems.isEmpty()) {
      return false;
    }
    log.error("Duplicate items:{} detected.", duplicateItems);
    return true;
  }

  public List<OnlineShoppingItemDto> getOnlineShoppingItems() {
    final var userId = "markz";

    return searchDao.getOnlineShoppingItemsByUser(userId)
            .stream()
            .map(DtoDataParser::parseData)
            .toList();
  }

  public void deleteOnlineShoppingItems(final List<OnlineShoppingItemDto> onlineShoppingItemDtos) {
    searchDao.deleteOnlineShoppingItems(
            onlineShoppingItemDtos.stream().map(DtoDataParser::parseDto).toList()
    );
  }

  public void updateOnlineShoppingItems(final List<OnlineShoppingItemDto> onlineShoppingItemDtos) {

    final var userId = "markz";

    onlineShoppingItemDtos.forEach(onlineShoppingItemDto -> onlineShoppingItemDto.setUserId(userId));
    searchDao.upsertOnlineShoppingItems(onlineShoppingItemDtos.stream().map(DtoDataParser::parseDto).toList());
  }

  public List<OnlineShoppingItemDto> scrapeSearchResults(
          @NonNull final OnlineShopDto onlineShopDto, @NonNull final String searchString) {

    WebDriver driver = null;
    var searchUrl = Utils.getSearchUrl(onlineShopDto);

    try {

      driver = seleniumDriverService.lazyLoadWebDriver();
      log.debug("Loading search url: {}", searchUrl);
      final var parsedSearchUrl = searchUrl.getSearchUrlWithPathParams(searchString);
      driver.get(parsedSearchUrl);
      log.debug("Parsed search url: {}", parsedSearchUrl);

      log.debug("Parsing search result");
      final var results = parserFactory
              .getSeleniumParser(onlineShopDto)
              .parse(driver);
      log.debug("Parsed search result={}", results);

      if(results == null) {
        return new ArrayList<>();
      }
      return results;

    } finally {
      if (driver != null) {
        // Clean up after scraping. Might need to re-initialize webDriver in case of browser session timeout.
        seleniumDriverService.handleWebDriverCleanUp(searchUrl);
      }
    }
  }
}
