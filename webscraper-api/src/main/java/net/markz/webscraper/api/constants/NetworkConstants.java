package net.markz.webscraper.api.constants;


public enum NetworkConstants {
    LOCAL_SELENIUM_HOST_NAME("http://selenium-remote-chrome-driver-service:4444/wd/hub"),
    ECS_SELENINUM_HOST_NAME("http://localhost:4444/wd/hub");

    private final String str;

    NetworkConstants(final String str) {
        this.str = str;
    }

    public String getStr() {
        return str;
    }

}

