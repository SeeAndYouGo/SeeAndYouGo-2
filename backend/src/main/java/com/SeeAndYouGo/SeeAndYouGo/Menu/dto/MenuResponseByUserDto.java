package com.SeeAndYouGo.SeeAndYouGo.Menu.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@Getter
@NoArgsConstructor
public class MenuResponseByUserDto {
    private Long menuId;
    private String restaurantName;
    private List<String> mainDishList = new LinkedList<>();
    private List<String> sideDishList = new LinkedList<>();
    private Integer price;
    private String dept;
    private String date;
    private String menuType;
    private List<String> keywordList = new LinkedList<>();

    @Builder
    private MenuResponseByUserDto(Long menuId,
                                 String restaurantName,
                                 List<String> mainDishList,
                                 List<String> sideDishList,
                                 int price,
                                 String dept,
                                 String date,
                                 String menuType,
                                 List<String> keywordList){
        this.menuId = menuId;
        this.restaurantName = restaurantName;
        this.mainDishList = mainDishList;
        this.sideDishList = sideDishList;
        this.date = date;
        this.menuType = menuType;
        this.keywordList = keywordList;
        this.price = price;
        this.dept = dept;
    }
}