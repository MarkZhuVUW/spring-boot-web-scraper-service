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

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
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

    @Value("${amazon.secretsmanager.secretName}")
    private String  secretName;

    @Value("${amazon.secretsmanager.endpoint}")
    private String  endpoint;

    @Value("${amazon.secretsmanager.region}")
    private String  region;

    @Autowired
    private Environment env;


    @Bean
    public DynamoDBMapper dynamoDBMapper() {

        final boolean isLocal = Arrays.stream(env.getActiveProfiles()).toList().contains("local");
        if(isLocal) {

            final AmazonDynamoDB localClient = AmazonDynamoDBClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("foo", "bar")))
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                    .build();
            final var dynamoDBMapper = new DynamoDBMapper(localClient, DynamoDBMapperConfig.DEFAULT);

            // Create the table if we are in local environment.
            // In prod the table is provisioned through AWS CDK: https://github.com/MarkZhuVUW/aws-cdk-all/tree/master/src/main/java/net/markz/awscdkstack/services/webscraperservice

            log.debug("Creating table");
            try {
                createTable(dynamoDBMapper, localClient);

            } catch(ResourceInUseException e) {
                localClient.deleteTable(new DeleteTableRequest(Constants.DYNAMO_TABLE_NAME_ONLINESHOPPINGITEMS.getStr()));
                createTable(dynamoDBMapper, localClient);
            }
            log.debug("Created table");
            return dynamoDBMapper;

        }

        log.debug("Creating dynamodb client");
        final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
        log.debug("Created dynamodb client");

        return new DynamoDBMapper(client, DynamoDBMapperConfig.DEFAULT);
    }

//    private Map<String, String> getCredentialsFromSSM() {
//        final var  config  =  new  AwsClientBuilder.EndpointConfiguration(endpoint, region);
//        final var clientBuilder  =  AWSSecretsManagerClientBuilder.standard();
//        clientBuilder.setEndpointConfiguration(config);
//        final var client  =  clientBuilder.build();
//
//        final var objectMapper  =  new ObjectMapper();
//
//        JsonNode secretsJson  =  null;
//
//        ByteBuffer binarySecretData;
//
//        final var getSecretValueRequest  =  new GetSecretValueRequest().withSecretId(secretName);
//
//        GetSecretValueResult getSecretValueResponse  =  null;
//
//        try  {
//            getSecretValueResponse  =  client.getSecretValue(getSecretValueRequest);
//        }
//
//        catch  (ResourceNotFoundException e)  {
//            log.error("The requested secret "  +  secretName  +  " was not found");
//        }
//
//        catch  (InvalidRequestException e)  {
//            log.error("The request was invalid due to: "  +  e.getMessage());
//        }
//
//        catch  (InvalidParameterException e)  {
//            log.error("The request had invalid params: "  +  e.getMessage());
//        }
//        if  (getSecretValueResponse  ==  null)  {
//            return  null;
//        }  // Decrypted secret using the associated KMS key // Depending on whether the secret was a string or binary, one of these fields will be populated
//
//
//        String secret = getSecretValueResponse.getSecretString();
//
//        if (secret != null) {
//            try {
//                secretsJson  =  objectMapper.readTree(secret);
//            }
//
//            catch  (IOException e)  {
//                log.error("Exception while retrieving secret values: "  +  e.getMessage());
//            }
//        }
//
//        else  {
//            log.error("The Secret String returned is null");
//
//            return null;
//
//        }
//        String  host  =  secretsJson.get("host").textValue();
//        String  port  =  secretsJson.get("port").textValue();
//        String  dbname  =  secretsJson.get("dbname").textValue();
//        String  username  =  secretsJson.get("username").textValue();
//        String  password  =  secretsJson.get("password").textValue();
//    }

    private void createTable(final DynamoDBMapper mapper, final AmazonDynamoDB client) {

        final var request = mapper
                .generateCreateTableRequest(OnlineShoppingItem.class)
                .withProvisionedThroughput(new ProvisionedThroughput(1L, 1L));

        client.createTable(request);

    }
}
