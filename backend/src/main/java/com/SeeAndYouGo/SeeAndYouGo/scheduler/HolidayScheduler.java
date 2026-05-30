package com.SeeAndYouGo.SeeAndYouGo.scheduler;

import com.SeeAndYouGo.SeeAndYouGo.holiday.HolidayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 휴일 정보 관련 스케줄러.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HolidayScheduler {

    private final HolidayService holidayService;

    /**
     * 매년 12월 31일 22시에 다음 해의 휴일 정보를 저장한다.
     */
    @Scheduled(cron = "${scheduler.holiday.year-end}")
    public void saveNextYearHolidayInfo() {
        try {
            // 다음 날(1월 1일)을 기준으로 해당 연도의 휴일 정보를 저장
            holidayService.saveThisYearHoliday(LocalDate.now().plusDays(1));
        } catch (Exception e) {
            log.error("Failed to save next year holiday info", e);
        }
    }
}
