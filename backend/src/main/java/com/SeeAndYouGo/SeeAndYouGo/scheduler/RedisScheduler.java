package com.SeeAndYouGo.SeeAndYouGo.scheduler;

import com.SeeAndYouGo.SeeAndYouGo.menu.mainCache.NewDishCacheService;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final NewDishCacheService newDishCacheService;

    @Scheduled(cron = "0 0 0 * * *")
    public void resetKey() {
        Set<String> keys = redisTemplate.keys("menu:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 매일 새벽 0시 5분에 모든 레스토랑의 historical 캐시 갱신
     * 어제 날짜의 메인메뉴들을 캐시에 추가
     *
     * Note: resetKey()보다 5분 늦게 실행되어 실행 순서 보장
     */
    @Scheduled(cron = "0 5 0 * * *")
    public void syncHistoricalDishes() {
        log.info("Starting historical dishes sync for all restaurants");

        for (Restaurant restaurant : Restaurant.values()) {
            try {
                newDishCacheService.syncHistoricalDishes(restaurant);
            } catch (Exception e) {
                log.error("Failed to sync historical dishes for restaurant: {}", restaurant, e);
            }
        }

        log.info("Completed historical dishes sync for all restaurants");
    }
}