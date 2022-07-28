package net.markz.webscraper.api.controllers;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import net.markz.webscraper.api.constants.Constants;
import net.markz.webscraper.api.daos.searchdao.OnlineShoppingItem;
import net.markz.webscraper.api.exceptions.WebscraperException;
import net.markz.webscraper.api.sqs.EventType;
import net.markz.webscraper.api.sqs.Message;
import net.markz.webscraper.model.OnlineShoppingItemDto;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
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

    public static void produceMessageFromDto(
            final List<OnlineShoppingItemDto> onlineShoppingItemDtos,
            final AmazonSQS amazonSQS,
            final String queueUrl
    ) {

        final var objectMapper = new JsonMapper();

        final var now = LocalDateTime.now();

        onlineShoppingItemDtos
                .forEach(
                        dto -> {
                            // update last modified date
                            final var message = new Message<OnlineShoppingItemDto>();
                            message.setEventType(EventType.CRON_ITEM_UPDATE_AND_PRICE_CHANGE_ALERT.name());
                            message.setData(List.of(dto));
                            message.setMsgId(UUID.randomUUID());

                            try {
                                final var strMessage = objectMapper.writeValueAsString(message);

                                final var sendMsgReq =
                                        new SendMessageRequest()
                                                .withQueueUrl(queueUrl)
                                                .withMessageBody(strMessage)
                                                .addMessageAttributesEntry(
                                                        Constants.LAMBDA_REPLAY_TIMES_ATTRIBUTE.getStr(),
                                                        new MessageAttributeValue().withStringValue("0").withDataType("Number")
                                                )
                                                .withDelaySeconds(
                                                        Integer.parseInt(Constants.LAMBDA_REPLAY_DELAY_SECONDS.getStr()));
                                amazonSQS.sendMessage(sendMsgReq);
                            } catch (JsonProcessingException e) {
                                throw new WebscraperException(
                                        HttpStatus.INTERNAL_SERVER_ERROR,
                                        String.format("Message=%s cannot be parsed to string", message));
                            }
                        });
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
