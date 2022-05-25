/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package net.markz.webscraper.api.configs;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceInUseException;
import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.constants.Constants;
import net.markz.webscraper.api.daos.searchdao.OnlineShoppingItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@Configuration
@Slf4j
public class DynamoDBConfiguration {

    @Value("${amazon.region}")
    private String region;

    @Value("${amazon.dynamodb.endpoint.url}")
    private String dynamoDBEndpoint;

    @Autowired
    private Environment env;


    @Bean
    public DynamoDBMapper dynamoDBMapper() {

        final boolean isLocal = Arrays.stream(env.getActiveProfiles()).toList().contains("local");

        log.debug("Creating dynamodb client");

        final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
//                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("foo", "bar")))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(dynamoDBEndpoint, region))
                .build();
        log.debug("Created dynamodb client");
        final var dynamoDBMapper = new DynamoDBMapper(client, DynamoDBMapperConfig.DEFAULT);

        if(isLocal) {
            // Create the table if we are in local environment.
            // In prod the table is provisioned through AWS CDK: https://github.com/MarkZhuVUW/aws-cdk-all/tree/master/src/main/java/net/markz/awscdkstack/services/webscraperservice

            log.debug("Creating table");
            try {
                createTable(dynamoDBMapper, client);

            } catch(ResourceInUseException e) {
                client.deleteTable(new DeleteTableRequest(Constants.DYNAMO_TABLE_NAME_ONLINESHOPPINGITEMS.getStr()));
                createTable(dynamoDBMapper, client);
            }
            log.debug("Created table");

        }


        return dynamoDBMapper;
    }

    private void createTable(final DynamoDBMapper mapper, final AmazonDynamoDB client) {

        final var request = mapper
                .generateCreateTableRequest(OnlineShoppingItem.class)
                .withProvisionedThroughput(new ProvisionedThroughput(1L, 1L));

        client.createTable(request);

    }
}
