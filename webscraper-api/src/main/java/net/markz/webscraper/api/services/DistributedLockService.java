package net.markz.webscraper.api.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class DistributedLockService {
//    private final RedisService redisService;

    public String tryLock(final String key, final long timeout) {
        log.info("Trying to acquire lock on key={}", key);

        final var value = RandomStringUtils.random(10);
//        if(!redisService.set(key, value, timeout)) {
//            log.error("Failed to release lock. Key={}, value={}, timeout={}", key, value, timeout);
//            throw new WebscraperException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to acquire lock, maybe retry it later.");
//        }
        log.info("Acquired lock on key={}", key);
        return value;
    }

    public void release(final String key, final String value) {
//        final var result = redisService.delete(key, value);
//        log.info("Attempted releasing lock. Key={}, value={}, result={}", key, value, result);
    }
}
