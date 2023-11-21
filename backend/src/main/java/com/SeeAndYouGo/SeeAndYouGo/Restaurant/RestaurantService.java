package com.SeeAndYouGo.SeeAndYouGo.Restaurant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;


    public Restaurant getRestaurant(String name, String date) {
        // 평일에 이 함수가 실행되면 restaurant는 찾아와질 것임.
        return restaurantRepository.findTodayRestaurant(name, date);
    }

    public boolean checkRestaurantInDate(String name, String date) {
        Long aLong = restaurantRepository.countNumberOfDataInDate(name, date);
        return aLong > 0 ? true : false;
    }

    public boolean checkRestaurantInDate(String date) {
        Long aLong = restaurantRepository.countNumberOfDataInDate(date);
        return aLong > 0 ? true : false;
    }

    public void deleteRestaurants(String date) {
        restaurantRepository.deleteRestaurantsMatchedDate(date);
    }

    public List<Restaurant> findAllRestaurantByDate(String place, String date) {
        return restaurantRepository.findTodayAllRestaurant(parseRestaurantName(place), date);
    }

    public String parseRestaurantName(String name) {
        if (name.contains("1")) return "1학생회관";
        else if (name.contains("2")) return "2학생회관";
        else if (name.contains("3")) return "3학생회관";
        else if (name.contains("4")) return "상록회관";
        else if (name.contains("5") || name.contains("생활과학대") ) return "생활과학대";
        return name;
    }

    public void createWeeklyRestaurant(LocalDate nearestMonday) {

        // 날짜를 인자로 받아와서, 해당 날짜의 월요일과 해당 날짜의 금요일까지의 식당 객체를 생성.
        for (LocalDate date = nearestMonday; date.getDayOfWeek() != DayOfWeek.SATURDAY; date = date.plusDays(1)) {
            createRestaurantsInDate(date);
        }
    }

    private void createRestaurantsInDate(LocalDate date) {

    }
}
