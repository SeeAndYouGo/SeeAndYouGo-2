package com.SeeAndYouGo.SeeAndYouGo;

import com.SeeAndYouGo.SeeAndYouGo.Connection.ConnectionService;
import com.SeeAndYouGo.SeeAndYouGo.Rate.RateService;
import com.SeeAndYouGo.SeeAndYouGo.statistics.StatisticsService;
import com.SeeAndYouGo.SeeAndYouGo.visitor.VisitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final IterService iterService;
    private final ConnectionService connectionService;
    private final RateService rateService;
    private final StatisticsService statisticsService;
    private final VisitorService visitorService;

    @Override
    public void run(String... args) throws Exception {
        iterService.weeklyIterative();
        connectionService.saveAndCacheConnection();
        rateService.setRestaurant1MenuField();

        if(!rateService.exists()){
            rateService.insertAllRestaurant();
        }

        statisticsService.initSetting();

        visitorService.init();
    }
}