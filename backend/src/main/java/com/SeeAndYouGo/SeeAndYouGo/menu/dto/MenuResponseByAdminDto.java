package com.SeeAndYouGo.SeeAndYouGo.menu.dto;

import com.SeeAndYouGo.SeeAndYouGo.menu.Menu;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@Getter
@NoArgsConstructor
public class MenuResponseByAdminDto {
    private Long menuId;
    private List<String> mainDishList = new LinkedList<>();
    private List<String> sideDishList = new LinkedList<>();
    private String restaurantName;
    private String dept;
    private String date;

    public MenuResponseByAdminDto(Menu menu){
        this.menuId = menu.getId();
        this.restaurantName = menu.getRestaurant().toString();
        this.mainDishList = menu.getMainDishToString();
        this.sideDishList = menu.getSideDishToString();
        this.dept = menu.getDept().toString();
        this.date = menu.getDate();
    }
}