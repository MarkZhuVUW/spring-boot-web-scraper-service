package net.markz.webscraper.api.consumers;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.sqs.AmazonSQS;
import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.daos.searchdao.OnlineShoppingItem;
import net.markz.webscraper.api.services.SearchService;
import net.markz.webscraper.api.utils.Utils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class WebscraperEventProcessor extends AbstractEventProcessor <OnlineShoppingItem>{

    @Autowired
    private final SearchService searchService;

    @Value("${amazon.sqs.queue.url}")
    private final String sqsQueueUrl;

    @Autowired
    private final AmazonSQS amazonSQS;

    @Autowired
    public WebscraperEventProcessor(
            final WebscraperEventErrorHandler errorHandler,
            final String sqsQueueUrl,
            final AmazonSQS amazonSQS,
            final SearchService searchService
            ) {


        super(buildExecutorService(), errorHandler, sqsQueueUrl, amazonSQS);
        this.amazonSQS = amazonSQS;
        this.sqsQueueUrl = sqsQueueUrl;
        this.searchService = searchService;
    }

    @Override
    public void processMessage(final Message<OnlineShoppingItem> message) {

    }

    @Override
    public Message<OnlineShoppingItem> parseMessage(final SQSEvent.SQSMessage message) {
        return null;
    }

    @Override
    public Logger getLogger() {
        return log;
    }

    @Override
    public boolean shouldIgnoreMessage(final Message<OnlineShoppingItem> message) {

        final var lastModified = Utils.toUtcDateTime(message.getLastModified());
        final var currentLastModified = Utils.toUtcDateTime(searchService.getOnlineShoppingItem(message.getData()).getLastModifiedDate());

        return lastModified.isBefore(currentLastModified);
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
