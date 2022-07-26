package net.markz.webscraper.api.controllers.configs;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.controllers.ITBase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static net.markz.webscraper.api.controllers.TestUtils.createTable;

@Configuration
@Slf4j
public class InitTestConfigs extends ITBase {
    @Bean
    AmazonSQS initializeAmazonSQS() {

        return AmazonSQSClientBuilder
                .standard()
                .withRegion(Regions.AP_SOUTHEAST_2)
                .build();
    }
    @Bean
    AmazonDynamoDB initializeAmazonDynamoDB() {

        final AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder
                .standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                        String.format("http://localhost:%d", getDynamoContainer().getMappedPort(8000))
                        , "ap-southeast-2"
                ))
                .build();


        createTable(amazonDynamoDB);
        return amazonDynamoDB;
    }
}
