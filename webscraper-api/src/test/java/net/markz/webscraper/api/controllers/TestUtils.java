package net.markz.webscraper.api.controllers;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import net.markz.webscraper.api.constants.Constants;
import net.markz.webscraper.api.daos.searchdao.OnlineShoppingItem;

public class TestUtils {
    public static void createTable(final AmazonDynamoDB amazonDynamoDB) {
        final var mapper = new DynamoDBMapper(amazonDynamoDB, DynamoDBMapperConfig.DEFAULT);
        final var request =
                mapper
                        .generateCreateTableRequest(OnlineShoppingItem.class)
                        .withTableName(Constants.DYNAMO_TABLE_NAME_ONLINESHOPPINGITEMS.getStr())
                        .withProvisionedThroughput(new ProvisionedThroughput(1L, 1L));

        amazonDynamoDB.createTable(request);
    }

    public static CreateQueueResult createQueue(final AmazonSQS amazonSQS) {
        final var req = new CreateQueueRequest()
                .withQueueName("test");
        return amazonSQS.createQueue(req);
    }

}
