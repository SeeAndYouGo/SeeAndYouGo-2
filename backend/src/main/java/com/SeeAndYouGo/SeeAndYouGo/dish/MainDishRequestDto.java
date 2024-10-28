package com.SeeAndYouGo.SeeAndYouGo.dish;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MainDishRequestDto {
    private String restaurantName;
    private String dept;
    private String date; // YYYY-mm-DD
    private List<String> mainDishList;
    private List<String> sideDishList;
}