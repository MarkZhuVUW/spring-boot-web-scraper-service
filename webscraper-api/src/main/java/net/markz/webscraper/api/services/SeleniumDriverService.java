package net.markz.webscraper.api.services;

import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.exceptions.WebscraperException;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;

@Service
@Slf4j
public class SeleniumDriverService {
  public RemoteWebDriver getNewChromeDriver() {
    final String remote_url_chrome = "http://selenium-remote-chrome-driver-service:4444/wd/hub";
    ChromeOptions opts = new ChromeOptions();

    opts.addArguments("–no-sandbox");
    opts.addArguments("incognito");
    try {
      return new RemoteWebDriver(new URL(remote_url_chrome), opts);
    } catch (MalformedURLException e) {
      throw new WebscraperException(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Invalid url: %s", remote_url_chrome));
    }
  }
}
