package com.SeeAndYouGo.SeeAndYouGo.Restaurant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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
        restaurantRepository.deleteRestaurantsMatchedDate(date);
    }
}
