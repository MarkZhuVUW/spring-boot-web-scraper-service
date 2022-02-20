package net.markz.webscraper.api.services;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.daos.SearchDao;
import net.markz.webscraper.api.exceptions.WebscraperException;
import net.markz.webscraper.api.utils.Utils;
import net.markz.webscraper.model.GetSearchResultsResponse;
import net.markz.webscraper.model.OnlineShopDto;
import org.openqa.selenium.WebDriver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@AllArgsConstructor
@Slf4j
public class SearchService {

  private final SearchDao searchDao;
  private final SeleniumDriverService seleniumDriverService;

  public GetSearchResultsResponse getSearchResults(
      @NonNull final OnlineShopDto onlineShopDto, @NonNull final String searchString) {

    SearchUrl searchUrl = null;

    try {
      searchUrl = Utils.getSearchUrl(onlineShopDto);
      if (searchUrl.isSelenium()) {
        return new GetSearchResultsResponse()
            .data(
                Parser.parse(
                    onlineShopDto,
                    null,
                    getChromeDriver(searchUrl.getSearchUrlWithPathParams(searchString))));
      }
      if (searchUrl.isHttp()) {
        return new GetSearchResultsResponse()
            .data(
                Parser.parse(
                    onlineShopDto,
                    getHttpResp(searchUrl.getSearchUrlWithPathParams(searchString)),
                    null));
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
      log.error("checked exception caught: {}", e.toString());
      throw new WebscraperException(HttpStatus.BAD_REQUEST, e.getMessage());
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
