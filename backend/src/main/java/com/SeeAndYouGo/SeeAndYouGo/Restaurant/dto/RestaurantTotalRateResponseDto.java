package com.SeeAndYouGo.SeeAndYouGo.Restaurant.dto;

import com.SeeAndYouGo.SeeAndYouGo.Rate.Rate;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
public class RestaurantTotalRateResponseDto {
    private String restaurant;
    private double totalAvgRate;

    public RestaurantTotalRateResponseDto(Rate rate){
        this.restaurant = rate.getRestaurant().toString();
        this.totalAvgRate = rate.getRate();
    }
}
