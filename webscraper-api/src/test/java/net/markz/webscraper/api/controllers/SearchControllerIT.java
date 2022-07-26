package net.markz.webscraper.api.controllers;


import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.constants.Constants;
import net.markz.webscraper.api.exceptions.WebscraperException;
import net.markz.webscraper.model.CreateOnlineShoppingItemsRequest;
import net.markz.webscraper.model.DeleteOnlineShoppingItemsRequest;
import net.markz.webscraper.model.OnlineShopDto;
import net.markz.webscraper.model.OnlineShoppingItemDto;
import net.markz.webscraper.model.UpdateOnlineShoppingItemsRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Objects;

import static net.markz.webscraper.api.controllers.TestUtils.createTable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@ActiveProfiles("test")
@SpringBootTest
@Slf4j
@ExtendWith(MockitoExtension.class)
class SearchControllerIT extends ITBase {

    @Autowired
    private SearchController searchController;

    @Autowired
    private AmazonDynamoDB amazonDynamoDB;

    private static final String USERID = "markz";

    @BeforeEach
    void afterEach() {
        amazonDynamoDB.deleteTable(Constants.DYNAMO_TABLE_NAME_ONLINESHOPPINGITEMS.getStr());
        createTable(amazonDynamoDB);
    }

    @Test
    void deleteExistingOnlineShoppingItem_success() {

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
        assertNotEquals(item1.getLastModifiedDate(), items.get(0).getLastModifiedDate());
        assertEquals(item1.getOnlineShopName(), items.get(0).getOnlineShopName());
        assertEquals(item1.getName(), items.get(0).getName());
        assertEquals(item1.getUserId(), items.get(0).getUserId());
        assertEquals(item1.getUuid(), items.get(0).getUuid());

        item1.setSalePrice("123");
        item1.setHref("123");

        final var req = new DeleteOnlineShoppingItemsRequest()
                .addDataItem(item1);

        // When
        final var result = Objects.requireNonNull(
                searchController.deleteOnlineShoppingItems(req)
        );

        final var deletedItems = Objects.requireNonNull(
                searchController
                        .getOnlineShoppingItems()
                        .getBody()
        ).getData();

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(0, deletedItems.size());

    }


    // By design batchDelete in dynamodb is idempotent. Running the same api multiple times does not generate error responses.
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
                () -> searchController.getOnlineShoppingItem(item.getOnlineShopName(), item.getName())
        );

    }

    @Test
    void upsertNonExistingOnlineShoppingItems_success() {

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

        final var dbItem = Objects.requireNonNull(searchController.getOnlineShoppingItem(item.getOnlineShopName(), item.getName()).getBody()).getData();

        assertNotEquals(item.getLastModifiedDate(), dbItem.getLastModifiedDate());
        assertEquals(item.getOnlineShopName(), dbItem.getOnlineShopName());
        assertEquals(item.getName(), dbItem.getName());
        assertEquals(item.getUserId(), dbItem.getUserId());
        assertEquals(item.getUuid(), dbItem.getUuid());

    }

    @Test
    void upsertNonExistingOnlineShoppingItems_withNonEmptyDb_success() {

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

        assertNotEquals(existingItem.getLastModifiedDate(), items.get(0).getLastModifiedDate());
        assertEquals(existingItem.getOnlineShopName(), items.get(0).getOnlineShopName());
        assertEquals(existingItem.getName(), items.get(0).getName());
        assertEquals(existingItem.getUserId(), items.get(0).getUserId());
        assertEquals(existingItem.getUuid(), items.get(0).getUuid());

        assertNotEquals(nonExistingItem.getLastModifiedDate(), items.get(1).getLastModifiedDate());
        assertEquals(nonExistingItem.getOnlineShopName(), items.get(1).getOnlineShopName());
        assertEquals(nonExistingItem.getName(), items.get(1).getName());
        assertEquals(nonExistingItem.getUserId(), items.get(1).getUserId());
        assertEquals(nonExistingItem.getUuid(), items.get(1).getUuid());

    }

    @Test
    void getOnlineShoppingItems_success() {

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
        assertNotEquals(resultList.get(0).getLastModifiedDate(), item1.getLastModifiedDate());
        assertEquals(resultList.get(0).getOnlineShopName(), item1.getOnlineShopName());
        assertEquals(resultList.get(0).getName(), item1.getName());
        assertEquals(resultList.get(0).getUserId(), item1.getUserId());
        assertEquals(resultList.get(0).getUuid(), item1.getUuid());

        assertNotEquals(resultList.get(1).getLastModifiedDate(), item2.getLastModifiedDate());
        assertEquals(resultList.get(1).getOnlineShopName(), item2.getOnlineShopName());
        assertEquals(resultList.get(1).getName(), item2.getName());
        assertEquals(resultList.get(1).getUserId(), item2.getUserId());
        assertEquals(resultList.get(1).getUuid(), item2.getUuid());

    }

    @Test
    void upsertExistingOnlineShoppingItems_success() {

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
        assertNotEquals(item1.getLastModifiedDate(), items.get(0).getLastModifiedDate());
        assertEquals(item1.getOnlineShopName(), items.get(0).getOnlineShopName());
        assertEquals(item1.getName(), items.get(0).getName());
        assertEquals(item1.getUserId(), items.get(0).getUserId());
        assertEquals(item1.getUuid(), items.get(0).getUuid());

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
        assertNotEquals(item1.getLastModifiedDate(), updatedItems.get(0).getLastModifiedDate());
        assertEquals(item1.getOnlineShopName(), updatedItems.get(0).getOnlineShopName());
        assertEquals(item1.getName(), updatedItems.get(0).getName());
        assertEquals(item1.getUserId(), updatedItems.get(0).getUserId());
        assertEquals(item1.getUuid(), updatedItems.get(0).getUuid());
    }



}