package net.markz.webscraper.api.controllers;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.constants.Constants;
import net.markz.webscraper.api.exceptions.WebscraperException;
import net.markz.webscraper.model.CreateOnlineShoppingItemsRequest;
import net.markz.webscraper.model.DeleteOnlineShoppingItemsRequest;
import net.markz.webscraper.model.OnlineShopDto;
import net.markz.webscraper.model.OnlineShoppingItemDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static net.markz.webscraper.api.controllers.TestUtils.createQueue;
import static net.markz.webscraper.api.controllers.TestUtils.createTable;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@SpringBootTest
@Slf4j
class SQSControllerIT extends ITBase {

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

    @BeforeEach
    void beforeEach() {
        queueUrl = createQueue(amazonSQS).getQueueUrl();
        createTable(amazonDynamoDB);
    }
    @AfterEach
    void afterEach() {

        amazonDynamoDB.deleteTable(Constants.DYNAMO_TABLE_NAME_ONLINESHOPPINGITEMS.getStr());
        amazonSQS.deleteQueue(new DeleteQueueRequest().withQueueUrl(queueUrl));

    }

    @Test
    void poll_messageLastModifiedDate_beforeExistingItem_fail() {
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


        // The created item will be parsed to message and pushed to SQS
        sqsController.sqsProduce();

        // when
        sqsController.sqsPoll();

        // then
        await().atMost(60, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).untilAsserted(() -> {
            assertEquals(0, amazonSQS.receiveMessage(new ReceiveMessageRequest().withQueueUrl(queueUrl)).getMessages().size());
        });

    }


    // By design batchDelete in dynamodb is idempotent. Running the same api multiple times does
    // not generate error responses.
    @Test
    void deleteNonExistingOnlineShoppingItem_success() {

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
                () -> searchController.getOnlineShoppingItem(item.getOnlineShopName(),
                        item.getName())
        );

    }

}
