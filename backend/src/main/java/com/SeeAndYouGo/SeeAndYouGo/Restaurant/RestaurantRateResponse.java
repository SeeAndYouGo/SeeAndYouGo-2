package com.SeeAndYouGo.SeeAndYouGo.Restaurant;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantRateResponse {
    private String restaurantName;
    private double rateAverage;
}
