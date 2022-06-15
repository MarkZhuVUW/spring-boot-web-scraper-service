package net.markz.webscraper.api.controllers;


import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.constants.Constants;
import net.markz.webscraper.api.daos.searchdao.OnlineShoppingItem;
import net.markz.webscraper.model.CreateOnlineShoppingItemsRequest;
import net.markz.webscraper.model.OnlineShopDto;
import net.markz.webscraper.model.OnlineShoppingItemDto;
import net.markz.webscraper.model.UpdateOnlineShoppingItemsRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
@Slf4j
@Configuration
public class SearchControllerIT {

    @Autowired
    private SearchController searchController;

    @Autowired
    private AmazonDynamoDB amazonDynamoDB;

    private static final String USERID = "markz";

    private static final GenericContainer LOCALSTACK;

    static {

        LOCALSTACK = new GenericContainer("amazon/dynamodb-local:latest")
                .withCommand("-jar DynamoDBLocal.jar -inMemory -sharedDb")
                .withExposedPorts(8000)
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(
                        SearchControllerIT.class)));
        LOCALSTACK.start();

    }

    @AfterEach
    public void afterEach() {
        amazonDynamoDB.deleteTable(Constants.DYNAMO_TABLE_NAME_ONLINESHOPPINGITEMS.getStr());
        createTable(amazonDynamoDB);
    }

    @AfterAll
    public static void terminate() {
        LOCALSTACK.stop();

    }

    @Test
    public void deleteExistingOnlineShoppingItem_success() {

        // TODO

    }

    @Test
    public void deleteNonExistingOnlineShoppingItem_fail() {

        // TODO

    }



    @Test
    public void upsertExistingOnlineShoppingItem_success() {

        // TODO

    }

    @Test
    public void upsertNonExistingOnlineShoppingItem_success() {

      // TODO

    }

    @Test
    public void upsertNonExistingOnlineShoppingItem_withLastModifiedDateBefore_fail() {
       // TODO
    }

    @Test
    public void upsertExistingOnlineShoppingItem_withLastModifiedDateBefore_fail() {
        // TODO
    }

    @Test
    public void deleteOnlineShoppingItem_withLastModifiedDateBefore_fail() {
        // TODO
    }

    @Test
    public void upsertNonExistingOnlineShoppingItems_success() {

        // Given
        final var item = new OnlineShoppingItemDto()
                .onlineShopName(OnlineShopDto.GOOGLE_SHOPPING.name())
                .userId(USERID)
                .uuid("testUUID")
                .name("testItem");

        final var req = new CreateOnlineShoppingItemsRequest().addDataItem(item);

        // When
        ResponseEntity<Void> result = searchController.createOnlineShoppingItems(req);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(item, Objects.requireNonNull(
                searchController.getOnlineShoppingItem(item.getOnlineShopName(), item.getName()).getBody()
                ).getData()
        );
    }

    @Test
    public void upsertNonExistingOnlineShoppingItems_withNonEmptyDb_success() {

        // Given
        final var existingItem = new OnlineShoppingItemDto()
                .onlineShopName(OnlineShopDto.GOOGLE_SHOPPING.name())
                .userId(USERID)
                .uuid("testUUID")
                .name("testItem");

        final var nonExistingItem = new OnlineShoppingItemDto()
                .onlineShopName(OnlineShopDto.GOOGLE_SHOPPING.name())
                .userId(USERID)
                .uuid("testUUID123")
                .name("testItem123");

        final var req = new CreateOnlineShoppingItemsRequest()
                .addDataItem(existingItem)
                .addDataItem(nonExistingItem);
        ResponseEntity<Void> result = searchController.createOnlineShoppingItems(req);


        // When
        final var items = Objects.requireNonNull(
                searchController.getOnlineShoppingItems().getBody()
        ).getData();

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(2, items.size());
        assertTrue(items.contains(existingItem));
        assertTrue(items.contains(nonExistingItem));


    }

    @Test
    public void getOnlineShoppingItems_success() {

        // Given
        final var item1 = new OnlineShoppingItemDto()
                .onlineShopName(OnlineShopDto.GOOGLE_SHOPPING.name())
                .userId(USERID)
                .uuid("testUUID1")
                .name("testItem1");
        final var item2 = new OnlineShoppingItemDto()
                .onlineShopName(OnlineShopDto.GOOGLE_SHOPPING.name())
                .userId(USERID)
                .uuid("testUUID2")
                .name("testItem2");
        final var req = new CreateOnlineShoppingItemsRequest()
                .addDataItem(item1)
                .addDataItem(item2);

        ResponseEntity<Void> createResp = searchController.createOnlineShoppingItems(req);
        assertEquals(HttpStatus.OK, createResp.getStatusCode());


        // When
        final var resultList = Objects.requireNonNull(
                searchController.getOnlineShoppingItems().getBody()
        ).getData();

        // Then
        assertEquals(2, resultList.size());
        assertTrue(resultList.contains(item1));
        assertTrue(resultList.contains(item2));

    }

    @Test
    public void upsertExistingOnlineShoppingItems_success() {

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
        final var updateReq = new UpdateOnlineShoppingItemsRequest()
                .addDataItem(item1);

        // When
        final var result = Objects.requireNonNull(
                searchController.updateOnlineShoppingItems(updateReq)
        );

        final var updatedItems = Objects.requireNonNull(
                searchController
                        .getOnlineShoppingItems()
                        .getBody()
        ).getData();

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, updatedItems.size());
        assertEquals(item1, updatedItems.get(0));
    }

    @Bean
    public AmazonDynamoDB initializeAmazonDynamoDB() {

        log.debug("Creating dynamodb client");
        final AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder
                .standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(String.format("http://localhost:%d", LOCALSTACK.getMappedPort(8000)), "ap-southeast-2"))
                .build();
        log.debug("Created dynamodb client");


        createTable(amazonDynamoDB);
        return amazonDynamoDB;
    }

    private void createTable(final AmazonDynamoDB amazonDynamoDB) {
        log.debug("Creating table");
        final var mapper = new DynamoDBMapper(amazonDynamoDB, DynamoDBMapperConfig.DEFAULT);
        final var request =
                mapper
                        .generateCreateTableRequest(OnlineShoppingItem.class)
                        .withTableName(Constants.DYNAMO_TABLE_NAME_ONLINESHOPPINGITEMS.getStr())
                        .withProvisionedThroughput(new ProvisionedThroughput(1L, 1L));

        amazonDynamoDB.createTable(request);
        log.debug("Created table");
    }

}