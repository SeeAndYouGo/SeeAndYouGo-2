package com.SeeAndYouGo.SeeAndYouGo.Restaurant;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantRateResponseDto {
    private String restaurantName;
    private double rateAverage;

    public RestaurantRateResponseDto(List<Restaurant> restaurants){
        double restaurantRate = 0;
        for (Restaurant restaurant : restaurants) {
            restaurantRate += restaurant.getRestaurantRate();
        }

        this.restaurantName = restaurants.get(0).getName();
        this.rateAverage = restaurantRate/restaurants.size();
    }
}
