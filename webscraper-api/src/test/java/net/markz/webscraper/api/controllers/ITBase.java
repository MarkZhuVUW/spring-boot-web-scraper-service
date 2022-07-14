package net.markz.webscraper.api.controllers;

import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;

public class ITBase {
    // Dynamo
    private static final GenericContainer DYNAMO;

    static {

        DYNAMO = new GenericContainer("amazon/dynamodb-local:latest")
                .withCommand("-jar DynamoDBLocal.jar -inMemory -sharedDb")
                .withExposedPorts(8000)
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(
                        ITBase.class)));
        DYNAMO.start();
    }

    public static GenericContainer getDynamoContainer() {
        return DYNAMO;
    }

}
