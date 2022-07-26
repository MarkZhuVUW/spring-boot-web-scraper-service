package net.markz.webscraper.api.consumers;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.exceptions.WebscraperException;
import net.markz.webscraper.api.services.DistributedLockService;
import net.markz.webscraper.api.services.SearchService;
import net.markz.webscraper.api.sqs.Message;
import net.markz.webscraper.model.OnlineShoppingItemDto;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class WebscraperEventProcessor extends AbstractEventProcessor<OnlineShoppingItemDto> {

    private final SearchService searchService;

    @Autowired
    public WebscraperEventProcessor(
            final WebscraperEventExceptionHandler errorHandler,
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
    public void processMessage(final Message<OnlineShoppingItemDto> message) {
        message.getData().forEach(dto -> {
            // Scrape item in message body by exact item name
            final var searchResult = searchService.scrapeSearchResults(
                    dto.getOnlineShop(),
                    dto.getName()
            );

            if(searchResult.isEmpty()) {
                log.info(
                        "Cannot find item in onlineShopName={}, item name={}",
                        dto.getOnlineShop(),
                        dto.getName()
                );
                throw new WebscraperException(
                        HttpStatus.BAD_REQUEST,
                        String.format("Error scraping for message item=%s, will replay later.", dto)
                );
            }


            // Update item if they are not identical.
            if(!dto.getUuid().equals(searchResult.get(0).getUuid())) {
                log.info(
                        "uuid of the scraped item {} is different to the item in message body: {}. " +
                                "Maybe the scraped item is not the same as whats in the message. Abort processing.",
                        dto.getUuid(),
                        searchResult.get(0)
                );
                return;
            }

            // Alert price change.
            if(!dto.getSalePrice().equals(searchResult.get(0).getSalePrice())) {
                log.info("Current price={}, stored price={}", searchResult.get(0), dto.getSalePrice());
                // TODO do alert
            }

            searchService.updateOnlineShoppingItems(List.of(searchResult.get(0)));
        });
    }

    @Override
    public Message<OnlineShoppingItemDto> parseMessage(final SQSEvent.SQSMessage message) {
        try {
            return new ObjectMapper().readValue(
                    message.getBody(),
                    new TypeReference<Message<OnlineShoppingItemDto>>() {}
            );
        } catch (JsonProcessingException e) {
            throw new WebscraperException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage());
        }
    }

    @Override
    public Logger getLogger() {
        return log;
    }


    @Override
    public boolean shouldIgnoreMessage(final Message<OnlineShoppingItemDto> message) {

    return message.getData().stream()
        .filter(
            dto -> {
              final var messageLastModified = LocalDateTime.parse(dto.getLastModifiedDate());
              final var dbLastModified =
                  searchService.getOnlineShoppingItem(dto).getLastModifiedDate();

              return messageLastModified.isBefore(LocalDateTime.parse(dbLastModified));
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
    public String getLockKey(OnlineShoppingItemDto obj) {
        return "online-shopping-item-lock-key-" + obj.getUuid();
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
