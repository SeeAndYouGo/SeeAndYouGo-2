package com.SeeAndYouGo.SeeAndYouGo.caching;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 메뉴 캐시 규칙이 쿼리 파라미터 기반 경로에 걸리고, 식당(+날짜)으로 키를 만드는지 검증한다.
 */
@DisplayName("메뉴 캐시 규칙")
class MenuCacheRuleTest {

    private final List<CacheRule> rules = new CacheConfig().cacheRules();

    @Test
    @DisplayName("하루치 메뉴: date가 없으면 식당, 있으면 식당:날짜로 키를 만든다")
    void dailyMenu_keyByRestaurantAndDate() {
        CacheRule rule = findRule("/api/menus/daily");

        assertThat(rule.getCacheName()).isEqualTo("daily-menu");
        assertThat(rule.generateKey(contextOf(Map.of("restaurant", "restaurant1")))).isEqualTo("restaurant1");
        assertThat(rule.generateKey(contextOf(Map.of("restaurant", "restaurant1", "date", "2026-10-20"))))
                .isEqualTo("restaurant1:2026-10-20");
    }

    @Test
    @DisplayName("하루치 메뉴: 로그인 사용자의 요청은 키워드가 포함되므로 캐시하지 않는다")
    void dailyMenu_loggedInUser_notCached() {
        CacheRule rule = findRule("/api/menus/daily");

        CacheContext loggedIn = contextOf(Map.of("restaurant", "restaurant1"));
        loggedIn.setUserEmail("user@test.com");

        assertThat(rule.generateKey(loggedIn)).isNull();
    }

    @Test
    @DisplayName("주간 메뉴: 식당으로 키를 만든다")
    void weeklyMenu_keyByRestaurant() {
        CacheRule rule = findRule("/api/menus/weekly");

        assertThat(rule.getCacheName()).isEqualTo("weekly-menu");
        assertThat(rule.generateKey(contextOf(Map.of("restaurant", "restaurant2")))).isEqualTo("restaurant2");
    }

    @Test
    @DisplayName("관리자 주간 메뉴는 캐시하지 않는다")
    void adminWeeklyMenu_notCached() {
        assertThat(rules.stream().anyMatch(rule -> rule.matches("/api/admin/menus/weekly", "GET"))).isFalse();
    }

    private CacheRule findRule(String uri) {
        return rules.stream()
                .filter(rule -> rule.matches(uri, "GET"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("캐시 규칙 없음: " + uri));
    }

    private CacheContext contextOf(Map<String, String> queryParams) {
        return CacheContext.builder()
                .queryParams(new HashMap<>(queryParams))
                .pathVariables(new HashMap<>())
                .build();
    }
}
