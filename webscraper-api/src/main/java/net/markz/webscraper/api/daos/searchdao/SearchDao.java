package net.markz.webscraper.api.daos.searchdao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Repository
public class SearchDao {

    private final DynamoDBMapper dynamoDBMapper;

    public void upsertOnlineShoppingItems(final List<OnlineShoppingItem> onlineShoppingItems) {

        final var now = LocalDate.now();

        // Set last modified date.
        onlineShoppingItems.forEach(item -> item.setLastModifiedDate(now));

        DynamoDBMapperConfig config = DynamoDBMapperConfig.builder()
                .build();
        dynamoDBMapper.batchWrite(onlineShoppingItems, List.of(), config);
    }

    public List<OnlineShoppingItem> getOnlineShoppingItemsByUser(final String userId) {
        final var result = dynamoDBMapper.query(
                OnlineShoppingItem.class,
                new DynamoDBQueryExpression<OnlineShoppingItem>()
                        // reads from GSI can't be consistent
                        .withConsistentRead(false)
                        .withExpressionAttributeValues(
                                Map.of(
                                        ":val1",
                                        new AttributeValue().withS(String.format("ITEM#%s", userId))
                                ))
//                        .withIndexName("GSI_1")
                        .withKeyConditionExpression("PK = :val1")
        );

        if(result == null) {
            return new ArrayList<>();
        }
        return result;
    }

    public void deleteOnlineShoppingItems(final List<OnlineShoppingItem> onlineShoppingItems) {
        dynamoDBMapper.batchDelete(onlineShoppingItems);
    }
}
