package net.markz.webscraper.api.constants;

public enum Constants {
    DYNAMO_TABLE_NAME_ONLINESHOPPINGITEMS("OnlineShoppingItems");

    private final String str;

    Constants(final String str) {
        this.str = str;
    }

    public String getStr() {
        return str;
    }

}


