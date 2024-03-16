package com.SeeAndYouGo.SeeAndYouGo.Menu.dto;

import com.SeeAndYouGo.SeeAndYouGo.Dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.Dish.DishType;
import com.SeeAndYouGo.SeeAndYouGo.Menu.Menu;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@Getter
@NoArgsConstructor
public class MenuResponseByAdminDto {

    private String mainDishName = "";

    private List<String> subDishList = new LinkedList<>();

    private String restaurantName;

    private String dept;
    private String date;

    public MenuResponseByAdminDto(Menu menu){
        this.restaurantName = menu.getRestaurant().getName();
        setDishList(menu);
        this.dept = menu.getDept().toString();
        this.date = menu.getDate();
    }

    private void setDishList(Menu menu){
        for (Dish dish : menu.getDishList()) {
            if (dish.getDishType() == DishType.MAIN) {   // MAIN Dish가 있으면 속성을 가진다.
                this.mainDishName = dish.getName();
            } else {
                this.subDishList.add(dish.toString());
            }
        }
    }
}