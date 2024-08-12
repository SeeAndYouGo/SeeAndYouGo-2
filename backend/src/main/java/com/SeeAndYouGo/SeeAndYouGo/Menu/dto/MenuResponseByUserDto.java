package com.SeeAndYouGo.SeeAndYouGo.Menu.dto;

import com.SeeAndYouGo.SeeAndYouGo.Dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.Menu.Menu;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@Getter
@NoArgsConstructor
public class MenuResponseByUserDto {
    private Long menuId;
    private String restaurantName;
    private List<String> dishList = new LinkedList<>();
    private Integer price;
    private String dept;
    private String date;
    private String menuType;
    private List<String> keywordList = new LinkedList<>();

    public MenuResponseByUserDto(Menu menu, List<String> keywords){
        this.menuId = menu.getId();
        this.restaurantName = menu.getRestaurant().getName();
        setDishList(menu);
        this.price = menu.getPrice();
        this.dept = menu.getDept().toString();
        this.date = menu.getDate();
        this.menuType = menu.getMenuType().toString();
        this.keywordList = keywords;
    }

    private void setDishList(Menu menu){
        for (Dish dish : menu.getDishList()) {
            this.dishList.add(dish.toString());
        }
    }
}