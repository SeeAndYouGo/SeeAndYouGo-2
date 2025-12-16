package com.SeeAndYouGo.SeeAndYouGo.statistics;

import com.SeeAndYouGo.SeeAndYouGo.connection.Connection;
import com.SeeAndYouGo.SeeAndYouGo.connection.ConnectionRepository;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import com.SeeAndYouGo.SeeAndYouGo.holiday.HolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StatisticsService {
    private final ConnectionRepository connectionRepository;
    private final StatisticsRepository statisticsRepository;
    private final HolidayService holidayService;

    @Value("${app.statistics.start-time}")
    private String startTimeStr;

    @Value("${app.statistics.end-time}")
    private String endTimeStr;

    @Value("${app.statistics.time-quantum}")
    private Long timeQuantum;

    private LocalTime startTime;
    private LocalTime endTime;

    @PostConstruct
    public void init() {
        this.startTime = LocalTime.parse(startTimeStr);
        this.endTime = LocalTime.parse(endTimeStr);
    }

    public List<ConnectionsStatisticsResponseDto> getConnectionStatistics(String restaurantName) {
        Restaurant restaurant = Restaurant.valueOf(restaurantName);
        List<Statistics> connectionStatistics = statisticsRepository.findByRestaurant(restaurant);

        List<ConnectionsStatisticsResponseDto> result = new ArrayList<>();
        connectionStatistics.stream().forEach(statistics -> result.add(new ConnectionsStatisticsResponseDto(statistics)));

        return result;
    }


    @Transactional
    public void updateConnectionStatistics(LocalDate date){
        if(holidayService.isHoliday(date)){ // 오늘이 휴일이라면 업데이트 안함. 즉 반영 안함.
            return;
        }

        Restaurant[] restaurants = Restaurant.values();

        for (Restaurant restaurant : restaurants) {
            // 여기에서 나온 리스트를 정제하는 작업이 필요.
            // 예를 들면 11시 56분에 찍힌 것은 11시 55분에 추가해주는 등..
            List<Connection> connectionByRestaurant = connectionRepository.findByRestaurantAndTimeStartsWith(restaurant, date.toString());
            for (Connection connection : connectionByRestaurant) {
                LocalTime time = getValidTime(connection);
                statisticsRepository.findByRestaurantAndTime(restaurant, time)
                        .ifPresent(statistics -> statistics.updateAverageConnection(connection, date));
            }
        }
    }

    /**
     * 2024-02-14 12:15:40 에서 12:15만 추출
     * 이 때, 분 단위가 5로 떨어지지 않으면 5로 떨어지도록 바꿔줌.
     */
    private static LocalTime getValidTime(Connection connection) {
        String timeStr = connection.getTime().substring(11, 16);
        LocalTime time = LocalTime.parse(timeStr);
        return parseValidTime(time);
    }

    private static LocalTime parseValidTime(LocalTime time) {
        int minuteErrorRange = time.getMinute() % 5;
        if(minuteErrorRange != 0){
            return time.minusMinutes(minuteErrorRange);
        }

        return time;
    }

    @Transactional
    public void initSetting() {
        if(statisticsRepository.count() > 0) return;

        Restaurant[] restaurants = Restaurant.values();
        LocalDate today = LocalDate.now();

        List<Statistics> statisticsList = new ArrayList<>();
        for (Restaurant restaurant : restaurants) {
            for(LocalTime time = startTime; time.isBefore(endTime) || time.equals(endTime); time = time.plusMinutes(timeQuantum)){
                Statistics statistics = Statistics.builder()
                        .restaurant(restaurant)
                        .time(time)
                        .updateTime(today)
                        .averageConnection(0)
                        .accumulatedCount(0)
                        .build();

                statisticsList.add(statistics);
            }
        }

        statisticsRepository.saveAll(statisticsList);
    }
}
