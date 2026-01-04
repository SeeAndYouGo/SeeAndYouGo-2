package com.SeeAndYouGo.SeeAndYouGo.scheduler;

import com.SeeAndYouGo.SeeAndYouGo.connection.ConnectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

/**
 * 연결 정보 크롤링 관련 스케줄러.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectionScheduler {

    private final ConnectionService connectionService;

    /**
     * 5분마다 연결 정보를 크롤링하여 저장한다.
     * 운영 시간(06:00 ~ 19:30) 내에서만 실행된다.
     */
    @Scheduled(cron = "${scheduler.connection.crawl}")
    public void crawlConnectionInfo() {
        LocalTime now = LocalTime.now();
        LocalTime startTime = LocalTime.of(6, 0);
        LocalTime endTime = LocalTime.of(19, 30);

        if (now.isBefore(startTime) || now.isAfter(endTime)) {
            return;
        }

        try {
            connectionService.saveRecentConnection();
        } catch (Exception e) {
            log.error("Failed to crawl connection info", e);
        }
    }
}
