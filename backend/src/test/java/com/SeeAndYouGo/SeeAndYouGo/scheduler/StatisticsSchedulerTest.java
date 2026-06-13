package com.SeeAndYouGo.SeeAndYouGo.scheduler;

import com.SeeAndYouGo.SeeAndYouGo.statistics.StatisticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

/**
 * 테스트 목표: 매일 21시(평일) 스케줄러가 그날의 통계 업데이트를 트리거하는지,
 * 그리고 서비스 예외가 스케줄러를 죽이지 않는지 검증한다.
 *
 * 실제 cron("0 0 21 * * MON-FRI") 은 application.yml 의 외부 설정이므로
 * 여기서는 트리거 메서드(updateConnectionStatistics)의 동작만 검증한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("일간 통계 스케줄러 - StatisticsScheduler")
class StatisticsSchedulerTest {

    @Mock private StatisticsService statisticsService;

    @InjectMocks private StatisticsScheduler statisticsScheduler;

    @Test
    @DisplayName("스케줄러는 오늘 날짜로 StatisticsService.updateConnectionStatistics 를 호출한다")
    void invokesServiceWithToday() {
        // when
        statisticsScheduler.updateConnectionStatistics();

        // then
        verify(statisticsService).updateConnectionStatistics(LocalDate.now());
    }

    @Test
    @DisplayName("Service 가 예외를 던져도 스케줄러는 예외를 흘려보내지 않는다")
    void swallowsServiceException() {
        // given
        willThrow(new RuntimeException("DB down"))
                .given(statisticsService).updateConnectionStatistics(any());

        // expect - 예외가 외부로 전파되지 않음
        statisticsScheduler.updateConnectionStatistics();

        verify(statisticsService).updateConnectionStatistics(any());
    }
}
