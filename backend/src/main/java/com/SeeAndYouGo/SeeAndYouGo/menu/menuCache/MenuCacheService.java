package com.SeeAndYouGo.SeeAndYouGo.menu.menuCache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 특정 레스토랑의 여러 캐시 키들을 무효화
     * @param cacheKeys 캐시 키 패턴들 (예: ["daily-menu", "weekly-menu"])
     * @param restaurantNumber 레스토랑 번호 (1~6)
     */
    public void clearMenuCache(String[] cacheKeys, String restaurantNumber) {
        List<String> deletedKeys = new ArrayList<>();
        
        for (String cacheKey : cacheKeys) {
            String fullCacheKey = cacheKey + ":" + restaurantNumber;
            Boolean deleted = redisTemplate.delete(fullCacheKey);
            
            if (Boolean.TRUE.equals(deleted)) {
                deletedKeys.add(fullCacheKey);
            }
        }
        
        if (!deletedKeys.isEmpty()) {
            log.info("Cleared menu cache keys: {} for restaurant: {}", deletedKeys, restaurantNumber);
        } else {
            log.debug("No cache found or failed to delete for restaurant: {}", restaurantNumber);
        }
    }

    /**
     * 모든 레스토랑의 여러 캐시 키들을 무효화 (1~6번)
     * @param cacheKeys 캐시 키 패턴들 (예: ["daily-menu", "weekly-menu"])
     */
    public void clearAllMenuCache(String[] cacheKeys) {
        for (int i = 1; i <= 6; i++) {
            clearMenuCache(cacheKeys, String.valueOf(i));
        }
        log.info("Cleared all menu caches for keys: {} (restaurants 1-6)", (Object) cacheKeys);
    }

    /**
     * 패턴으로 특정 캐시 키들을 모두 삭제
     * @param cacheKeyPattern 캐시 키 패턴 (예: "daily-menu", "weekly-menu")
     */
    public void clearAllMenuCacheByPattern(String cacheKeyPattern) {
        String pattern = cacheKeyPattern + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        
        if (keys != null && !keys.isEmpty()) {
            Long deletedCount = redisTemplate.delete(keys);
            log.info("Cleared {} menu cache keys matching pattern: {}", deletedCount, pattern);
        } else {
            log.debug("No menu cache keys found matching pattern: {}", pattern);
        }
    }

    /**
     * 특정 레스토랑의 특정 캐시 존재 여부 확인
     * @param cacheKey 캐시 키 패턴 (예: "daily-menu")
     * @param restaurantNumber 레스토랑 번호 (1~6)
     */
    public boolean hasMenuCache(String cacheKey, String restaurantNumber) {
        String fullCacheKey = cacheKey + ":" + restaurantNumber;
        return Boolean.TRUE.equals(redisTemplate.hasKey(fullCacheKey));
    }

    /**
     * 단일 캐시 키에 대한 특정 레스토랑 캐시 삭제
     * @param cacheKey 캐시 키 패턴 (예: "daily-menu")
     * @param restaurantNumber 레스토랑 번호 (1~6)
     */
    public void clearSingleMenuCache(String cacheKey, String restaurantNumber) {
        clearMenuCache(new String[]{cacheKey}, restaurantNumber);
    }
}