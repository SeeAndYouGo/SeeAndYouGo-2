package com.SeeAndYouGo.SeeAndYouGo.restaurant.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class RestaurantDetailRateResponseDto {
    String category;
    List<RestaurantRateMenuResponseDto> avgRateByMenu;

    @Builder
    public RestaurantDetailRateResponseDto(String category, List<RestaurantRateMenuResponseDto> avgRateByMenu) {
        this.category = category;
        this.avgRateByMenu = avgRateByMenu;
    }
}
