package com.SeeAndYouGo.SeeAndYouGo;

import com.SeeAndYouGo.SeeAndYouGo.Connection.ConnectionService;
import com.SeeAndYouGo.SeeAndYouGo.Dish.DishService;
import com.SeeAndYouGo.SeeAndYouGo.Menu.MenuRepository;
import com.SeeAndYouGo.SeeAndYouGo.Menu.MenuService;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import com.SeeAndYouGo.SeeAndYouGo.holiday.HolidayService;
import com.SeeAndYouGo.SeeAndYouGo.statistics.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static java.time.DayOfWeek.*;

@Service
@RequiredArgsConstructor
public class IterService {
    private final DishService dishService;
    private final MenuService menuService;
    private final MenuRepository menuRepository;
    private final ConnectionService connectionService;
    private final StatisticsService statisticsService;
    private final HolidayService holidayService;
    private static final List<DayOfWeek> weekday = List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
    private static final List<DayOfWeek> weekend = List.of(SATURDAY, SUNDAY);

    @Scheduled(cron="0 0 0 * * SAT")
    public void weeklyIterative(){
        // 기본적으로 토요일에 호출되는 메섣.

        // 토, 일에 호출하면 다음주 메뉴를 불러옴.
        // 월-금에 호출하면 해당 주 메뉴를 불러옴.
        LocalDate nearestMonday = getNearestMonday(LocalDate.now());

        try {
            // Restaurant가 Enum으로 변경되었으므로 Restaurant가 아닌 Menu의 유무를 통해서 해당 메뉴를 캐싱했는지 파악한다.
            if(!menuRepository.existsByDate(nearestMonday.toString())) {
                menuService.createRestaurant1Menu(nearestMonday);

                // 월요일부터 금요일까지의 메뉴를 캐싱한다.
                dishService.saveAndCacheWeekDish(1);
                dishService.saveAndCacheWeekDish(2);
                dishService.saveAndCacheWeekDish(3);

                menuService.checkWeekMenu(nearestMonday);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Scheduled(cron="0 0 21 * * MON-FRI")
    public void updateConnectionStatistics(LocalDate now){
        // 모두 모아진 connection 데이터의 평균을 업데이트해준다.

        try {
            if(holidayService.isHoliday(now)){ // 오늘이 휴일이라면 업데이트 안함. 즉 반영 안함.
                return;
            }
            statisticsService.updateConnectionStatistics(now);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

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

    // 평일 점심 정보는 10시에 올리기
    @Scheduled(cron = "0 0 10 * * MON-FRI")
    public void postMenuInfo(){
        Restaurant[] restaurantNames = Restaurant.values();

        for (Restaurant restaurant : restaurantNames) {
            if(restaurant.equals(Restaurant.제1학생회관))
                continue;

            menuService.postMenu(restaurant, LocalDate.now().toString());
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

    @Scheduled(cron = "0 0 22 31 12 *")
    public void saveNextYearHolidayInfo(){
        // 내년의 정보는 매년 말일에 해야하므로, 다음 날을 return하여 2025년을 계산하도록 진행
        holidayService.saveThisYearHoliday(LocalDate.now().plusDays(1));
    }
}
