package net.markz.webscraper.api.consumers;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.daos.searchdao.OnlineShoppingItem;
import net.markz.webscraper.api.exceptions.WebscraperException;
import net.markz.webscraper.api.parsers.DtoDataParser;
import net.markz.webscraper.api.services.DistributedLockService;
import net.markz.webscraper.api.services.SearchService;
import net.markz.webscraper.api.sqs.Message;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class WebscraperEventProcessor extends AbstractEventProcessor <OnlineShoppingItem>{

    @Autowired
    private final SearchService searchService;

    @Autowired
    public WebscraperEventProcessor(
            final WebscraperEventErrorHandler errorHandler,
            final AmazonSQS amazonSQS,
            final SearchService searchService,
            final Environment env,
            final DistributedLockService distributedLockService
    ) {
        super(
                buildExecutorService(),
                errorHandler,
                env.getProperty("amazon.sqs.queue.url"),
                amazonSQS,
                distributedLockService
        );
        this.searchService = searchService;
    }

    /**
     * Scrape item in message body by exact item name, update it in db and send price change alert.
     * @param message
     */
    @Override
    public void processMessage(final Message<OnlineShoppingItem> message) {
        log.info("Processing message={}", message);



        message.getData().forEach(onlineShoppingItem -> {
            // Scrape item in message body by exact item name
            final var searchResult = searchService.scrapeSearchResults(
                    onlineShoppingItem.getOnlineShop(),
                    onlineShoppingItem.getName()
            );

            if(searchResult.isEmpty()) {
                log.info(
                        "Cannot find item in onlineShopName={}, item name={}",
                        onlineShoppingItem.getOnlineShop(),
                        onlineShoppingItem.getName()
                );
                throw new WebscraperException(
                        HttpStatus.BAD_REQUEST,
                        String.format("Error scraping for message item=%s, will replay later.", onlineShoppingItem)
                );
            }


            // Update item if they are not identical.
            if(!onlineShoppingItem.getUuid().equals(searchResult.get(0).getUuid())) {
                log.info(
                        "uuid of the scraped item {} is different to the item in message body: {}. " +
                                "Maybe the scraped item is not the same as whats in the message. Abort processing.",
                        onlineShoppingItem.getUuid(),
                        searchResult.get(0)
                );
                return;
            }

            // Alert price change.
            if(!onlineShoppingItem.getSalePrice().equals(searchResult.get(0).getSalePrice())) {
                log.info("Current price={}, stored price={}", searchResult.get(0), onlineShoppingItem.getSalePrice());
                // do alert
            }

            searchService.updateOnlineShoppingItems(List.of(searchResult.get(0)));
        });
    }

    @Override
    public Message<OnlineShoppingItem> parseMessage(final SQSEvent.SQSMessage message) {
        log.info("Parsing message={}", message);

        try {
            return new ObjectMapper().readValue(
                    message.getBody(),
                    new TypeReference<>() {}
            );
        } catch (JsonProcessingException e) {
            throw new WebscraperException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format("Message body=%s cannot be parsed from string", message.getBody())
            );
        }
    }

    @Override
    public Logger getLogger() {
        return log;
    }

    @Override
    public boolean shouldIgnoreMessage(final Message<OnlineShoppingItem> message) {

        return message.getData()
                .stream()
                .filter(item -> {
                    final var messageLastModified = item.getLastModifiedDate();
                    final var dbLastModified = DtoDataParser
                            .parseDto(searchService.getOnlineShoppingItem(
                                    DtoDataParser.parseData(item)
                            ))
                            .getLastModifiedDate();


                    return messageLastModified.isBefore(dbLastModified);
                })
                .toList()
                .isEmpty();
    }

    @Override
    public long getLockTimeout() {
        return 60000L; // Scraping a website could be extremely slow. I will set timeout to 60s
    }

    /**
     * Lock key.
     *
     * @return
     */
    @Override
    public String getLockKey(OnlineShoppingItem obj) {
        return null;
    }

    private static ExecutorService buildExecutorService() {
        final var factory = new BasicThreadFactory
                .Builder()
                .namingPattern(SQSConsumerLambda.class.getSimpleName() + "-%d")
                .build();
        return new ThreadPoolExecutor(
                10,
                50,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(200), factory
        );
    }

}
