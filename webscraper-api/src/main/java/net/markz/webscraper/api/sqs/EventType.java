package net.markz.webscraper.api.sqs;

public enum EventType {
    /**
     * Triggered by a cron job to scrape and alert if an online shopping item has a price change.
     */
    CRON_ITEM_UPDATE_AND_PRICE_CHANGE_ALERT
}
