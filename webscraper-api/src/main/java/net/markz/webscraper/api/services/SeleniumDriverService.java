package net.markz.webscraper.api.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.constants.NetworkConstants;
import net.markz.webscraper.api.exceptions.WebscraperException;
import org.openqa.selenium.WebDriver;
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
@RequiredArgsConstructor
public class SeleniumDriverService {

  @Autowired
  private Environment env;

  // We want to keep the driver object but we don't want to invert the control of creating this bean.
  private WebDriver driver;

  public WebDriver lazyLoadWebDriver() {

    if(driver != null) {
      return driver;
    }

    log.debug("Getting new chrome driver instance");

    final String remote_url_chrome = Arrays.stream(env.getActiveProfiles()).toList().contains("local") ?
            NetworkConstants.LOCAL_SELENIUM_HOST_NAME.getStr() :
            NetworkConstants.ECS_SELENINUM_HOST_NAME.getStr();
    final ChromeOptions opts = new ChromeOptions();

    opts.addArguments("--no-sandbox"); // Bypass OS security model
    //    opts.addArguments("incognito");
    opts.addArguments("--disable-dev-shm-usage");
    opts.addArguments("--headless");

    // Make headless chrome invisible.
    final var userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.41 Safari/537.36";
    opts.addArguments(String.format("user-agent=%s", userAgent));

    try {
      this.driver = new RemoteWebDriver(new URL(remote_url_chrome), opts);
      log.debug("Got new chrome driver instance");

      return this.driver;
    } catch (MalformedURLException e) {
      throw new WebscraperException(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Invalid url: %s", remote_url_chrome));
    }
  }
}
