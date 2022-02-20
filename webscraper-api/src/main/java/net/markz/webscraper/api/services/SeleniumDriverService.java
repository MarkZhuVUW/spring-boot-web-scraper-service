package net.markz.webscraper.api.services;

import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;

@Service
public class SeleniumDriverService {
  public ChromeDriver getNewChromeDriver() {
    System.setProperty("webdriver.ie.driver", "../../../../../chromedriver-linux");
    return new ChromeDriver();
  }
}
