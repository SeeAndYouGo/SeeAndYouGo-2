package com.SeeAndYouGo.SeeAndYouGo.caching;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CacheEvictionService {

    private final CacheService cacheService;
    private final List<CacheRule> cacheRules;

    /**
     * 특정 URL과 메서드에 해당하는 캐시 항목 무효화
     */
    public void evictCache(String url, String method, Map<String, String> pathVariables) {
        cacheRules.stream()
                .filter(rule -> rule.matches(url, method))
                .findFirst()
                .ifPresent(rule -> {
                    // 캐시 컨텍스트 생성
                    CacheContext context = CacheContext.builder()
                            .url(url)
                            .method(method)
                            .pathVariables(pathVariables)
                            .build();

                    // 캐시 키 생성
                    String cacheKey = rule.generateKey(context);

                    // 캐시 항목 삭제
                    cacheService.evict(rule.getCacheName(), cacheKey);
                });
    }

    /**
     * 특정 캐시의 모든 항목 무효화
     */
    public void evictAllCache(String cacheName) {
        cacheService.evictAll(cacheName);
    }
}