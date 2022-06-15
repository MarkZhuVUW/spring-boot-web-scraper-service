//package net.markz.webscraper.api.services;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.lettuce.core.RedisClient;
//import io.lettuce.core.RedisException;
//import io.lettuce.core.SetArgs;
//import io.lettuce.core.api.StatefulConnection;
//import io.lettuce.core.api.StatefulRedisConnection;
//import io.lettuce.core.api.sync.RedisCommands;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.PreDestroy;
//import java.time.Duration;
//import java.time.temporal.ChronoUnit;
//import java.util.Objects;
//
//@Slf4j
//@Service
//public class RedisService {
//    private final RedisClient redisClient;
//    private final StatefulConnection<String, String> connection;
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    public RedisService(
//            final ObjectMapper objectMapper,
//            final RedisClient redisClient
//    ) {
//
//        this.redisClient = redisClient;
//        this.objectMapper = objectMapper;
//        redisClient.setDefaultTimeout(Duration.of(3000, ChronoUnit.MILLIS));
//        this.connection = redisClient.connect();
//    }
//    private RedisCommands<String, String> getCommands() {
//        if(connection instanceof StatefulRedisConnection) {
//            final var commands = ((StatefulRedisConnection<String, String>) connection).sync();
//            commands.setTimeout(Duration.of(1500, ChronoUnit.MILLIS));
//            return commands;
//        }
//        throw new RedisException(String.format("Unrecognized connection type: %s", connection.getClass().getName()));
//
//    }
//    public boolean set(final String key, final String value, long timeout) {
//        return "OK".equals(getCommands().set(key, value, new SetArgs().nx().px(timeout)));
//    }
//
//    public boolean delete(final String key, final String value) {
//        if( StringUtils.equals(value, get(key))) {
//            return Objects.equals(getCommands().del(key), 1L);
//        }
//        return false;
//    }
//
//    public String get(final String key) {
//        return getCommands().get(key);
//    }
//
//    @PreDestroy
//    public void shutdown() {
//        connection.close();
//        redisClient.shutdown();
//    }
//
//}
