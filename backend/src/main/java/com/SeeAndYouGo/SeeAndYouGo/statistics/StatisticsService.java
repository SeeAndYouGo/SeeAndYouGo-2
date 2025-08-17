package com.SeeAndYouGo.SeeAndYouGo.statistics;

import com.SeeAndYouGo.SeeAndYouGo.connection.Connection;
import com.SeeAndYouGo.SeeAndYouGo.connection.ConnectionRepository;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import com.SeeAndYouGo.SeeAndYouGo.holiday.HolidayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StatisticsService {
    private final ConnectionRepository connectionRepository;
    private final StatisticsRepository statisticsRepository;
    private final HolidayService holidayService;
    private static final LocalTime START_TIME = LocalTime.of(7, 30, 0);
    private static final LocalTime END_TIME = LocalTime.of(19, 30, 0);
    private static final Long TIME_QUANTUM = 5L;  // 5분 간격으로 connection이 갱신되는 것을 의미.

    public List<ConnectionsStatisticsResponseDto> getConnectionStatistics(String restaurantName) {
        Restaurant restaurant = Restaurant.valueOf(restaurantName);
        List<Statistics> connectionStatistics = statisticsRepository.findByRestaurant(restaurant);
        return connectionStatistics.stream()
                .map(ConnectionsStatisticsResponseDto::new)
                .collect(Collectors.toList());
    }


    @Transactional
    public void updateConnectionStatistics(LocalDate date){
        log.info("Starting to update connection statistics for date: {}", date);
        if(holidayService.isHoliday(date)){ // 오늘이 휴일이라면 업데이트 안함.
            log.info("Date {} is a holiday. Skipping connection statistics update.", date);
            return;
        }

        for (Restaurant restaurant : Restaurant.values()) {
            // 여기에서 나온 리스트를 정제하는 작업이 필요.
            // 예를 들면 11시 56분에 찍힌 것은 11시 55분에 추가해주는 등..
            List<Connection> connectionByRestaurant = connectionRepository.findByRestaurantAndTimeStartsWith(restaurant, date.toString());

            for (Connection connection : connectionByRestaurant) {
                LocalTime time = getValidTime(connection);
                Statistics statistics = statisticsRepository.findByRestaurantAndTime(restaurant, time);
                if (statistics != null) {
                    statistics.updateAverageConnection(connection, date);
                } else {
                    log.warn("Statistics data not found for restaurant: {} at time: {}. Cannot update.", restaurant.name(), time);
                }
            }
        }
        log.info("Finished updating connection statistics for date: {}", date);
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
        log.info("Checking if statistics initialization is needed.");
        if(statisticsRepository.count() > 0) {
            log.info("Statistics data already exists. Initialization not needed.");
            return;
        }

        log.warn("No statistics data found. Initializing statistics table.");
        List<Statistics> statisticsList = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Restaurant restaurant : Restaurant.values()) {
            for(LocalTime time = START_TIME; !time.isAfter(END_TIME); time = time.plusMinutes(TIME_QUANTUM)){
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
        log.info("Successfully initialized statistics table with {} entries.", statisticsList.size());
    }
}
