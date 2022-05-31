package net.markz.webscraper.api.services;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.constants.Constants;
import net.markz.webscraper.api.exceptions.WebscraperException;
import net.markz.webscraper.api.parsers.DtoDataParser;
import net.markz.webscraper.api.sqs.EventType;
import net.markz.webscraper.api.sqs.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Calendar;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SQSProduceService {

    private final SearchService searchService;
    private final AmazonSQS amazonSQS;
    private final Environment env;

    /**
     * Get all current user's favourite online shopping items,
     * build them as messages and send them to an sqs queue for later consumption.
     */
    public void sqsProduce() {
        final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();

        final var queueName = env.getProperty("amazon.sqs.queue.name");
        final var queueUrl = env.getProperty("amazon.sqs.queue.url");
//        try {
//            CreateQueueResult create_result = sqs.createQueue(queueName);
//        } catch (AmazonSQSException e) {
//            if (!e.getErrorCode().equals("QueueAlreadyExists")) {
//                throw e;
//            }
//        }

        final var onlineShoppingItems = searchService.getOnlineShoppingItems();

        final var objectMapper = new JsonMapper();

        onlineShoppingItems
                .parallelStream() // Concurrently send messages to sqs queue as we follow an eventually consistent mechanism
                .map(DtoDataParser::parseDto)
                .forEach(item -> {
                    final var message = Message
                            .builder()
                            .lastModified(Calendar.getInstance())
                            .eventType(EventType.CRON_ITEM_UPDATE_AND_PRICE_DROP_ALERT.name())
                            .data(item)
                            .build();
                    try {
                        final var strMessage = objectMapper.writeValueAsString(
                                message
                        );
                        log.info("Sending stringified message={}", message);


                        final var sendMsgReq = new SendMessageRequest()
                                .withQueueUrl(queueUrl)
                                .withMessageBody(strMessage)
                                .withDelaySeconds(Integer.parseInt(Constants.LAMBDA_REPLAY_DELAY_SECONDS.getStr()));
                        sqs.sendMessage(sendMsgReq);
                        log.info("Sent message={}", message);
                    } catch (JsonProcessingException e) {
                        throw new WebscraperException(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Message=%s cannot be parsed to string", message));
                    }

                });


    }
}
