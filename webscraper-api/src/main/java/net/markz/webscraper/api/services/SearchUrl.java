package net.markz.webscraper.api.services;

public enum SearchUrl {
  // Google search uses "uule" param to get geolocation-specific results.
  // This here I am hardcoding the encoded uule for Northcote,Auckland,New Zealand
  // so that we always get google shopping results from Northcote.
  GOOGLE_SHOPPING(
      "https://www.google.com/search?tbm=shop&q={}&tbs=vw:g,sales:1&sa=X&ved=2ahUKEwiimuOQj432AhVQXH0KHZkxCbEQzJkGegQIAhAG&" +
              "uule=w+CAIQICIeTm9ydGhjb3RlLEF1Y2tsYW5kLE5ldyBaZWFsYW5k");

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
