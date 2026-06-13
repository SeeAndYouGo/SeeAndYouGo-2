package com.SeeAndYouGo.SeeAndYouGo.visitor;

import com.SeeAndYouGo.SeeAndYouGo.scheduler.VisitorScheduler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;

/**
 * 테스트 목표: VisitorScheduler 가 Redis ↔ DB 방문자수를 안전하게 동기화하는지 검증.
 *
 *  [backupVisitorCount] 30분 마다 동작 (scheduler.visitor.backup-rate=1800000)
 *  - Redis 값이 DB 보다 크거나 같으면(정상) → Redis 값을 그대로 신뢰
 *  - Redis 값이 DB 보다 작으면(재시작·초기화) → DB + Redis 로 복구 (소실분 방어)
 *  - Redis 값이 null 이면 "0" 으로 간주
 *  - 해당 날짜의 DB 행이 없으면 신규 저장, 있으면 updateCount + save
 *
 *  [syncDBAndRedis] 매일 0시 동작 (scheduler.visitor.daily-sync)
 *  - Redis 에 데이터가 없으면 오늘만 처리하고 today=0, total=직전 total 로 초기화 (setVisitorCount)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("방문자 캐시 - VisitorScheduler")
class VisitorSchedulerTest {

    @Mock private HashOperations<String, String, String> todayRedisTemplate;
    @Mock private ValueOperations<String, String> totalRedisTemplate;
    @Mock private VisitorCountRepository visitorCountRepository;

    @InjectMocks private VisitorScheduler scheduler;

    // ===== backupVisitorCount =====

    @Test
    @DisplayName("backupVisitorCount (Redis ≥ DB, 정상): Redis 값을 그대로 신뢰해 DB/Redis 모두 동기화")
    void backup_redisHealthy_usesRedisValue() {
        // given
        LocalDate today = LocalDate.now();
        given(todayRedisTemplate.get(Const.KEY_TODAY_VISITOR, today.toString())).willReturn("100");
        given(totalRedisTemplate.get(Const.KEY_TOTAL_VISITOR)).willReturn("5000");

        VisitorCount existingToday = VisitorCount.from(80, today, false);
        VisitorCount existingTotal = VisitorCount.from(4900, today, true);
        given(visitorCountRepository.findByIsTotalFalseAndCreatedAt(today)).willReturn(Optional.of(existingToday));
        given(visitorCountRepository.findByIsTotalTrueAndCreatedAt(today)).willReturn(Optional.of(existingTotal));

        // when
        scheduler.backupVisitorCount();

        // then - 100 ≥ 80 → 100, 5000 ≥ 4900 → 5000
        verify(todayRedisTemplate).put(Const.KEY_TODAY_VISITOR, today.toString(), "100");
        verify(totalRedisTemplate).set(Const.KEY_TOTAL_VISITOR, "5000");
        assertThat(existingToday.getCount()).isEqualTo(100);
        assertThat(existingTotal.getCount()).isEqualTo(5000);
        verify(visitorCountRepository).save(existingToday);
        verify(visitorCountRepository).save(existingTotal);
    }

    @Test
    @DisplayName("backupVisitorCount (Redis < DB, 초기화됨): DB + Redis 로 복구해 소실분 보존")
    void backup_redisLost_recoversBySum() {
        // given - Redis 가 재시작되어 일부만 카운트되어 있는 상황
        LocalDate today = LocalDate.now();
        given(todayRedisTemplate.get(Const.KEY_TODAY_VISITOR, today.toString())).willReturn("5");
        given(totalRedisTemplate.get(Const.KEY_TOTAL_VISITOR)).willReturn("10");

        VisitorCount existingToday = VisitorCount.from(80, today, false);
        VisitorCount existingTotal = VisitorCount.from(4900, today, true);
        given(visitorCountRepository.findByIsTotalFalseAndCreatedAt(today)).willReturn(Optional.of(existingToday));
        given(visitorCountRepository.findByIsTotalTrueAndCreatedAt(today)).willReturn(Optional.of(existingTotal));

        // when
        scheduler.backupVisitorCount();

        // then - 5 < 80 → 80+5=85, 10 < 4900 → 4900+10=4910
        verify(todayRedisTemplate).put(Const.KEY_TODAY_VISITOR, today.toString(), "85");
        verify(totalRedisTemplate).set(Const.KEY_TOTAL_VISITOR, "4910");
        assertThat(existingToday.getCount()).isEqualTo(85);
        assertThat(existingTotal.getCount()).isEqualTo(4910);
    }

    @Test
    @DisplayName("backupVisitorCount (Redis null): \"0\" 으로 간주해 DB 값으로 복구")
    void backup_redisNull_treatsAsZero() {
        // given
        LocalDate today = LocalDate.now();
        given(todayRedisTemplate.get(Const.KEY_TODAY_VISITOR, today.toString())).willReturn(null);
        given(totalRedisTemplate.get(Const.KEY_TOTAL_VISITOR)).willReturn(null);

        VisitorCount existingToday = VisitorCount.from(80, today, false);
        VisitorCount existingTotal = VisitorCount.from(4900, today, true);
        given(visitorCountRepository.findByIsTotalFalseAndCreatedAt(today)).willReturn(Optional.of(existingToday));
        given(visitorCountRepository.findByIsTotalTrueAndCreatedAt(today)).willReturn(Optional.of(existingTotal));

        // when
        scheduler.backupVisitorCount();

        // then - 0 < 80 → 80+0=80, 0 < 4900 → 4900+0=4900
        verify(todayRedisTemplate).put(Const.KEY_TODAY_VISITOR, today.toString(), "80");
        verify(totalRedisTemplate).set(Const.KEY_TOTAL_VISITOR, "4900");
    }

    @Test
    @DisplayName("backupVisitorCount (DB 미존재): today/total 둘 다 신규 VisitorCount 로 저장")
    void backup_dbMissing_createsNewEntries() {
        // given
        LocalDate today = LocalDate.now();
        given(todayRedisTemplate.get(Const.KEY_TODAY_VISITOR, today.toString())).willReturn("42");
        given(totalRedisTemplate.get(Const.KEY_TOTAL_VISITOR)).willReturn("9999");
        given(visitorCountRepository.findByIsTotalFalseAndCreatedAt(today)).willReturn(Optional.empty());
        given(visitorCountRepository.findByIsTotalTrueAndCreatedAt(today)).willReturn(Optional.empty());

        // when
        scheduler.backupVisitorCount();

        // then - 42, 9999 둘 다 신규 저장
        ArgumentCaptor<VisitorCount> captor = ArgumentCaptor.forClass(VisitorCount.class);
        verify(visitorCountRepository, times(2)).save(captor.capture());
        List<VisitorCount> saved = captor.getAllValues();

        VisitorCount savedToday = saved.stream()
                .filter(v -> !v.isTotal()).findFirst().orElseThrow();
        assertThat(savedToday.getCount()).isEqualTo(42);
        assertThat(savedToday.getCreatedAt()).isEqualTo(today);

        VisitorCount savedTotal = saved.stream()
                .filter(VisitorCount::isTotal).findFirst().orElseThrow();
        assertThat(savedTotal.getCount()).isEqualTo(9999);
        assertThat(savedTotal.getCreatedAt()).isEqualTo(today);
    }

    // ===== syncDBAndRedis =====

    @Test
    @DisplayName("syncDBAndRedis (Redis 비어 있음): today=0 으로 초기화하고 직전 total 을 오늘 total 로 복사")
    void syncDBAndRedis_emptyRedis_initializesToday() {
        // given - Redis 에 날짜 키가 하나도 없음
        LocalDate today = LocalDate.now();
        given(todayRedisTemplate.keys(Const.KEY_TODAY_VISITOR)).willReturn(Collections.emptySet());
        given(visitorCountRepository.findByIsTotalFalseAndCreatedAt(today)).willReturn(Optional.empty());
        // 가장 최근 total 은 12345
        given(visitorCountRepository.findTopByIsTotalTrueOrderByCreatedAtDesc())
                .willReturn(Optional.of(VisitorCount.from(12345, today.minusDays(1), true)));

        // when
        scheduler.syncDBAndRedis();

        // then - Redis today 가 "0" 으로 초기화
        verify(todayRedisTemplate).put(Const.KEY_TODAY_VISITOR, today.toString(), "0");

        // DB: today(0) + total(12345) 둘 다 신규 저장
        ArgumentCaptor<VisitorCount> captor = ArgumentCaptor.forClass(VisitorCount.class);
        verify(visitorCountRepository, times(2)).save(captor.capture());
        List<VisitorCount> saved = captor.getAllValues();

        VisitorCount savedToday = saved.stream()
                .filter(v -> !v.isTotal()).findFirst().orElseThrow();
        assertThat(savedToday.getCount()).isZero();
        assertThat(savedToday.getCreatedAt()).isEqualTo(today);

        VisitorCount savedTotal = saved.stream()
                .filter(VisitorCount::isTotal).findFirst().orElseThrow();
        assertThat(savedTotal.getCount()).isEqualTo(12345);
        assertThat(savedTotal.getCreatedAt()).isEqualTo(today);
    }
}
