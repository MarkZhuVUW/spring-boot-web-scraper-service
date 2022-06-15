package net.markz.webscraper.api.configs;

import io.lettuce.core.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Initialize redis client talking to Elasticache or something else.
 * Currently I use redis only for distributed locking.
 */
@Configuration
@Slf4j
public class RedisClientInitializer {
    @Autowired
    private Environment env;

    @Bean
    public RedisClient redisClient() {
//        return RedisClient.create(env.getProperty("amazon.distributed-locking-service.endpoint"));
        return null;
    }
}