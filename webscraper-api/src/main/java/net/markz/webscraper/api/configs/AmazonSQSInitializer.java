package net.markz.webscraper.api.configs;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Slf4j
@Profile("!test")
public class AmazonSQSInitializer {

    @Value("${amazon.region}")
    private String region;

    @Value("${amazon.sqs.queue.url}")
    private String queueUrl;

    @Value("${amazon.sqs.dlq.url}")
    private String dlqUrl;

    @Bean
    public AmazonSQS amazonSQS() {
        final var amazonSQS = AmazonSQSClientBuilder
                .standard()
                .withRegion(Regions.AP_SOUTHEAST_2)
                .build();
        log.info("Created SQS client with queue url={}", queueUrl);

        return amazonSQS;
    }

}
