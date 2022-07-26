package net.markz.webscraper.api.controllers;

import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;

public class ITBase {
    // Dynamo
    private static final GenericContainer DYNAMO;

    private static final GenericContainer SELENIUM;

    static {

        DYNAMO = new GenericContainer("amazon/dynamodb-local:latest")
                .withCommand("-jar DynamoDBLocal.jar -inMemory -sharedDb")
                .withExposedPorts(8000)
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(
                        ITBase.class)));


        DYNAMO.start();

        SELENIUM = new GenericContainer("selenium/standalone-chrome:latest")
                .withExposedPorts(4444)
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(
                        ITBase.class)));

        SELENIUM.start();
        System.setProperty("selenium.url", "http://localhost:" + SELENIUM.getMappedPort(4444) + "/wd/hub");
    }


    public static GenericContainer getDynamoContainer() {
        return DYNAMO;
    }

}
