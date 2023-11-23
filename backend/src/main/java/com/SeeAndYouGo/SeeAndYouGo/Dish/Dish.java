package com.SeeAndYouGo.SeeAndYouGo.Dish;

import com.SeeAndYouGo.SeeAndYouGo.Menu.Dept;
import com.SeeAndYouGo.SeeAndYouGo.Menu.Menu;
import com.SeeAndYouGo.SeeAndYouGo.Menu.MenuType;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Dish {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dish_id")
    private Long id;

    private String name;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="menu_id")
    private Menu menu;
    @Enumerated(EnumType.STRING)
    private Dept dept;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="restaurant_id")
    private Restaurant restaurant;
    @Enumerated(EnumType.STRING)
    private DishType dishType;
    private int price;
    private String date;
    @Enumerated(EnumType.STRING)
    private MenuType menuType;

    // ------- 생성 메서드 --------
    public Dish(String name, Dept dept, String date, DishType dishType, Restaurant restaurant, MenuType menuType, int price) {
        this.name = name;
        this.dept = dept;
        this.date = date;
        this.dishType = dishType;
        this.restaurant = restaurant;
        this.menuType = menuType;
        this.price = price;
    }

    @Override
    public String toString(){
        return name;
    }
}