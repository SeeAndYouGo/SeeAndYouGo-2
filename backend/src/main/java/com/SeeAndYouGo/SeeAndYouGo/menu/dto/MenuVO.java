package com.SeeAndYouGo.SeeAndYouGo.menu.dto;

import com.SeeAndYouGo.SeeAndYouGo.dish.DishVO;
import com.SeeAndYouGo.SeeAndYouGo.menu.Dept;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuType;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import com.SeeAndYouGo.SeeAndYouGo.review.Review;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@ToString
public class MenuVO {
    private Integer price;
    private List<DishVO> dishVOs;
    private Restaurant restaurant;
    private List<Review> reviewList;
    private String date;
    private double rate;
    private Long likeCount;
    private Dept dept;
    private MenuType menuType;

    public MenuVO(Integer price, String date, Dept dept, Restaurant restaurant, MenuType menuType) {
        this.price = price;
        this.dishVOs = new ArrayList<>();
        this.restaurant = restaurant;
        this.reviewList = new ArrayList<>();
        this.date = date;
        this.rate = 0;
        this.likeCount = 0l;
        this.dept = dept;
        this.menuType = menuType;
    }

    public void addDishVO(DishVO dishVO) {
        this.dishVOs.add(dishVO);
    }
}
