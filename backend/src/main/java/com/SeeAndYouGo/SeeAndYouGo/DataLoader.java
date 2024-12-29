package com.SeeAndYouGo.SeeAndYouGo;

import com.SeeAndYouGo.SeeAndYouGo.connection.ConnectionService;
import com.SeeAndYouGo.SeeAndYouGo.rate.RateService;
import com.SeeAndYouGo.SeeAndYouGo.holiday.HolidayRepository;
import com.SeeAndYouGo.SeeAndYouGo.holiday.HolidayService;
import com.SeeAndYouGo.SeeAndYouGo.statistics.StatisticsService;
import com.SeeAndYouGo.SeeAndYouGo.visitor.VisitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    @Value("${app.test}")
    private boolean isTest;

    private final IterService iterService;
    private final ConnectionService connectionService;
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
        visitorService.init();

        if(!rateService.exists()){
            rateService.insertAllRestaurant();
        }

        if(holidayRepository.count() == 0){
            holidayService.saveThisYearHoliday(LocalDate.now());
        }

        // 데이터 삽입 메서드(테스트 환경에서는 데이터를 임의로 넣어줄 것이므로 불필요함.
        if(!isTest) {
            iterService.weeklyIterative();
            connectionService.saveRecentConnection();
        }
    }
}