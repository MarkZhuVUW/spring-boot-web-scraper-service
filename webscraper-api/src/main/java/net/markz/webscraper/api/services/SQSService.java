package net.markz.webscraper.api.services;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.constants.Constants;
import net.markz.webscraper.api.consumers.WebscraperEventProcessor;
import net.markz.webscraper.api.exceptions.WebscraperException;
import net.markz.webscraper.api.parsers.DtoDataParser;
import net.markz.webscraper.api.sqs.EventType;
import net.markz.webscraper.api.sqs.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SQSService {

    private final SearchService searchService;
    private final AmazonSQS amazonSQS;
    private final Environment env;
    private final WebscraperEventProcessor webscraperEventProcessor;

    /**
     * Get all current user's favourite online shopping items,
     * build them as messages and send them to an sqs queue for later consumption.
     */
    public void sqsProduce() {
        final var queueUrl = env.getProperty("amazon.sqs.queue.url");

        final var onlineShoppingItems = searchService.getOnlineShoppingItems();

        final var objectMapper = new JsonMapper();

        onlineShoppingItems
                .parallelStream() // Concurrently send messages to sqs queue as we follow an eventually
                // consistent mechanism
                .map(DtoDataParser::parseDto)
                .forEach(
                        item -> {
                            final var message =
                                    Message.builder()
                                            .lastModified(LocalDateTime.now())
                                            .eventType(EventType.CRON_ITEM_UPDATE_AND_PRICE_CHANGE_ALERT.name())
                                            .data(item)
                                            .build();
                            try {
                                final var strMessage = objectMapper.writeValueAsString(message);
                                log.info("Sending stringified message={}", message);

                                final var sendMsgReq =
                                        new SendMessageRequest()
                                                .withQueueUrl(queueUrl)
                                                .withMessageBody(strMessage)
                                                .withDelaySeconds(
                                                        Integer.parseInt(Constants.LAMBDA_REPLAY_DELAY_SECONDS.getStr()));
                                amazonSQS.sendMessage(sendMsgReq);
                                log.info("Sent message={}", message);
                            } catch (JsonProcessingException e) {
                                throw new WebscraperException(
                                        HttpStatus.INTERNAL_SERVER_ERROR,
                                        String.format("Message=%s cannot be parsed to string", message));
                            }
                        });
    }

    /**
     * Long poll from SQS and process the messages.
     * TODO: Refactor this to lambda function
     */
    public void sqsLongPoll() {
        final var queueUrl = env.getProperty("amazon.sqs.queue.url");

        // receive messages from the queue
        final var messages = amazonSQS.receiveMessage(queueUrl).getMessages();

        // Hardcode it to lambda SQSMessage in order to use the lambda processor.
        final var lambdaSQSMessages = messages
                .stream()
                .map(msg -> {
                    final var lambdaMsg = new SQSEvent.SQSMessage();
                    lambdaMsg.setAttributes(msg.getAttributes());
                    lambdaMsg.setMessageId(msg.getMessageId());
                    lambdaMsg.setBody(msg.getBody());
                    lambdaMsg.setReceiptHandle(msg.getReceiptHandle());
                    return lambdaMsg;
                })
                .toList();
        final var event = new SQSEvent();
        event.setRecords(lambdaSQSMessages);


        webscraperEventProcessor.processEvent(event);

    }
}
