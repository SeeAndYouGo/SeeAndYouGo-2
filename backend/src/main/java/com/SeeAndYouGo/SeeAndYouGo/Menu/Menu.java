package com.SeeAndYouGo.SeeAndYouGo.Menu;

import com.SeeAndYouGo.SeeAndYouGo.Dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.Dish.DishType;
import com.SeeAndYouGo.SeeAndYouGo.MenuDish.MenuDish;
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

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL)  // Menu를 저장할 때 연관된 MenuDish가 자동으로 저장되어야 함
    private List<MenuDish> menuDishes = new ArrayList<>();

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
        for (MenuDish menuDish : this.menuDishes) {
            Dish dish = menuDish.getDish();
            if(dish.getDishType().equals(DishType.MAIN)){
                return dish.getName();
            }
        }
        return this.menuDishes.get(0).getDish().getName();
    }

    public void setDishList(List<Dish> dishes) {
        for (Dish dish : dishes) {
            this.menuDishes.add(new MenuDish(this, dish));
        }
    }

    public List<Dish> getDishList() {
        List<Dish> dishes = new ArrayList<>();
        for(MenuDish menuDish : this.menuDishes){
            dishes.add(menuDish.getDish());
        }
        return dishes;
    }

//    @Override
//    public String toString(){
//        return price+" "+date+" "+dept.toString()+" "+menuType.toString()+" "+restaurant.getName()+" "+dishList.toString();
//    }
}