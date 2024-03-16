package com.SeeAndYouGo.SeeAndYouGo.Menu.dto;

import com.SeeAndYouGo.SeeAndYouGo.Dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.Menu.Menu;
import lombok.*;

import java.util.LinkedList;
import java.util.List;

@Getter
@NoArgsConstructor
public class MenuResponseDto {
    private String restaurantName;
    private List<String> dishList = new LinkedList<>();
    private Integer price;
    private String dept;
    private String date;
    private String menuType;

    public MenuResponseDto(Menu menu){
        this.restaurantName = menu.getRestaurant().getName();
        setDishList(menu);
        this.price = menu.getPrice();
        this.dept = menu.getDept().toString();
        this.date = menu.getDate();
        this.menuType = menu.getMenuType().toString();
    }

    private void setDishList(Menu menu){
        for (Dish dish : menu.getDishList()) {
            this.dishList.add(dish.toString());
        }
    }
}