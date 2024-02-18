package com.SeeAndYouGo.SeeAndYouGo.Restaurant;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
public class RestaurantTotalRateResponseDto {
    private double totalAvgRate;

    @Builder
    public RestaurantTotalRateResponseDto(double totalAvgRate) {
        this.totalAvgRate = totalAvgRate;
    }
}
