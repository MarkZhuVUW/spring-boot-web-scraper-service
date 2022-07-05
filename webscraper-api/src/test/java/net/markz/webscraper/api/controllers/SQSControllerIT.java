package net.markz.webscraper.api.controllers;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.constants.Constants;
import net.markz.webscraper.api.exceptions.WebscraperException;
import net.markz.webscraper.model.CreateOnlineShoppingItemsRequest;
import net.markz.webscraper.model.DeleteOnlineShoppingItemsRequest;
import net.markz.webscraper.model.OnlineShopDto;
import net.markz.webscraper.model.OnlineShoppingItemDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;

import java.util.Objects;

import static net.markz.webscraper.api.controllers.TestUtils.createQueue;
import static net.markz.webscraper.api.controllers.TestUtils.createTable;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@SpringBootTest
@Slf4j
@Configuration
class SQSControllerIT {

    @Autowired
    private SearchController searchController;

    @Autowired
    private SQSController sqsController;

    @Autowired
    private AmazonDynamoDB amazonDynamoDB;

    @Autowired
    private AmazonSQS amazonSQS;

    private static String queueUrl;

    private static final String USERID = "markz";

    private static final GenericContainer LOCALSTACK;

    static {

        LOCALSTACK = new GenericContainer("amazon/dynamodb-local:latest")
                .withCommand("-jar DynamoDBLocal.jar -inMemory -sharedDb")
                .withExposedPorts(8000)
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(
                        SQSControllerIT.class)));
        LOCALSTACK.start();

    }

    @AfterEach
    public void afterEach() {
        amazonDynamoDB.deleteTable(Constants.DYNAMO_TABLE_NAME_ONLINESHOPPINGITEMS.getStr());
        amazonSQS.deleteQueue(queueUrl);

        createTable(amazonDynamoDB);
        queueUrl = createQueue(amazonSQS).getQueueUrl();
    }

    @AfterAll
    public static void terminate() {
        LOCALSTACK.stop();

    }

    @Test
    public void poll_messageLastModifiedDate_beforeExistingItem_fail() {

        // Given
        final var item1 = new OnlineShoppingItemDto()
                .onlineShopName(OnlineShopDto.GOOGLE_SHOPPING.name())
                .userId(USERID)
                .uuid("testUUID1")
                .name("testItem1");

        final var createReq = new CreateOnlineShoppingItemsRequest()
                .addDataItem(item1);

        ResponseEntity<Void> createResp = searchController.createOnlineShoppingItems(createReq);

        final var items = Objects.requireNonNull(
                searchController
                        .getOnlineShoppingItems()
                        .getBody()
        ).getData();

        assertEquals(HttpStatus.OK, createResp.getStatusCode());
        assertEquals(1, items.size());
        assertEquals(item1, items.get(0));

        item1.setSalePrice("123");
        item1.setHref("123");


        // The created item will be parsed to message and pushed to SQS
        sqsController.sqsProduce();

        // Try polling.
        sqsController.sqsPoll();


    }


    // By design batchDelete in dynamodb is idempotent. Running the same api multiple times does not generate error responses.
    @Test
    public void deleteNonExistingOnlineShoppingItem_success() {

        // Given
        final var item = new OnlineShoppingItemDto()
                .onlineShopName(OnlineShopDto.GOOGLE_SHOPPING.name())
                .userId(USERID)
                .uuid("testUUID")
                .name("testItem");

        final var req = new DeleteOnlineShoppingItemsRequest().addDataItem(item);

        // When
        ResponseEntity<Void> result = searchController.deleteOnlineShoppingItems(req);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertThrows(
                WebscraperException.class,
                () -> searchController.getOnlineShoppingItem(item.getOnlineShopName(), item.getName())
        );

    }

    @Bean
    public AmazonDynamoDB initializeAmazonDynamoDB() {

        final AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder
                .standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(String.format("http://localhost:%d", LOCALSTACK.getMappedPort(8000)), "ap-southeast-2"))
                .build();


        createTable(amazonDynamoDB);
        return amazonDynamoDB;
    }

    @Bean
    public AmazonSQS initializeAmazonSQS() {

        final var amazonSQS = AmazonSQSClientBuilder
                .standard()
                .withRegion(Regions.AP_SOUTHEAST_2)
                .build();

        queueUrl = createQueue(amazonSQS).getQueueUrl();
        return amazonSQS;
    }

}