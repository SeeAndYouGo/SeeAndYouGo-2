package com.SeeAndYouGo.SeeAndYouGo.Dish;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MainDishResponse {
    private String restaurantName;
    private String dept;
    private String date; // YYYY-mm-DD
    private String mainDishName;
}
