package com.SeeAndYouGo.SeeAndYouGo;

import com.SeeAndYouGo.SeeAndYouGo.Connection.ConnectionService;
import com.SeeAndYouGo.SeeAndYouGo.Dish.DishService;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static java.time.DayOfWeek.*;

@Service
public class IterService {
    private final DishService dishService;
    private final RestaurantService restaurantService;
    private final ConnectionService connectionService;
    private static final List<DayOfWeek> weekday = List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
    private static final List<DayOfWeek> weekend = List.of(SATURDAY, SUNDAY);

    @Autowired
    public IterService(DishService dishService, RestaurantService restaurantService, ConnectionService connectionService) {
        this.dishService = dishService;
        this.restaurantService = restaurantService;
        this.connectionService = connectionService;
    }

    @Scheduled(cron="0 0 0 * * SAT")
    @Transactional
    public void weeklyIterative(){
        // 기본적으로 토요일에 호출되는 메섣.

        // 토, 일에 호출하면 다음주 메뉴를 불러옴.
        // 월-금에 호출하면 해당 주 메뉴를 불러옴.
        LocalDate nearestMonday = getNearestMonday(LocalDate.now());

        try {
            if(!restaurantService.existWeekRestaurant(nearestMonday)) {
                // 해당 주의 식당을 만든다.
                restaurantService.createWeeklyRestaurant(nearestMonday);

                // 월요일부터 금요일까지의 메뉴를 캐싱한다.
                dishService.saveAndCacheWeekDish(1);
                dishService.saveAndCacheWeekDish(2);
                dishService.saveAndCacheWeekDish(3);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

//    @Scheduled(cron="0 0 0 * * MON-FRI")
//    public void dailyIterative(){
//        try {
//            dishService.saveAndCacheTodayDish(LocalDate.now());
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        }
//    }

    @Scheduled(cron = "40 0/5 7-20 * * *")
    public void continuousIterative(){
        try {
            LocalTime now = LocalTime.now();
            LocalTime startTime = LocalTime.of(7, 30);
            LocalTime endTime = LocalTime.of(19, 30);
            if (now.isBefore(startTime) || now.isAfter(endTime)) {
                return;
            }
            connectionService.saveAndCacheConnection();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static LocalDate getNearestMonday(LocalDate now) {
        if(weekday.contains(now.getDayOfWeek())){
            // 평일이면 해당 주 월요일 반환
            return now.with(MONDAY);
        }
        // 주말이라면 다음 주 월요일 반환
        return now.with(MONDAY).plusWeeks(1);
    }

    public static LocalDate getFridayOfWeek(LocalDate inputDate) {
        // 해당 날짜의 요일을 얻습니다.
        DayOfWeek dayOfWeek = inputDate.getDayOfWeek();

        // 현재 날짜에서 금요일까지의 차이를 계산합니다.
        int daysUntilFriday = DayOfWeek.FRIDAY.getValue() - dayOfWeek.getValue() + (dayOfWeek.getValue() >= DayOfWeek.FRIDAY.getValue() ? 7 : 0);

        // 입력된 날짜에서 금요일까지의 날짜를 반환합니다.
        return inputDate.plusDays(daysUntilFriday);
    }
}
