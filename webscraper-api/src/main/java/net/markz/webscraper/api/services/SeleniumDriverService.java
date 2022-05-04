package net.markz.webscraper.api.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.constants.NetworkConstants;
import net.markz.webscraper.api.exceptions.WebscraperException;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SeleniumDriverService {

  private final Environment env;

  public RemoteWebDriver getNewChromeDriver() {

    final String remote_url_chrome = Arrays.stream(env.getActiveProfiles()).toList().contains("local") ?
            NetworkConstants.LOCAL_SELENIUM_HOST_NAME.getStr() :
            NetworkConstants.ECS_SELENINUM_HOST_NAME.getStr();
    ChromeOptions opts = new ChromeOptions();

    opts.addArguments("--no-sandbox"); // Bypass OS security model
//    opts.addArguments("incognito");
    opts.addArguments("--disable-dev-shm-usage");
    opts.addArguments("--headless");

    // Make headless chrome invisible.
    final var userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.41 Safari/537.36";
    opts.addArguments(String.format("user-agent=%s", userAgent));

    try {
      return new RemoteWebDriver(new URL(remote_url_chrome), opts);
    } catch (MalformedURLException e) {
      throw new WebscraperException(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Invalid url: %s", remote_url_chrome));
    }
  }
}
