package com.SeeAndYouGo.SeeAndYouGo.Menu;

import com.SeeAndYouGo.SeeAndYouGo.Dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.Dish.DishType;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import com.SeeAndYouGo.SeeAndYouGo.Review.Review;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Long id;

    private Integer price;

    @ManyToMany(cascade = CascadeType.ALL)
    private List<Dish> dishList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL)
    private List<Review> reviewList = new ArrayList<>();

    private String date;
    private Long likeCount;
    @Enumerated(EnumType.STRING)
    private Dept dept;
    @Enumerated(EnumType.STRING)
    private MenuType menuType;

    public Menu(Integer price, String date, Dept dept, MenuType menuType, Restaurant restaurant) {
        this.price = price;
        this.date = date;
        this.dept = dept;
        this.likeCount = 0L;
        this.menuType = menuType;
        this.restaurant = restaurant;
        restaurant.getMenuList().add(this);
    }

    public String getMenuName(){
        for (Dish dish : this.dishList) {
            if(dish.getDishType().equals(DishType.MAIN)){
                return dish.getName();
            }
        }
        return this.dishList.get(0).getName();
    }

    @Override
    public String toString(){
        return price+" "+date+" "+dept.toString()+" "+menuType.toString()+" "+restaurant.getName()+" "+dishList.toString();
    }
}