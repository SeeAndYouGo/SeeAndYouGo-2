package com.SeeAndYouGo.SeeAndYouGo.caching.aspect;

import com.SeeAndYouGo.SeeAndYouGo.caching.CacheEvictionService;
import com.SeeAndYouGo.SeeAndYouGo.caching.annotation.EvictAllCache;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class CacheEvictAspect {

    private final CacheEvictionService cacheEvictionService;

    @AfterReturning("@annotation(evictAllCache)")
    public void evictAllCaches(EvictAllCache evictAllCache) {
        for (String cacheName : evictAllCache.value()) {
            cacheEvictionService.evictAllCache(cacheName);
        }
    }
}