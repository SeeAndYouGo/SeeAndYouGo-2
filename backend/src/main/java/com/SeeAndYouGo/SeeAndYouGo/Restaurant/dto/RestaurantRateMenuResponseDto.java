package com.SeeAndYouGo.SeeAndYouGo.Restaurant.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RestaurantRateMenuResponseDto {
    String menuName;
    Integer price;
    double averageRate;

    @Builder
    public RestaurantRateMenuResponseDto(String menuName, Integer price, double averageRate) {
        this.menuName = menuName;
        this.price = price;
        this.averageRate = averageRate;
    }
}
