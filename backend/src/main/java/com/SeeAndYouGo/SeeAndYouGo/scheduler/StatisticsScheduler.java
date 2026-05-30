package com.SeeAndYouGo.SeeAndYouGo.scheduler;

import com.SeeAndYouGo.SeeAndYouGo.statistics.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 통계 관련 스케줄러.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsScheduler {

    private final StatisticsService statisticsService;

    /**
     * 매일 저녁 21시에 연결 통계를 업데이트한다.
     */
    @Scheduled(cron = "${scheduler.statistics.daily-update}")
    public void updateConnectionStatistics() {
        LocalDate today = LocalDate.now();

        try {
            statisticsService.updateConnectionStatistics(today);
        } catch (Exception e) {
            log.error("Failed to update connection statistics for date: {}", today, e);
        }
    }
}
