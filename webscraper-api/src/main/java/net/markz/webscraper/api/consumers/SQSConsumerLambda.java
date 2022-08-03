package net.markz.webscraper.api.consumers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import net.markz.webscraper.api.configs.SpringContextManager;

public class SQSConsumerLambda implements RequestHandler<SQSEvent, Void> {


    @Override
    public Void handleRequest(final SQSEvent event, final Context context) {

        SpringContextManager.startSpringContext();
        final var eventProcessor = SpringContextManager.getBean(WebscraperEventProcessor.class);
        eventProcessor.processEvent(event);
        return null;
    }


}
