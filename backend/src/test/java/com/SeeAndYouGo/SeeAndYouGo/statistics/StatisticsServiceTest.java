package com.SeeAndYouGo.SeeAndYouGo.statistics;

import com.SeeAndYouGo.SeeAndYouGo.connection.Connection;
import com.SeeAndYouGo.SeeAndYouGo.connection.ConnectionRepository;
import com.SeeAndYouGo.SeeAndYouGo.holiday.HolidayService;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * 테스트 목표: 매일 21시(스케줄러)에 그날의 혼잡도 평균을 계산해 Statistics 에 반영하는 로직 검증.
 *
 * StatisticsService#updateConnectionStatistics(LocalDate) 동작:
 *  - 휴일이면 아무 작업도 하지 않는다.
 *  - 평일이면 모든 식당의 그날 Connection 들을 조회한다.
 *  - Connection.time 의 분(minute)은 5분 단위로 내림되어 Statistics 의 시간대와 매칭된다 (예: 12:07 → 12:05).
 *  - 매칭되는 Statistics 행이 있으면 updateAverageConnection 으로 누적 평균/카운트가 갱신된다.
 *  - 누적 평균은 (기존 평균 × 기존 횟수 + 새 값) / 새 횟수 로 계산된다.
 *  - 매칭되는 Statistics 행이 없으면 조용히 무시된다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("일간 평균 혼잡도 통계 - StatisticsService.updateConnectionStatistics")
class StatisticsServiceTest {

    @Mock private ConnectionRepository connectionRepository;
    @Mock private StatisticsRepository statisticsRepository;
    @Mock private HolidayService holidayService;

    @InjectMocks private StatisticsService statisticsService;

    private static final LocalDate DATE = LocalDate.of(2025, 11, 3); // 월요일
    private static final String DATE_STR = "2025-11-03";

    @Test
    @DisplayName("휴일이면 Connection/Statistics 저장소에 일체 접근하지 않는다")
    void skipOnHoliday() {
        // given
        given(holidayService.isHoliday(DATE)).willReturn(true);

        // when
        statisticsService.updateConnectionStatistics(DATE);

        // then
        verifyNoInteractions(connectionRepository, statisticsRepository);
    }

    @Test
    @DisplayName("평일에는 모든 식당에 대해 그날의 Connection 들을 조회한다")
    void queriesEachRestaurantOnWeekday() {
        // given
        given(holidayService.isHoliday(DATE)).willReturn(false);
        given(connectionRepository.findByRestaurantAndTimeStartsWith(any(Restaurant.class), eq(DATE_STR)))
                .willReturn(Collections.emptyList());

        // when
        statisticsService.updateConnectionStatistics(DATE);

        // then - 모든 식당 enum 마다 조회 한 번씩
        for (Restaurant r : Restaurant.values()) {
            verify(connectionRepository).findByRestaurantAndTimeStartsWith(r, DATE_STR);
        }
        // 통계 row 매칭은 호출되지 않음 (Connection 목록이 비어있으므로)
        verify(statisticsRepository, never()).findByRestaurantAndTime(any(), any());
    }

    @Test
    @DisplayName("Connection 의 분이 5단위가 아니면 5단위로 내림되어 통계가 갱신된다 (12:07 → 12:05)")
    void roundsDownToFiveMinuteBucket() {
        // given
        given(holidayService.isHoliday(DATE)).willReturn(false);

        Connection conn = Connection.builder()
                .connected(100)
                .time("2025-11-03 12:07:30")
                .restaurant(Restaurant.제2학생회관)
                .build();
        Statistics target = Statistics.builder()
                .restaurant(Restaurant.제2학생회관)
                .time(LocalTime.of(12, 5))
                .updateTime(DATE.minusDays(1))
                .averageConnection(0)
                .accumulatedCount(0)
                .build();

        // 다른 식당들은 빈 리스트 (default), 2학생회관만 conn 1건
        given(connectionRepository.findByRestaurantAndTimeStartsWith(any(Restaurant.class), eq(DATE_STR)))
                .willReturn(Collections.emptyList());
        given(connectionRepository.findByRestaurantAndTimeStartsWith(Restaurant.제2학생회관, DATE_STR))
                .willReturn(List.of(conn));
        given(statisticsRepository.findByRestaurantAndTime(Restaurant.제2학생회관, LocalTime.of(12, 5)))
                .willReturn(Optional.of(target));

        // when
        statisticsService.updateConnectionStatistics(DATE);

        // then - 12:05 슬롯이 100 으로 1회 반영됨, updateTime 도 오늘로 갱신
        assertThat(target.getAverageConnection()).isEqualTo(100.0);
        assertThat(target.getAccumulatedCount()).isEqualTo(1);
        assertThat(target.getUpdateTime()).isEqualTo(DATE);
    }

    @Test
    @DisplayName("정확히 5분 단위인 Connection 은 같은 시간대에 그대로 매칭된다 (12:05 → 12:05)")
    void exactFiveMinuteBucket() {
        // given
        given(holidayService.isHoliday(DATE)).willReturn(false);

        Connection conn = Connection.builder()
                .connected(75)
                .time("2025-11-03 12:05:00")
                .restaurant(Restaurant.제3학생회관)
                .build();
        Statistics target = Statistics.builder()
                .restaurant(Restaurant.제3학생회관)
                .time(LocalTime.of(12, 5))
                .updateTime(DATE.minusDays(1))
                .averageConnection(0)
                .accumulatedCount(0)
                .build();

        given(connectionRepository.findByRestaurantAndTimeStartsWith(any(Restaurant.class), eq(DATE_STR)))
                .willReturn(Collections.emptyList());
        given(connectionRepository.findByRestaurantAndTimeStartsWith(Restaurant.제3학생회관, DATE_STR))
                .willReturn(List.of(conn));
        given(statisticsRepository.findByRestaurantAndTime(Restaurant.제3학생회관, LocalTime.of(12, 5)))
                .willReturn(Optional.of(target));

        // when
        statisticsService.updateConnectionStatistics(DATE);

        // then
        assertThat(target.getAverageConnection()).isEqualTo(75.0);
        assertThat(target.getAccumulatedCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("같은 시간대(같은 5분 슬롯)에 여러 Connection 이 있으면 평균이 누적된다")
    void accumulatesAverageWithinSameBucket() {
        // given
        given(holidayService.isHoliday(DATE)).willReturn(false);

        Connection c1 = Connection.builder()
                .connected(100)
                .time("2025-11-03 12:00:00")
                .restaurant(Restaurant.제2학생회관)
                .build();
        Connection c2 = Connection.builder()
                .connected(200)
                .time("2025-11-03 12:03:00") // 동일하게 12:00 슬롯으로 내림
                .restaurant(Restaurant.제2학생회관)
                .build();
        Statistics target = Statistics.builder()
                .restaurant(Restaurant.제2학생회관)
                .time(LocalTime.of(12, 0))
                .updateTime(DATE.minusDays(1))
                .averageConnection(0)
                .accumulatedCount(0)
                .build();

        given(connectionRepository.findByRestaurantAndTimeStartsWith(any(Restaurant.class), eq(DATE_STR)))
                .willReturn(Collections.emptyList());
        given(connectionRepository.findByRestaurantAndTimeStartsWith(Restaurant.제2학생회관, DATE_STR))
                .willReturn(Arrays.asList(c1, c2));
        given(statisticsRepository.findByRestaurantAndTime(Restaurant.제2학생회관, LocalTime.of(12, 0)))
                .willReturn(Optional.of(target));

        // when
        statisticsService.updateConnectionStatistics(DATE);

        // then - (100 + 200) / 2 = 150
        assertThat(target.getAverageConnection()).isEqualTo(150.0);
        assertThat(target.getAccumulatedCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("기존에 누적된 평균에 새 데이터가 합쳐질 때 (기존 평균 × 기존 횟수 + 새 값) / 새 횟수 로 갱신된다")
    void continuesAccumulatingAcrossDays() {
        // given - 어제까지 평균 100, 누적 2회 였던 슬롯에 오늘 120 한 건 추가
        given(holidayService.isHoliday(DATE)).willReturn(false);

        Connection conn = Connection.builder()
                .connected(120)
                .time("2025-11-03 12:00:00")
                .restaurant(Restaurant.제2학생회관)
                .build();
        Statistics existing = Statistics.builder()
                .restaurant(Restaurant.제2학생회관)
                .time(LocalTime.of(12, 0))
                .updateTime(DATE.minusDays(1))
                .averageConnection(100.0)
                .accumulatedCount(2)
                .build();

        given(connectionRepository.findByRestaurantAndTimeStartsWith(any(Restaurant.class), eq(DATE_STR)))
                .willReturn(Collections.emptyList());
        given(connectionRepository.findByRestaurantAndTimeStartsWith(Restaurant.제2학생회관, DATE_STR))
                .willReturn(List.of(conn));
        given(statisticsRepository.findByRestaurantAndTime(Restaurant.제2학생회관, LocalTime.of(12, 0)))
                .willReturn(Optional.of(existing));

        // when
        statisticsService.updateConnectionStatistics(DATE);

        // then - (100 × 2 + 120) / 3 = 320 / 3 ≈ 106.67
        assertThat(existing.getAverageConnection()).isCloseTo(106.67, Offset.offset(0.01));
        assertThat(existing.getAccumulatedCount()).isEqualTo(3);
        assertThat(existing.getUpdateTime()).isEqualTo(DATE);
    }

    @Test
    @DisplayName("매칭되는 Statistics 행이 없으면 예외 없이 조용히 스킵한다")
    void skipsSilentlyWhenStatisticsRowMissing() {
        // given
        given(holidayService.isHoliday(DATE)).willReturn(false);

        Connection conn = Connection.builder()
                .connected(50)
                .time("2025-11-03 12:00:00")
                .restaurant(Restaurant.제2학생회관)
                .build();

        given(connectionRepository.findByRestaurantAndTimeStartsWith(any(Restaurant.class), eq(DATE_STR)))
                .willReturn(Collections.emptyList());
        given(connectionRepository.findByRestaurantAndTimeStartsWith(Restaurant.제2학생회관, DATE_STR))
                .willReturn(List.of(conn));
        given(statisticsRepository.findByRestaurantAndTime(Restaurant.제2학생회관, LocalTime.of(12, 0)))
                .willReturn(Optional.empty());

        // expect - 예외 없이 정상 종료
        statisticsService.updateConnectionStatistics(DATE);
    }
}
