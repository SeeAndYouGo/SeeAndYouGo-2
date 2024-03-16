package com.SeeAndYouGo.SeeAndYouGo.Menu;

import com.SeeAndYouGo.SeeAndYouGo.Dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.Dish.DishType;
import lombok.*;

import java.util.LinkedList;
import java.util.List;

@Getter
@NoArgsConstructor
public class MenuResponseDto {
    private String restaurantName;
    private List<String> dishList = new LinkedList<>();

    private String mainDishName = "";

    private List<String> subDishList = new LinkedList<>();

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
            if (dish.getDishType() == DishType.MAIN) {   // MAIN Dish가 있으면 속성을 가진다.
                this.mainDishName = dish.getName();
            } else {
                this.subDishList.add(dish.toString());
            }
            this.dishList.add(dish.toString());
        }
    }
}


