package com.SeeAndYouGo.SeeAndYouGo.scheduler;

import com.SeeAndYouGo.SeeAndYouGo.caching.annotation.EvictAllCache;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuService;
import com.SeeAndYouGo.SeeAndYouGo.menu.mainCache.ClearMainDishCache;
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
    private final MenuService menuService;

    @Scheduled(cron = "0 47 15 * * *") // 매일 새벽 1시에 실행
    @EvictAllCache({"daily-menu", "weekly-menu"})
    @ClearMainDishCache(clearAll = true)
    public void updateDailyMenu() {
        try {
            LocalDate today = LocalDate.now();

            // Update in-memory maps
            apiMenuProvider.updateDailyMenu(Restaurant.제2학생회관, today);
            apiMenuProvider.updateDailyMenu(Restaurant.제3학생회관, today);
            apiMenuProvider.updateDailyMenu(Restaurant.상록회관, today);
            apiMenuProvider.updateDailyMenu(Restaurant.생활과학대, today);
            jsonMenuProvider.updateDailyMenu(Restaurant.제1학생회관, today);
            crawlingMenuProvider.updateDailyMenu(Restaurant.학생생활관, today);

            // Persist changes to DB
            for (Restaurant restaurant : Restaurant.values()) {
                menuService.saveDailyMenu(restaurant, today);
            }
        } catch (Exception e) {
            // In production, you might want more sophisticated error handling
            e.printStackTrace();
        }
    }
}
