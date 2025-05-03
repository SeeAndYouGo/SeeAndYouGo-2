package com.SeeAndYouGo.SeeAndYouGo.caching;

import com.SeeAndYouGo.SeeAndYouGo.caching.CacheRule;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisCacheService implements CacheService {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheService.class);
    private final RedisTemplate<String, byte[]> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;


    @Override
    public void putBytes(String cacheName, String key, byte[] data) {
        String fullKey = generateFullKey(cacheName, key);
        redisTemplate.opsForValue().set(fullKey, data);
        logger.debug("Cached data with key: {}", fullKey);
    }

    @Override
    public void putString(String cacheName, String key, String data) {
        String fullKey = generateFullKey(cacheName, key);
        stringRedisTemplate.opsForValue().set(fullKey, data);
        logger.debug("Cached data with key: {}", fullKey);
    }


    /**
     * 만료 시간이 있는 캐시 저장
     */
    public void put(String cacheName, String key, byte[] data, CacheRule rule) {
        String fullKey = generateFullKey(cacheName, key);
        if (rule.getTtl() != null) {
            redisTemplate.opsForValue().set(fullKey, data, rule.getTtl().toMillis(), TimeUnit.MILLISECONDS);
            logger.debug("Cached data with key: {} and TTL: {}", fullKey, rule.getTtl());
        } else {
            redisTemplate.opsForValue().set(fullKey, data);
            logger.debug("Cached data with key: {} (no TTL)", fullKey);
        }
    }

    @Override
    public Optional<byte[]> get(String cacheName, String key) {
        String fullKey = generateFullKey(cacheName, key);
        byte[] data = redisTemplate.opsForValue().get(fullKey);

        if (data != null && data.length > 0) {
            logger.debug("Cache hit for key: {}", fullKey);
            return Optional.of(data);
        } else {
            logger.debug("Cache miss for key: {}", fullKey);
            return Optional.empty();
        }
    }

    @Override
    public void evict(String cacheName, String key) {
        String fullKey = generateFullKey(cacheName, key);
        redisTemplate.delete(fullKey);
        logger.debug("Evicted cache entry: {}", fullKey);
    }

    @Override
    public void evictAll(String cacheName) {
        String pattern = cacheName + ":*";
        redisTemplate.delete(redisTemplate.keys(pattern));
        logger.debug("Evicted all entries for cache: {}", cacheName);
    }

    private String generateFullKey(String cacheName, String key) {
        return cacheName + ":" + key;
    }
}
