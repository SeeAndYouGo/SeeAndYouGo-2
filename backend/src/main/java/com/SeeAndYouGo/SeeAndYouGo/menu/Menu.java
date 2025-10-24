package com.SeeAndYouGo.SeeAndYouGo.menu;

import com.SeeAndYouGo.SeeAndYouGo.dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishType;
import com.SeeAndYouGo.SeeAndYouGo.menu.dto.MenuVO;
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

    private boolean isOpen;

    @Builder
    public Menu(Integer price, String date, Dept dept, MenuType menuType, Restaurant restaurant, boolean isOpen) {
        this.price = price;
        this.date = date;
        this.dept = dept;
        this.menuType = menuType;
        this.restaurant = restaurant;
        this.isOpen = isOpen;
    }

    public Menu(MenuVO menuVO) {
        this.price = menuVO.getPrice();
        this.date = menuVO.getDate();
        this.dept = menuVO.getDept();
        this.menuType = menuVO.getMenuType();
        this.restaurant = menuVO.getRestaurant();

        // MenuVO에서 DishList를 받아오지 않으므로 default를 false로한다.
        this.isOpen = false;
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

        // Dish가 들어왔을 때는, true를 deafult로 하고 '메뉴정보없음'이나 '운영중단'이 오면 false로 설정한다.
        boolean isOpen = true;
        for (Dish dish : dishes) {
            this.menuDishes.add(new MenuDish(this, dish));

            if(!checkIsOpen(dish)){
                isOpen = false;
            }
        }

        this.isOpen = isOpen;
    }

    private static boolean checkIsOpen(Dish dish) {
        String name = dish.getName();

        return !name.equals("메뉴 정보 없음") && !name.contains("운영중단") && !name.contains("운영안함");
    }

    /**
     * dish가 이 menu에 저장되어 있지 않다면 저장해준다.
     */
    public void addDish(Dish dish) {
        List<Dish> dishList = this.getDishList();
        boolean isOpen = true;

        if (!dishList.contains(dish)) {
            MenuDish menuDish = new MenuDish(this, dish);
            this.menuDishes.add(menuDish);
            dish.addMenuDish(menuDish);

            if(!checkIsOpen(dish)){
                isOpen = false;
            }
        }

        this.isOpen = isOpen;
    }

    public List<Dish> getDishList() {
        List<Dish> dishes = new ArrayList<>();
        for(MenuDish menuDish : this.menuDishes){
            dishes.add(menuDish.getDish());
        }
        return dishes;
    }

    public Long addReviewAndUpdateRate(Review review) {
        double sum = this.rate * reviewList.size();
        this.reviewList.add(review);

        if(this.reviewList.size() == 0){
            this.rate = 0.0;
        }else {
            this.rate = (sum + review.getReviewRate()) / (this.reviewList.size());
        }

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