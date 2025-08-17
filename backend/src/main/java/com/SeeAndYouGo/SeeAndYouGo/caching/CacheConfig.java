package com.SeeAndYouGo.SeeAndYouGo.caching;

import com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Configuration
public class CacheConfig {

    @Autowired
    private TokenProvider tokenProvider;

    /**
     * 캐시 규칙 목록 정의
     */
    @Bean
    public List<CacheRule> cacheRules() {
        return Arrays.asList(
                // 이미지 캐싱 규칙
                CacheRule.builder()
                        .cacheType(CacheType.BYTES)
                        .urlPattern("/api/images/**")
                        .method("GET")
                        .cacheName("images")
                        .ttl(Duration.ofDays(7))
                        .keyGenerator(ctx -> {
                            String uri = ctx.getUrl();
                            String[] parts = uri.split("/");
                            return parts[parts.length - 1]; // 이미지 이름
                        })
                        .build(),

                // 메뉴 캐싱 규칙
                CacheRule.builder()
                        .cacheType(CacheType.STRING)
                        .urlPattern("/api/daily-menu/{restaurant}")
                        .method("GET")
                        .cacheName("daily-menu")
                        .ttl(Duration.ofHours(6))
                        .keyGenerator(ctx -> ctx.getPathVariables().get("restaurant"))
                        .build(),

                // 주간 메뉴 캐싱 규칙
                CacheRule.builder()
                        .cacheType(CacheType.STRING)
                        .urlPattern("/api/weekly-menu/{restaurant}")
                        .method("GET")
                        .cacheName("weekly-menu")
                        .ttl(Duration.ofDays(1))
                        .keyGenerator(ctx -> ctx.getPathVariables().get("restaurant"))
                        .build(),

                // 좋아요 캐싱 규칙
                CacheRule.builder()
                        .cacheType(CacheType.STRING)
                        .urlPattern("/api/review/{reviewId}/like")
                        .method("*")
                        .cacheName("likes")
                        .ttl(Duration.ofDays(1))
                        .keyGenerator(ctx -> {
                            String reviewId = ctx.getPathVariables().get("reviewId");
                            String userEmail = ctx.getUserEmail();
                            return reviewId + ":" + userEmail;
                        })
                        .build()
        );
    }

    /**
     * 토큰에서 사용자 이메일 추출
     */
    @Bean
    public EmailExtractor emailExtractor() {
        return auth -> {
            if (auth != null && auth.startsWith("Bearer ")) {
                String token = auth.substring(7);
                return tokenProvider.decodeToEmailByAccess(token);
            }
            return null;
        };
    }

    /**
     * 이메일 추출 인터페이스
     */
    public interface EmailExtractor {
        String extractEmail(String authorization);
    }
}