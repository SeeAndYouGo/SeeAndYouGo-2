package com.SeeAndYouGo.SeeAndYouGo.menu.mainCache;

import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * NewDishCacheService의 Redis 통합 테스트 (간소화 버전)
 *
 * Redis의 기본 CRUD 기능만 테스트합니다.
 * 복잡한 DB 연동 시나리오는 HistoricalCacheTest에서 검증합니다.
 *
 * Redis 서버 시작 방법:
 * - macOS: brew install redis && brew services start redis
 * - Docker: docker run -d -p 6379:6379 redis:latest
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(RedisLocalTestConfig.class)
class NewDishCacheServiceRedisIntegrationTest {

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired(required = false)
    private NewDishCacheService newDishCacheService;

    private static final String CACHE_KEY_PREFIX = "historical:main-dishes:";
    private static final String SYNC_KEY_PREFIX = "historical:last-sync:";

    @BeforeEach
    void setUp() {
        // Redis가 없으면 테스트 스킵
        assumeTrue(redisTemplate != null, "Redis is not available. Skipping test.");
        assumeTrue(newDishCacheService != null, "NewDishCacheService is not available.");

        // Redis 캐시 초기화
        clearAllRedisCache();
    }

    @AfterEach
    void tearDown() {
        if (redisTemplate != null) {
            clearAllRedisCache();
        }
    }

    private void clearAllRedisCache() {
        try {
            Set<String> keys = redisTemplate.keys("historical:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (RedisConnectionFailureException e) {
            System.err.println("Redis connection failed. Skipping cache clear.");
        }
    }

    @Test
    @DisplayName("Redis - Long 타입 ID 저장 및 조회")
    void redisStoresAndRetrievesLongIds() {
        // given
        String cacheKey = CACHE_KEY_PREFIX + Restaurant.제2학생회관.toString();
        Long dishId1 = 100L;
        Long dishId2 = 200L;

        // when - Redis에 ID 저장
        redisTemplate.opsForSet().add(cacheKey, dishId1, dishId2);

        // then - 저장된 ID 조회 (Jackson serializer는 작은 숫자를 Integer로 저장)
        Set<Object> cachedIds = redisTemplate.opsForSet().members(cacheKey);

        assertThat(cachedIds).hasSize(2);
        // Number로 변환하여 비교 (Integer/Long 모두 허용)
        Set<Long> longIds = cachedIds.stream()
                .map(id -> ((Number) id).longValue())
                .collect(java.util.stream.Collectors.toSet());
        assertThat(longIds).containsExactlyInAnyOrder(dishId1, dishId2);
    }

    @Test
    @DisplayName("Redis - Set 자료구조로 중복 자동 제거")
    void redisSetRemovesDuplicates() {
        // given
        String cacheKey = CACHE_KEY_PREFIX + Restaurant.제2학생회관.toString();
        Long dishId = 100L;

        // when - 같은 ID를 여러 번 추가
        redisTemplate.opsForSet().add(cacheKey, dishId, dishId, dishId);

        // then - 중복 제거되어 1개만 존재 (Jackson serializer는 Integer로 저장)
        Set<Object> cachedIds = redisTemplate.opsForSet().members(cacheKey);

        assertThat(cachedIds).hasSize(1);
        Object retrieved = cachedIds.iterator().next();
        assertThat(retrieved).isInstanceOf(Number.class);
        assertThat(((Number) retrieved).longValue()).isEqualTo(dishId);
    }

    @Test
    @DisplayName("Redis - getHistoricalMainDishes 캐시 히트")
    void getHistoricalMainDishes_CacheHit() {
        // given - Redis에 직접 ID 저장
        String cacheKey = CACHE_KEY_PREFIX + Restaurant.제2학생회관.toString();
        Long dishId1 = 100L;
        Long dishId2 = 200L;
        redisTemplate.opsForSet().add(cacheKey, dishId1, dishId2);

        // when - 캐시 조회
        Set<Long> result = newDishCacheService.getHistoricalMainDishes(Restaurant.제2학생회관);

        // then - 저장한 ID가 정확히 반환됨
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(dishId1, dishId2);
    }

    @Test
    @DisplayName("Redis - clearHistoricalCache 특정 레스토랑 캐시 삭제")
    void clearHistoricalCache_ClearsSpecificRestaurant() {
        // given - 2개 레스토랑 캐시 생성
        String cacheKey1 = CACHE_KEY_PREFIX + Restaurant.제2학생회관.toString();
        String cacheKey2 = CACHE_KEY_PREFIX + Restaurant.제3학생회관.toString();
        redisTemplate.opsForSet().add(cacheKey1, 100L);
        redisTemplate.opsForSet().add(cacheKey2, 200L);

        // when - 2학생회관만 삭제
        newDishCacheService.clearHistoricalCache(Restaurant.제2학생회관);

        // then - 2학생회관만 삭제됨
        assertThat(redisTemplate.hasKey(cacheKey1)).isFalse();
        assertThat(redisTemplate.hasKey(cacheKey2)).isTrue();
    }

    @Test
    @DisplayName("Redis - clearAllHistoricalCache 모든 캐시 삭제")
    void clearAllHistoricalCache_ClearsAllRestaurants() {
        // given - 여러 레스토랑 캐시 생성
        for (Restaurant restaurant : Restaurant.values()) {
            String cacheKey = CACHE_KEY_PREFIX + restaurant.toString();
            redisTemplate.opsForSet().add(cacheKey, 100L);
        }

        // when - 전체 삭제
        newDishCacheService.clearAllHistoricalCache();

        // then - 모두 삭제됨
        for (Restaurant restaurant : Restaurant.values()) {
            String cacheKey = CACHE_KEY_PREFIX + restaurant.toString();
            assertThat(redisTemplate.hasKey(cacheKey)).isFalse();
        }
    }

    @Test
    @DisplayName("Redis - 마지막 동기화 날짜 저장 및 조회")
    void redisStoresLastSyncDate() {
        // given
        String syncKey = SYNC_KEY_PREFIX + Restaurant.제2학생회관.toString();
        String yesterday = "2025-11-01";

        // when - 날짜 저장
        redisTemplate.opsForValue().set(syncKey, yesterday);

        // then - 저장된 날짜 조회
        Object lastSync = redisTemplate.opsForValue().get(syncKey);

        assertThat(lastSync).isNotNull();
        assertThat(lastSync.toString()).isEqualTo(yesterday);
    }

    @Test
    @DisplayName("Redis - ID 타입 검증 (Long vs String)")
    void redisIdTypeVerification() {
        // given
        String cacheKey = CACHE_KEY_PREFIX + Restaurant.제2학생회관.toString();
        Long longId = 123L;

        // when
        redisTemplate.opsForSet().add(cacheKey, longId);

        // then - Long 타입으로 저장되고 조회됨
        Set<Object> cached = redisTemplate.opsForSet().members(cacheKey);

        assertThat(cached).hasSize(1);
        Object retrieved = cached.iterator().next();

        // Integer로 저장될 수 있으므로 Number로 확인
        assertThat(retrieved).isInstanceOf(Number.class);
        assertThat(((Number) retrieved).longValue()).isEqualTo(123L);
    }
}
