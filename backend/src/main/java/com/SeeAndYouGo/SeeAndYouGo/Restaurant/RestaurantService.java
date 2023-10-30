package com.SeeAndYouGo.SeeAndYouGo.Restaurant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;

    @Transactional
    public Restaurant getRestaurantIfExistElseCreate(String name, String date) {
        // 토 or 일요일에 이 함수가 실행되면 restaurant는 생성될 것이고,
        // 평일에 이 함수가 실행되면 restaurant는 찾아와질 것임.
        if(checkRestaurantInDate(name, date)){
            return restaurantRepository.findTodayRestaurant(name, date);
        }else{
            Restaurant restaurant = new Restaurant(name, date);
            restaurantRepository.save(restaurant);
            return restaurant;
        }
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
        System.out.println("11231453"+date);
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
        else if (name.contains("5") ||name.contains("생활과학대") ) return "생활과학대";
        return name;
    }
}
