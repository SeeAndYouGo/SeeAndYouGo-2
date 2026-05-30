package com.SeeAndYouGo.SeeAndYouGo.visitor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * 테스트 목표: GET /api/visitors/count 가 Redis 에서 오늘/누적 방문자수를 그대로 읽어 DTO 로 반환하는지 검증.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("방문자 캐시 - VisitorController")
class VisitorControllerTest {

    @Mock private HashOperations<String, String, String> todayRedisTemplate;
    @Mock private ValueOperations<String, String> totalRedisTemplate;

    @InjectMocks private VisitorController visitorController;

    @Test
    @DisplayName("count: Redis 의 오늘/누적 방문자수를 DTO 로 반환한다")
    void returnsTodayAndTotalFromRedis() {
        // given
        String today = LocalDate.now().toString();
        given(todayRedisTemplate.get(Const.KEY_TODAY_VISITOR, today)).willReturn("42");
        given(totalRedisTemplate.get(Const.KEY_TOTAL_VISITOR)).willReturn("12345");

        // when
        VisitorCountDto dto = visitorController.index();

        // then
        assertThat(dto.getVisitToday()).isEqualTo("42");
        assertThat(dto.getVisitTotal()).isEqualTo("12345");
    }

    @Test
    @DisplayName("count: Redis 값이 null 이면 DTO 에 null 이 그대로 들어간다 (스케줄러가 0으로 초기화하기 전 상태)")
    void returnsNullsWhenRedisIsEmpty() {
        String today = LocalDate.now().toString();
        given(todayRedisTemplate.get(Const.KEY_TODAY_VISITOR, today)).willReturn(null);
        given(totalRedisTemplate.get(Const.KEY_TOTAL_VISITOR)).willReturn(null);

        VisitorCountDto dto = visitorController.index();

        assertThat(dto.getVisitToday()).isNull();
        assertThat(dto.getVisitTotal()).isNull();
    }
}
