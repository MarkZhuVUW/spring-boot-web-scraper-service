package net.markz.webscraper.api.controllers;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.constants.Constants;
import net.markz.webscraper.model.CreateOnlineShoppingItemsRequest;
import net.markz.webscraper.model.OnlineShopDto;
import net.markz.webscraper.model.OnlineShoppingItemDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static net.markz.webscraper.api.controllers.TestUtils.createQueue;
import static net.markz.webscraper.api.controllers.TestUtils.createTable;
import static net.markz.webscraper.api.controllers.TestUtils.emptyQueue;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
@Slf4j
class SQSControllerIT extends ITBase {

    @Autowired
    private SQSController sqsController;

    private SearchController searchController;

    @Autowired
    private AmazonDynamoDB amazonDynamoDB;

    @Autowired
    private AmazonSQS amazonSQS;

    private static String queueUrl;

    private static String dlqUrl;

    private static final String USERID = "markz";

    @BeforeEach
    void beforeEach() {
        createQueue(amazonSQS);

        queueUrl = System.getProperty("amazon.sqs.queue.url");
        dlqUrl = System.getProperty("amazon.sqs.dlq.url");

        await().atMost(15, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).untilAsserted(() -> {
            assertTrue(emptyQueue(amazonSQS, queueUrl));
            assertTrue(emptyQueue(amazonSQS, dlqUrl));
        });

    }

    @BeforeEach
    void afterEach() {
        amazonDynamoDB.deleteTable(Constants.DYNAMO_TABLE_NAME_ONLINESHOPPINGITEMS.getStr());
        createTable(amazonDynamoDB);
    }


    @Test
    void poll_updateOnlineShoppingItems_success() throws InterruptedException {
        // Given

        final var item1 = new OnlineShoppingItemDto()
                .onlineShopName(OnlineShopDto.GOOGLE_SHOPPING.name())
                .userId(USERID)
                .onlineShop(OnlineShopDto.GOOGLE_SHOPPING)
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
        assertEquals(item1.getUuid(), items.get(0).getUuid());
        assertEquals(item1.getName(), items.get(0).getName());

        item1.setSalePrice("123");
        item1.setHref("123");

        Thread.sleep(100L);

        // The created item will be parsed to message and pushed to SQS
        sqsController.sqsProduce();


//        when(seleniumDriverService.)
        // when
        sqsController.sqsPoll();

        // then
        await().atMost(15, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).untilAsserted(() -> {
            assertEquals(0, amazonSQS.receiveMessage(new ReceiveMessageRequest().withQueueUrl(queueUrl)).getMessages().size());

            final var dbItem = Objects.requireNonNull(searchController.getOnlineShoppingItem(item1.getOnlineShopName(), item1.getName()).getBody()).getData();
            assertNotEquals(item1.getLastModifiedDate(), dbItem.getLastModifiedDate());
            assertEquals(item1.getOnlineShopName(), dbItem.getOnlineShopName());
            assertEquals(item1.getName(), dbItem.getName());
            assertEquals(item1.getUserId(), dbItem.getUserId());
            assertEquals(item1.getUuid(), dbItem.getUuid());

        });
    }


    @Test
    void poll_updateOnlineShoppingItems_lastModifiedDateBefore_fail() throws InterruptedException {
        // Given

        final var item1 = new OnlineShoppingItemDto()
                .onlineShopName(OnlineShopDto.GOOGLE_SHOPPING.name())
                .userId(USERID)
                .onlineShop(OnlineShopDto.GOOGLE_SHOPPING)
                .uuid("testUUID1")
                .lastModifiedDate(LocalDateTime.of(1000, 1, 1, 1, 1, 1).toString())
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
        assertEquals(item1.getUuid(), items.get(0).getUuid());
        assertEquals(item1.getName(), items.get(0).getName());

        item1.setSalePrice("123");
        item1.setHref("123");


        // The created item will be parsed to message and pushed to SQS
        sqsController.sqsProduce();

        Thread.sleep(100L);

        // when
        sqsController.sqsPoll();

        // then
        await().atMost(15, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).untilAsserted(() -> {
            assertEquals(0, amazonSQS.receiveMessage(new ReceiveMessageRequest().withQueueUrl(queueUrl)).getMessages().size());

            final var dbItem = Objects.requireNonNull(searchController.getOnlineShoppingItem(item1.getOnlineShopName(), item1.getName()).getBody()).getData();
            assertNotEquals(item1.getLastModifiedDate(), dbItem.getLastModifiedDate());
            assertEquals(item1.getOnlineShopName(), dbItem.getOnlineShopName());
            assertEquals(item1.getName(), dbItem.getName());
            assertEquals(item1.getUserId(), dbItem.getUserId());
            assertEquals(item1.getUuid(), dbItem.getUuid());

        });
    }

}
