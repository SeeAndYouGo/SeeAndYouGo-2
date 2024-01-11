package com.SeeAndYouGo.SeeAndYouGo.Dish;

import com.SeeAndYouGo.SeeAndYouGo.Menu.Dept;
import com.SeeAndYouGo.SeeAndYouGo.Menu.Menu;
import com.SeeAndYouGo.SeeAndYouGo.Menu.MenuType;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DishDto {
    private Long id;
    private String name;
    private Menu menu;
    private Dept dept;
    private Restaurant restaurant;
    private DishType dishType;
    private int price;
    private String date;
    private MenuType menuType;
}