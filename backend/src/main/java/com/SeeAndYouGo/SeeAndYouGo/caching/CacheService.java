package com.SeeAndYouGo.SeeAndYouGo.caching;

import java.util.Optional;

public interface CacheService {
    void putBytes(String cacheName, String key, byte[] data);
    void putString(String cacheName, String key, String data);
    void putBytes(String cacheName, String key, byte[] data, CacheRule rule);
    void putString(String cacheName, String key, String data, CacheRule rule);
    Optional<byte[]> getBytesCache(String cacheName, String key);
    Optional<String> getStringCache(String cacheName, String key);
    void evict(String cacheName, String key);
    void evictAll(String cacheName);
}
