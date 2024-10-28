package com.SeeAndYouGo.SeeAndYouGo.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Set;

@RequiredArgsConstructor
public class RedisScheduler {

    private final RedisTemplate<String, Object> redisTemplate;

    @Scheduled(cron = "0 0 0 * * *")
    public void resetKey() {
        Set<String> keys = redisTemplate.keys("menu:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}