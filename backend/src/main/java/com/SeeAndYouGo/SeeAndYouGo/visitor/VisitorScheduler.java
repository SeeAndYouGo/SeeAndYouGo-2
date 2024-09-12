package com.SeeAndYouGo.SeeAndYouGo.visitor;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class VisitorScheduler {

    private final RedisTemplate<String, String> redisTemplate;


    @Scheduled(cron = "0 0 0 * * *")
    public void resetVisitorCount() {

        // 방문여부 초기화
        redisTemplate.delete(Const.PREFIX_VISITOR_IP);
        redisTemplate.delete(Const.PREFIX_VISITOR_USER);

        // 방문자 수 초기화
        redisTemplate.delete(Const.KEY_TOTAL_VISITOR_COUNT);

        System.out.println("===== Redis Cache Reset =====");
    }
}