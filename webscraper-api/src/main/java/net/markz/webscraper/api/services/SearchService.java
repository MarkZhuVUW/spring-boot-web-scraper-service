package net.markz.webscraper.api.services;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.daos.ISearchDao;
import net.markz.webscraper.api.exceptions.WebscraperException;
import net.markz.webscraper.api.parsers.ParserFactory;
import net.markz.webscraper.api.utils.Utils;
import net.markz.webscraper.model.OnlineShopDto;
import net.markz.webscraper.model.OnlineShoppingItemDTO;
import org.apache.catalina.util.URLEncoder;
import org.openqa.selenium.WebDriver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class SearchService {

  private final ISearchDao ISearchDao;
  private final SeleniumDriverService seleniumDriverService;
  private final ParserFactory parserFactory;

  public List<OnlineShoppingItemDTO> getSearchResults(
          @NonNull final OnlineShopDto onlineShopDto, @NonNull final String searchString) {

    SearchUrl searchUrl = null;
    WebDriver chromeDriver = null;

    try {
      searchUrl = Utils.getSearchUrl(onlineShopDto);
      chromeDriver = getChromeDriver(searchUrl.getSearchUrlWithPathParams(searchString));
      if (searchUrl.isSelenium()) {
        return parserFactory
                .getSeleniumParser(onlineShopDto)
                .parse(chromeDriver);
      }
      if (searchUrl.isHttp()) {
        return parserFactory
                .getHttpParser(onlineShopDto)
                .parse(
                        getHttpResp(
                                searchUrl.getSearchUrlWithPathParams(
                                        URLEncoder.QUERY.encode(searchString, StandardCharsets.UTF_8))
                        )
                );
      }
      throw new WebscraperException(
              HttpStatus.BAD_REQUEST, "Unsupported onlineShop: " + onlineShopDto);
    } catch (URISyntaxException e) {
      log.error("Failed to parse url: {}", searchUrl);
      throw new WebscraperException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (IOException | InterruptedException e) {
      log.info(
              "Failed to make call to url: {} with OnlineShopDto: {} and searchString: {}",
              searchUrl,
              onlineShopDto,
              searchString);
      throw new WebscraperException(HttpStatus.BAD_REQUEST, e.getMessage());
    } finally {
      if (chromeDriver != null) {
        chromeDriver.close(); // close browser after each search to save RAM.
      }
    }
  }

  private HttpResponse<String> getHttpResp(String searchUrl)
          throws URISyntaxException, IOException, InterruptedException {
    final var req =
            HttpRequest.newBuilder()
                    .uri(new URI(searchUrl))
                    .header("x-requested-with", "OnlineShopping.WebApp")
                    .GET()
                    .build();

    return HttpClient.newBuilder().build().send(req, HttpResponse.BodyHandlers.ofString());
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
