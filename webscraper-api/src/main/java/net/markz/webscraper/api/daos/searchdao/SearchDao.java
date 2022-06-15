package net.markz.webscraper.api.daos.searchdao;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Repository
public class SearchDao {

    private final AmazonDynamoDB amazonDynamoDB;

  /**
   * On failure to upsert any of the onlineShoppingItems I will continue to do upsert the rest of the items.
   * The "lastModifiedDate" attribute is used to implement eventually consistent upsertion.
   *
   * The "version" attribute can be used to implement optimistic locking on the table.
   * @param onlineShoppingItems
   */
  public void upsertOnlineShoppingItems(final List<OnlineShoppingItem> onlineShoppingItems) {
      final var now = LocalDateTime.now();

      // Set last modified date.
      onlineShoppingItems.forEach(item -> item.setLastModifiedDate(now));

      // Set ttl
      onlineShoppingItems.forEach(item -> item.setTtl(99999999L));

      // batchSave creates items if they do not exist by checking its primary key.
      // It will update items if they do exist.
      new DynamoDBMapper(amazonDynamoDB).batchSave(onlineShoppingItems);
    }

    public List<OnlineShoppingItem> getOnlineShoppingItemsByUser(final String userId) {
        final var result = new DynamoDBMapper(amazonDynamoDB).query(
                OnlineShoppingItem.class,
                new DynamoDBQueryExpression<OnlineShoppingItem>()
                        .withConsistentRead(false)
                        .withExpressionAttributeValues(
                                Map.of(
                                        ":val1",
                                        new AttributeValue().withS(String.format("USERID#%s", userId))
                                ))
                        .withKeyConditionExpression("PK = :val1")
        );

        if(result == null) {
            return new ArrayList<>();
        }
        return result;
    }

    public Optional<OnlineShoppingItem> getOnlineShoppingItemByPrimaryKey(final OnlineShoppingItem onlineShoppingItem) {

        return Optional.ofNullable(
                new DynamoDBMapper(amazonDynamoDB).load(onlineShoppingItem)
        );
    }

    public void deleteOnlineShoppingItems(final List<OnlineShoppingItem> onlineShoppingItems) {
        new DynamoDBMapper(amazonDynamoDB).batchDelete(onlineShoppingItems);
    }

}
