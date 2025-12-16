package com.SeeAndYouGo.SeeAndYouGo.scheduler;

import com.SeeAndYouGo.SeeAndYouGo.caching.annotation.EvictAllCache;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuService;
import com.SeeAndYouGo.SeeAndYouGo.menu.mainCache.ClearMainDishCache;
import com.SeeAndYouGo.SeeAndYouGo.menu.menuProvider.MenuProviderFactory;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuScheduler {

    private final MenuProviderFactory menuProviderFactory;
    private final MenuService menuService;

    @Scheduled(cron = "0 15 0 * * *") // 매일 00시 15분에 실행
    @EvictAllCache({"daily-menu", "weekly-menu"})
    @ClearMainDishCache(clearAll = true)
    public void updateDailyMenu() {
        try {
            LocalDate today = LocalDate.now();

            // Update in-memory maps using factory
            for (Restaurant restaurant : Restaurant.values()) {
                menuProviderFactory.createMenuProvider(restaurant)
                        .updateDailyMenu(restaurant, today);
            }

            // Persist changes to DB
            for (Restaurant restaurant : Restaurant.values()) {
                menuService.saveDailyMenu(restaurant, today);
            }
        } catch (Exception e) {
            log.error("Failed to update daily menu", e);
        }
    }
}
