package com.SeeAndYouGo.SeeAndYouGo.scheduler;

import com.SeeAndYouGo.SeeAndYouGo.caching.annotation.EvictAllCache;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuService;
import com.SeeAndYouGo.SeeAndYouGo.menu.mainCache.ClearMainDishCache;
import com.SeeAndYouGo.SeeAndYouGo.menu.menuProvider.MenuProviderFactory;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class MenuScheduler {

    private final MenuProviderFactory menuProviderFactory;
    private final MenuService menuService;

    @Scheduled(cron = "${scheduler.menu.daily-update}")
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
            // In production, you might want more sophisticated error handling
            e.printStackTrace();
        }
    }
}
