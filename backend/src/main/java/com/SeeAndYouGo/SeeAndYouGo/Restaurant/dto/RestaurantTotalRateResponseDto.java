package com.SeeAndYouGo.SeeAndYouGo.Restaurant.dto;

import com.SeeAndYouGo.SeeAndYouGo.Rate.Rate;
import lombok.*;

import java.util.Arrays;
import java.util.List;

@Getter
@NoArgsConstructor
public class RestaurantTotalRateResponseDto {
    private String restaurant;
    private double totalAvgRate;

    public RestaurantTotalRateResponseDto(List<Rate> rates){
        this.restaurant = rates.get(0).getRestaurant().toString();

        // getRate의 결과가 0이면 포함 안함.
        // 모든 getRate가 0이어서 평균을 구할 수 없으면 0.0 return.
        this.totalAvgRate = rates.stream()
                            .mapToDouble(Rate::getRate)
                            .filter(rate -> rate != 0.0)
                            .average()
                            .orElse(0.0);
    }
}
