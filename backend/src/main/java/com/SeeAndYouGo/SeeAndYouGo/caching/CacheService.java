package com.SeeAndYouGo.SeeAndYouGo.caching;

import java.util.Optional;

public interface CacheService {
    /**
     * byte[] 데이터 (이미지 등) 캐싱
     */
    void putBytes(String cacheName, String key, byte[] data);

    /**
     * 문자열 데이터 저장
     */
    void putString(String cacheName, String key, String data);


    /**
     * 캐시에서 데이터 조회
     */
    Optional<byte[]> get(String cacheName, String key);

    /**
     * 캐시 항목 삭제
     */
    void evict(String cacheName, String key);

    /**
     * 특정 캐시의 모든 항목 삭제
     */
    void evictAll(String cacheName);
}
