package com.SeeAndYouGo.SeeAndYouGo.dish;

import com.SeeAndYouGo.SeeAndYouGo.menu.Dept;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuType;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
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

    public Dish toDish(){
        return Dish.builder()
                .name(this.getName())
                .dishType(this.getDishType())
                .build();
    }
}