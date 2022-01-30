package net.markz.webscraper.api.services;

public enum SearchUrl {
  COUNTDOWN("https://shop.countdown.co.nz/api/v1/products?target=search&search={}");

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
}
