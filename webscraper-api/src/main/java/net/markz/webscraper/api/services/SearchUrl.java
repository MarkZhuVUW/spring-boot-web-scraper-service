package net.markz.webscraper.api.services;

public enum SearchUrl {
  COUNTDOWN("https://shop.countdown.co.nz/api/v1/products?target=search&search={}"),
  PET_CO("https://www.pet.co.nz/cat/search/{}"),
  RAW_ESSENTIALS("https://www.rawessentials.co.nz/products/cats"),
  THE_WAREHOUSE("https://www.thewarehouse.co.nz/search?q={}&lang=en-US"),
  GOOGLE_SHOPPING(
      "https://www.google.com/search?tbm=shop&q={}&tbs=vw:g,sales:1&sa=X&ved=2ahUKEwiimuOQj432AhVQXH0KHZkxCbEQzJkGegQIAhAG");

  private final String endpoint;

  SearchUrl(String endpoint) {
    this.endpoint = endpoint;
  }

  @Override
  public String toString() {
    return this.endpoint;
  }

  public String getSearchUrlWithPathParams(String searchString) {
    return endpoint.replace("{}", searchString);
  }

  public boolean isSelenium() {
    return this.equals(SearchUrl.PET_CO)
        || this.equals(SearchUrl.GOOGLE_SHOPPING)
        || this.equals(SearchUrl.THE_WAREHOUSE);
  }

  public boolean isHttp() {
    return this.equals(SearchUrl.COUNTDOWN);
  }
}
