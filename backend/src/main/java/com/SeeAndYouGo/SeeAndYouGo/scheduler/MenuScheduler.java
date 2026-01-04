package com.SeeAndYouGo.SeeAndYouGo.scheduler;

import com.SeeAndYouGo.SeeAndYouGo.caching.annotation.EvictAllCache;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuRepository;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuService;
import com.SeeAndYouGo.SeeAndYouGo.menu.mainCache.ClearMainDishCache;
import com.SeeAndYouGo.SeeAndYouGo.menu.menuProvider.MenuProviderFactory;
import com.SeeAndYouGo.SeeAndYouGo.rate.RateService;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static com.SeeAndYouGo.SeeAndYouGo.global.DateUtils.getNearestMonday;
import static com.SeeAndYouGo.SeeAndYouGo.global.DateUtils.getSundayOfWeek;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuScheduler {

    private final MenuProviderFactory menuProviderFactory;
    private final MenuService menuService;
    private final MenuRepository menuRepository;
    private final RateService rateService;

    /**
     * 매일 자정에 해당 날짜의 메뉴를 업데이트한다.
     */
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
            log.error("Failed to update daily menu", e);
        }
    }

    /**
     * 매주 월요일 자정에 주간 메뉴를 저장한다.
     */
    @Scheduled(cron = "${scheduler.iter.weekly-menu}")
    public void saveWeeklyMenu() {
        try {
            LocalDate nearestMonday = getNearestMonday(LocalDate.now());
            LocalDate sunday = getSundayOfWeek(nearestMonday);

            if (!menuRepository.existsByDate(nearestMonday.toString())) {
                menuService.saveWeeklyMenuAllRestaurant(nearestMonday, sunday);
                rateService.saveRate();
            }
        } catch (Exception e) {
            log.error("Failed to save weekly menu", e);
        }
    }
}
