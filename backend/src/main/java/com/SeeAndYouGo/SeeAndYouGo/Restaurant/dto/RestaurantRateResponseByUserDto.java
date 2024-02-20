package com.SeeAndYouGo.SeeAndYouGo.Restaurant.dto;

import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantRateResponseByUserDto {
    private String restaurantName;
    private double rateAverage;
    private List<String> keywordList;

    public RestaurantRateResponseByUserDto(List<Restaurant> restaurants, List<String> keywordList){
        double restaurantRate = 0;
        for (Restaurant restaurant : restaurants) {
            restaurantRate += restaurant.getRestaurantRate();
        }

        this.restaurantName = restaurants.get(0).getName();
        this.rateAverage = restaurantRate/restaurants.size();
        this.keywordList = keywordList;
    }
}