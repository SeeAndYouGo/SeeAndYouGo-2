package com.SeeAndYouGo.SeeAndYouGo.Dish;

import com.SeeAndYouGo.SeeAndYouGo.Menu.Dept;
import com.SeeAndYouGo.SeeAndYouGo.Menu.Menu;
import com.SeeAndYouGo.SeeAndYouGo.Menu.MenuType;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import lombok.Builder;
import lombok.Getter;

@Getter
public class DishDto {
    private String name;
    private Dept dept;
    private Restaurant restaurant;
    private DishType dishType;
    private int price;
    private String date;
    private MenuType menuType;

    @Builder
    public DishDto(String name, Dept dept, Restaurant restaurant, DishType dishType, int price, String date, MenuType menuType) {
        this.name = name;
        this.dept = dept;
        this.restaurant = restaurant;
        this.dishType = dishType;
        this.price = price;
        this.date = date;
        this.menuType = menuType;
    }
}