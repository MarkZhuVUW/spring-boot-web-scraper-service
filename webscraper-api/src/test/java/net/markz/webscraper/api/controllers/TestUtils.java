package net.markz.webscraper.api.controllers;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import net.markz.webscraper.api.constants.Constants;
import net.markz.webscraper.api.daos.searchdao.OnlineShoppingItem;

import java.util.stream.Collectors;

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

    public static void createQueue(final AmazonSQS amazonSQS) {

        final var result =  amazonSQS.createQueue(
                new CreateQueueRequest().withQueueName("test")
        );
        final var dlqResult =  amazonSQS.createQueue(
                new CreateQueueRequest().withQueueName("test-dlq")
        );

        System.setProperty("amazon.sqs.queue.url", result.getQueueUrl());
        System.setProperty("amazon.sqs.dlq.url", dlqResult.getQueueUrl());

    }

    public static boolean emptyQueue(final AmazonSQS amazonSQS, final String queueUrl) {

        final var outliers = amazonSQS
                .receiveMessage(new ReceiveMessageRequest().withQueueUrl(queueUrl))
                .getMessages()
                .stream()
                .map(message -> new DeleteMessageBatchRequestEntry()
                        .withReceiptHandle(message.getReceiptHandle())
                        .withId(message.getMessageId())
                )
                .collect(Collectors.toList());

        if(outliers.isEmpty()) {
            return true;
        }

        final var req = new DeleteMessageBatchRequest()
                .withQueueUrl(queueUrl)
                .withEntries(outliers);

        return amazonSQS.deleteMessageBatch(req).getFailed().isEmpty();
    }
}
