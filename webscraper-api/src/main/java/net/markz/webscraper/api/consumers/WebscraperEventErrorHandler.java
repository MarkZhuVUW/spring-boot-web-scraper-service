package net.markz.webscraper.api.consumers;

import com.amazonaws.services.sqs.AmazonSQS;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebscraperEventErrorHandler extends AbstractEventErrorHandler{

    @Value("${amazon.sqs.queue.url}")
    private final String sqsQueueUrl;

    @Autowired
    private final AmazonSQS amazonSQS;

    @Value("${amazon.sqs.dlq.url}")
    private final String deadLetterQueueUrl;

    @Autowired
    public WebscraperEventErrorHandler(
            final String sqsQueueUrl,
            final AmazonSQS amazonSQS,
            final String deadLetterQueueUrl
    ) {
        super(sqsQueueUrl, amazonSQS, deadLetterQueueUrl);
        this.deadLetterQueueUrl = deadLetterQueueUrl;
        this.amazonSQS = amazonSQS;
        this.sqsQueueUrl = sqsQueueUrl;
    }

    @Override
    public Logger getLogger() {
        return log;
    }
}
