package com.SeeAndYouGo.SeeAndYouGo.MenuDish;

import com.SeeAndYouGo.SeeAndYouGo.Dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.Menu.Menu;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity @Getter
//@IdClass(MenuDishId.class) // 복합 키를 사용하기 위한 식별자 클래스
@NoArgsConstructor
public class MenuDish implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id")
    private Menu menu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id")
    private Dish dish;

    public MenuDish(Menu menu, Dish dish) {
        this.menu = menu;
        this.dish = dish;
    }
}
