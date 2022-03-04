package net.markz.webscraper.api.services;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

@Service
public class SeleniumDriverService {
  public ChromeDriver getNewChromeDriver() {
    System.setProperty("webdriver.ie.driver", "../../../../../chromedriver-linux");
    ChromeOptions opt = new ChromeOptions();
    opt.addArguments("–no-sandbox");
    opt.addArguments("incognito");
    return new ChromeDriver(opt);
  }
}
