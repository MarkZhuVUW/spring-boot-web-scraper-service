package net.markz.webscraper.api.constants;

public enum Constants {
    DYNAMO_TABLE_NAME_ONLINESHOPPINGITEMS("OnlineShoppingItems"),
    LAMBDA_REPLAY_TIMES("1"),
    LAMBDA_REPLAY_TIMES_ATTRIBUTE("replayTimes"),
    LAMBDA_REPLAY_DELAY_SECONDS("5");

    private final String str;

    Constants(final String str) {
        this.str = str;
    }

    public String getStr() {
        return str;
    }

}


