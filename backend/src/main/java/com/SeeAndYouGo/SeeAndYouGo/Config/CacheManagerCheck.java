package com.SeeAndYouGo.SeeAndYouGo.Config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class CacheManagerCheck implements CommandLineRunner {

    private final CacheManager cacheManager;

    public CacheManagerCheck(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void run(String... strings) {
        System.out.print("\n\n" + "=========================================================\n"
                + "Using cache manager: " + this.cacheManager.getClass().getName() + "\n"
                + "=========================================================\n\n");

        // EhcacheManager를 사용하는지, 아니면 기본(ConcurrentMapCacheManager) 사용하는지 확인용
        // 지금은 ConcurrentMapCacheManager
    }
}