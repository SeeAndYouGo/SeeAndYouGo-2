package com.SeeAndYouGo.SeeAndYouGo.caching;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

@DisplayName("CacheFilter")
class CacheFilterTest {

    @Test
    @DisplayName("키 생성 함수가 null을 반환하면 캐시를 조회·저장하지 않고 요청을 그대로 처리한다")
    void nullKey_bypassesCache() throws Exception {
        CacheService cacheService = mock(CacheService.class);
        CacheRule rule = CacheRule.builder()
                .cacheType(CacheType.STRING)
                .urlPattern("/api/menus/daily")
                .method("GET")
                .cacheName("daily-menu")
                .ttl(Duration.ofHours(6))
                .keyGenerator(ctx -> null)
                .build();
        CacheFilter cacheFilter = new CacheFilter(cacheService, List.of(rule), auth -> "user@test.com");

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/menus/daily");
        MockFilterChain chain = new MockFilterChain();

        cacheFilter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isNotNull();
        verifyNoInteractions(cacheService);
    }
}
