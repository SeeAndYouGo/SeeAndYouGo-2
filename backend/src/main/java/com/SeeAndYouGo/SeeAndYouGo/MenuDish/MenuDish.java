package com.SeeAndYouGo.SeeAndYouGo.MenuDish;

import com.SeeAndYouGo.SeeAndYouGo.Dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.Menu.Menu;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;

@Entity @Getter
@IdClass(MenuDishId.class) // 복합 키를 사용하기 위한 식별자 클래스
@RequiredArgsConstructor
public class MenuDish {
    @Id
    @ManyToOne
    @JoinColumn(name = "menu_id")
    private Menu menu;
    @Id
    @ManyToOne
    @JoinColumn(name = "dish_id")
    private Dish dish;

    public MenuDish(Menu menu, Dish dish) {
        this.menu = menu;
        this.dish = dish;
    }
}
