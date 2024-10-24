package com.SeeAndYouGo.SeeAndYouGo.menu;

import com.SeeAndYouGo.SeeAndYouGo.dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishType;
import com.SeeAndYouGo.SeeAndYouGo.menuDish.MenuDish;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import com.SeeAndYouGo.SeeAndYouGo.review.Review;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Long id;

    private Integer price;

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL)  // Menu를 저장할 때 연관된 MenuDish가 자동으로 저장되어야 함
    private List<MenuDish> menuDishes = new ArrayList<>();

    @Enumerated(value = EnumType.STRING)
    private Restaurant restaurant;

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL)
    private List<Review> reviewList = new ArrayList<>();

    private String date;
    private double rate;

    private Long likeCount = 0L;
    @Enumerated(EnumType.STRING)
    private Dept dept;
    @Enumerated(EnumType.STRING)
    private MenuType menuType;

    @Builder
    public Menu(Integer price, String date, Dept dept, MenuType menuType, Restaurant restaurant) {
        this.price = price;
        this.date = date;
        this.dept = dept;
        this.menuType = menuType;
        this.restaurant = restaurant;
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
        this.menuDishes = new ArrayList<>();
        for (Dish dish : dishes) {
            this.menuDishes.add(new MenuDish(this, dish));
        }
    }

    /**
     * dish가 이 menu에 저장되어 있지 않다면 저장해준다.
     */
    public void addDish(Dish dish) {
        List<Dish> dishList = this.getDishList();
        if (!dishList.contains(dish)) {
            MenuDish menuDish = new MenuDish(this, dish);
            this.menuDishes.add(menuDish);
            dish.getMenuDishes().add(menuDish);
        }
    }

    public List<Dish> getDishList() {
        List<Dish> dishes = new ArrayList<>();
        for(MenuDish menuDish : this.menuDishes){
            dishes.add(menuDish.getDish());
        }
        return dishes;
    }

    public List<String> getDishListToString() {
        return getDishList().stream().map(Dish::toString).collect(Collectors.toList());
    }

    public Long addReviewAndUpdateRate(Review review) {
        this.reviewList.add(review);
        this.rate = (this.rate + review.getReviewRate()) / this.reviewList.size();
        return review.getId();
    }

    public Long deleteReview(Review review) {
        double sum = this.rate * reviewList.size();
        this.reviewList.remove(review);

        if(this.reviewList.size() == 0){
            this.rate = 0.0;
        }else {
            this.rate = (sum - review.getReviewRate()) / (this.reviewList.size());
        }

        return review.getId();
    }

    public List<Dish> getMainDish() {
        List<Dish> dishes = getDishList();
        return dishes.stream()
                .filter(
                        dish -> dish.getDishType().equals(DishType.MAIN)
                ).collect(Collectors.toList());
    }

    public List<String> getMainDishToString() {
        return getMainDish()
                .stream()
                .map(Dish::toString)
                .collect(Collectors.toList());
    }

    public List<Dish> getSideDish() {
        List<Dish> dishes = getDishList();
        return dishes.stream()
                .filter(
                        dish -> dish.getDishType().equals(DishType.SIDE)
                ).collect(Collectors.toList());
    }

    public List<String> getSideDishToString() {
        return getSideDish()
                .stream()
                .map(Dish::toString)
                .collect(Collectors.toList());
    }
}