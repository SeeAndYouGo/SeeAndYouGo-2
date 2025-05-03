package com.SeeAndYouGo.SeeAndYouGo.caching;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.function.Function;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheRule {

    /**
     * URL 패턴 (예: /api/images/*)
     */
    private String urlPattern;

    /**
     * HTTP 메서드 (GET, POST 등)
     */
    private String method;

    /**
     * 캐시 저장소 이름 (예: "images", "menus")
     */
    private String cacheName;

    /**
     * 캐시 키 생성 함수 (URL, 요청 데이터로부터 키 생성)
     */
    private Function<CacheContext, String> keyGenerator;

    /**
     * 캐시 유효 시간
     */
    private Duration ttl;

    /**
     * 이 규칙이 주어진 URL과 메서드에 적용되는지 확인
     */
    public boolean matches(String url, String method) {
        return URLPatternMatcher.matches(url, urlPattern) &&
                (this.method.equals("*") || this.method.equals(method));
    }

    /**
     * 캐시 키 생성
     */
    public String generateKey(CacheContext context) {
        return keyGenerator.apply(context);
    }
}