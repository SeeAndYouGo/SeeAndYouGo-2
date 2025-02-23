package com.SeeAndYouGo.SeeAndYouGo;

import com.SeeAndYouGo.SeeAndYouGo.connection.ConnectionService;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishService;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuService;
import com.SeeAndYouGo.SeeAndYouGo.rate.RateService;
import com.SeeAndYouGo.SeeAndYouGo.holiday.HolidayRepository;
import com.SeeAndYouGo.SeeAndYouGo.holiday.HolidayService;
import com.SeeAndYouGo.SeeAndYouGo.statistics.StatisticsService;
import com.SeeAndYouGo.SeeAndYouGo.visitor.VisitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    @Value("${app.test}")
    private boolean isTest;

    private final IterService iterService;
    private final ConnectionService connectionService;
    private final MenuService menuService;
    private final RateService rateService;
    private final StatisticsService statisticsService;
    private final VisitorService visitorService;
    private final HolidayService holidayService;
    private final HolidayRepository holidayRepository;

    @Override
    public void run(String... args) throws Exception {

        // 초기 세팅 메서드들(테스트 환경이든 운영 환경이든 모두 필요함)
        rateService.setRestaurant1MenuField();
        statisticsService.initSetting();
        menuService.updateAllRestaurantMenuMap();
        connectionService.updateAllRestaurantMenuMap();
        visitorService.init();

        if(!rateService.exists()){
            rateService.insertAllRestaurant();
        }

        if(holidayRepository.count() == 0){
            holidayService.saveThisYearHoliday(LocalDate.now());
        }

        iterService.weeklyIterative();
        connectionService.saveRecentConnection();

        log.info("초기세팅 완료");
    }
}