package net.markz.webscraper.api.consumers;

import com.amazonaws.services.sqs.AmazonSQS;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;


/**
 * Currently my ec2 instances are under-utilized
 * so I am temporarily polling from sqs queue with my webscraper service application load balancer.
 * TODO: Refactor this to lambda
 */

@Slf4j
@Component
public class WebscraperEventExceptionHandler extends AbstractEventErrorHandler {

    @Autowired
    private Environment env;

    @Autowired
    private final AmazonSQS amazonSQS;

    @Autowired
    public WebscraperEventExceptionHandler(
            final Environment env,
            final AmazonSQS amazonSQS
    ) {
        super(
                env.getProperty("amazon.sqs.queue.url"),
                amazonSQS,
                env.getProperty("amazon.sqs.dlq.url")
        );
        this.amazonSQS = amazonSQS;
    }

    @Override
    public Logger getLogger() {
        return log;
    }
}
