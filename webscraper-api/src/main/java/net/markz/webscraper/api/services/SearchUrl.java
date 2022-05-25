package net.markz.webscraper.api.services;

public enum SearchUrl {
  COUNTDOWN(
      "https://www.countdown.co.nz/shop/searchproducts?search={}&page=1&filters=All;All;All;true;Specials%20%26%20Great%20Prices"), // % is the encoding for space
  PET_CO("https://www.pet.co.nz/pet/filter/promotions-all-t-1440/search/{}"),
  RAW_ESSENTIALS("https://www.rawessentials.co.nz/products/cats"),
  THE_WAREHOUSE("https://www.thewarehouse.co.nz/search?q={}&lang=en-US"),
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
