package net.markz.webscraper.api.consumers;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import lombok.AllArgsConstructor;
import net.markz.webscraper.api.constants.Constants;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

/**
 * Defines what happens when messages failed processing.
 */
@AllArgsConstructor
@Component
public abstract class AbstractEventErrorHandler {


    private final String sqsQueueUrl;
    private final AmazonSQS amazonSQS;
    private final String deadLetterQueueUrl;

    public abstract Logger getLogger();

    public final void replayMessage(SQSEvent.SQSMessage message) {
        if(!shouldReplay(message)) {
            getLogger().info("Message will not be replayed.");
            sendToDLQ(message);
            return;
        }
        sendToQueue(message);

    }

    private boolean shouldReplay(SQSEvent.SQSMessage message) {

        final var replayTimes = getReplayTimes(message);

        return replayTimes < Integer.parseInt(Constants.LAMBDA_REPLAY_TIMES.getStr());

    }

    private int getReplayTimes(SQSEvent.SQSMessage message) {
        final var replayTimesAttr = message
                .getMessageAttributes()
                .get(Constants.LAMBDA_REPLAY_TIMES_ATTRIBUTE.getStr());

        if(replayTimesAttr == null) {
            return 0;
        }

        return Integer.parseInt(replayTimesAttr.getStringValue());
    }

    private void sendToDLQ(SQSEvent.SQSMessage message) {
        final var req = new SendMessageRequest()
                .withQueueUrl(this.deadLetterQueueUrl)
                .withMessageBody(message.getBody());
        amazonSQS.sendMessage(req);
        getLogger().info("Message={} is sent to DLQ.", message);
    }

    private void sendToQueue(SQSEvent.SQSMessage message) {
        final var replayTimes = getReplayTimes(message);
        getLogger().info("Sending message={} back to queue. Current replay times={}", message, replayTimes);

    final var req =
        new SendMessageRequest()
            .withQueueUrl(this.sqsQueueUrl)
            .withMessageBody(message.getBody())
            .withDelaySeconds(Integer.parseInt(Constants.LAMBDA_REPLAY_DELAY_SECONDS.getStr()))
            .addMessageAttributesEntry(
                    Constants.LAMBDA_REPLAY_TIMES_ATTRIBUTE.getStr(),
                    new MessageAttributeValue()
                            .withStringValue(String.valueOf(replayTimes + 1))
                            .withDataType("Number")
            );
        amazonSQS.sendMessage(req);
        getLogger().info("Message={} is sent to the queue and will be replayed.", message);
    }
}
