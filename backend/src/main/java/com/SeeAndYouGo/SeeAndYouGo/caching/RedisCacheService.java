package com.SeeAndYouGo.SeeAndYouGo.caching;

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
    private final RedisTemplate<String, byte[]> byteRedisTemplate;
    private final StringRedisTemplate stringRedisTemplate;


    @Override
    public void putBytes(String cacheName, String key, byte[] data) {
        String fullKey = generateFullKey(cacheName, key);
        byteRedisTemplate.opsForValue().set(fullKey, data);
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
    public void putBytes(String cacheName, String key, byte[] data, CacheRule rule) {
        String fullKey = generateFullKey(cacheName, key);
        if (rule.getTtl() != null) {
            byteRedisTemplate.opsForValue().set(fullKey, data, rule.getTtl().toMillis(), TimeUnit.MILLISECONDS);
            logger.debug("Cached data with key: {} and TTL: {}", fullKey, rule.getTtl());
        } else {
            byteRedisTemplate.opsForValue().set(fullKey, data);
            logger.debug("Cached data with key: {} (no TTL)", fullKey);
        }
    }

    /**
     * 만료 시간이 있는 문자열 캐시 저장
     */
    public void putString(String cacheName, String key, String data, CacheRule rule) {
        String fullKey = generateFullKey(cacheName, key);
        if (rule.getTtl() != null) {
            stringRedisTemplate.opsForValue().set(fullKey, data, rule.getTtl().toMillis(), TimeUnit.MILLISECONDS);
            logger.debug("Cached string data with key: {} and TTL: {}", fullKey, rule.getTtl());
        } else {
            stringRedisTemplate.opsForValue().set(fullKey, data);
            logger.debug("Cached string data with key: {} (no TTL)", fullKey);
        }
    }

    @Override
    public Optional<byte[]> getBytesCache(String cacheName, String key) {

        String fullKey = generateFullKey(cacheName, key);
        byte[] data = byteRedisTemplate.opsForValue().get(fullKey);

        if (data != null && data.length > 0) {
            logger.debug("Cache hit for key: {}", fullKey);
            return Optional.of(data);
        } else {
            logger.debug("Cache miss for key: {}", fullKey);
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> getStringCache(String cacheName, String key) {
        String fullKey = generateFullKey(cacheName, key);
        String data = stringRedisTemplate.opsForValue().get(fullKey);

        if (data != null && !data.isEmpty()) {
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
        byteRedisTemplate.delete(fullKey);
        logger.debug("Evicted cache entry: {}", fullKey);
    }

    @Override
    public void evictAll(String cacheName) {
        String pattern = cacheName + ":*";
        byteRedisTemplate.delete(byteRedisTemplate.keys(pattern));
        logger.debug("Evicted all entries for cache: {}", cacheName);
    }

    private String generateFullKey(String cacheName, String key) {
        return cacheName + ":" + key;
    }
}
