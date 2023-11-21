package com.SeeAndYouGo.SeeAndYouGo;

import com.SeeAndYouGo.SeeAndYouGo.Connection.ConnectionRepository;
import com.SeeAndYouGo.SeeAndYouGo.Connection.ConnectionService;
import com.SeeAndYouGo.SeeAndYouGo.Dish.DishRepository;
import com.SeeAndYouGo.SeeAndYouGo.Dish.DishService;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.RestaurantRepository;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.RestaurantService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static java.time.DayOfWeek.*;

@Service
public class IterService {
    private final DishService dishService;
    private final RestaurantService restaurantService;
    private final ConnectionService connectionService;
    private final List<DayOfWeek> weekday = List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
    private final List<DayOfWeek> weekend = List.of(SATURDAY, SUNDAY);

    @Autowired
    public IterService(DishService dishService, RestaurantService restaurantService, ConnectionService connectionService) {
        this.dishService = dishService;
        this.restaurantService = restaurantService;
        this.connectionService = connectionService;
    }

    @Scheduled(cron="0 0 0 * * SAT")
    public void weeklyIterative(){
        // 기본적으로 토요일에 호출되는 메섣.

        // 토, 일에 호출하면 다음주 메뉴를 불러옴.
        // 월-금에 호출하면 해당 주 메뉴를 불러옴.
        LocalDate nearestMonday = getNearestMonday(LocalDate.now());

        try {
            // 해당 주의 식당을 만든다.
            restaurantService.createWeeklyRestaurant(nearestMonday);

            // 월요일부터 금요일까지의 메뉴를 캐싱한다.
            dishService.saveAndCacheWeekDish();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Scheduled(cron="0 0 0 * * MON-FRI")
    public void dailyIterative(){
        try {
            dishService.saveAndCacheTodayDish(LocalDate.now());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Scheduled(fixedRate = 60000, initialDelay = 1000)
    public void continuousIterative(){
        try {
            connectionService.saveAndCacheConnection();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private LocalDate getNearestMonday(LocalDate now) {
        if(weekday.contains(now.getDayOfWeek())){
            // 평일이면 해당 주 월요일 반환
            return now.with(MONDAY);
        }
        // 주말이라면 다음 주 월요일 반환
        return now.with(MONDAY).plusWeeks(1);
    }
}
