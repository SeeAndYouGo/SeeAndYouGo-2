package com.SeeAndYouGo.SeeAndYouGo.scheduler;

import com.SeeAndYouGo.SeeAndYouGo.menu.menuProvider.ApiMenuProvider;
import com.SeeAndYouGo.SeeAndYouGo.menu.menuProvider.CrawlingMenuProvider;
import com.SeeAndYouGo.SeeAndYouGo.menu.menuProvider.JsonMenuProvider;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class MenuScheduler {

    private final ApiMenuProvider apiMenuProvider;
    private final JsonMenuProvider jsonMenuProvider;
    private final CrawlingMenuProvider crawlingMenuProvider;

    @Scheduled(cron = "0 0 1 * * *") // 매일 새벽 1시에 실행
    public void updateDailyMenu() {
        try {
            LocalDate today = LocalDate.now();

            // ApiMenuProvider
            apiMenuProvider.updateDailyMenu(Restaurant.제2학생회관, today);
            apiMenuProvider.updateDailyMenu(Restaurant.제3학생회관, today);
            apiMenuProvider.updateDailyMenu(Restaurant.상록회관, today);
            apiMenuProvider.updateDailyMenu(Restaurant.생활과학대, today);

            // JsonMenuProvider
            jsonMenuProvider.updateDailyMenu(Restaurant.제1학생회관, today);

            // CrawlingMenuProvider
            crawlingMenuProvider.updateDailyMenu(Restaurant.학생생활관, today);
        } catch (Exception e) {
            // Handle exceptions appropriately
            e.printStackTrace();
        }
    }
}
