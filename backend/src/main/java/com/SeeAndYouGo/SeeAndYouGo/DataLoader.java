package com.SeeAndYouGo.SeeAndYouGo;

import com.SeeAndYouGo.SeeAndYouGo.connection.ConnectionService;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuService;
import com.SeeAndYouGo.SeeAndYouGo.menu.cache.NewDishCacheService;
import com.SeeAndYouGo.SeeAndYouGo.rate.RateService;
import com.SeeAndYouGo.SeeAndYouGo.holiday.HolidayRepository;
import com.SeeAndYouGo.SeeAndYouGo.holiday.HolidayService;
import com.SeeAndYouGo.SeeAndYouGo.scheduler.VisitorScheduler;
import com.SeeAndYouGo.SeeAndYouGo.statistics.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final IterService iterService;
    private final ConnectionService connectionService;
    private final MenuService menuService;
    private final RateService rateService;
    private final StatisticsService statisticsService;
    private final HolidayService holidayService;
    private final HolidayRepository holidayRepository;
    private final VisitorScheduler visitorScheduler;
    private final NewDishCacheService newDishCacheService;

    @Override
    public void run(String... args) throws Exception {

        // 초기 세팅 메서드들(테스트 환경이든 운영 환경이든 모두 필요함)
        rateService.setRestaurant1MenuField();
        statisticsService.initSetting();
        menuService.updateAllRestaurantMenuMap();
        connectionService.updateAllRestaurantMenuMap();
        visitorScheduler.syncDBAndRedis();

        if(!rateService.exists()){
            rateService.insertAllRestaurant();
        }

        if(holidayRepository.count() == 0){
            holidayService.saveThisYearHoliday(LocalDate.now());
        }

        iterService.weeklyIterative();
        connectionService.saveRecentConnection();
        
        // Historical 캐시 초기화
        newDishCacheService.initHistoricalCache();

        log.info("초기세팅 완료");
    }
}