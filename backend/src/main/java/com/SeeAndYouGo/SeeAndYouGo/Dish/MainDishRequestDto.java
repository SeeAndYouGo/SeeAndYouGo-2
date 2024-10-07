package com.SeeAndYouGo.SeeAndYouGo.Dish;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
public class MainDishRequestDto {
    private String restaurantName;
    private String dept;
    private String date; // YYYY-mm-DD
    private List<String> mainDishList;
    private List<String> sideDishList;
}
