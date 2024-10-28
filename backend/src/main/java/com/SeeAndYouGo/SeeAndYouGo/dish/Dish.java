package com.SeeAndYouGo.SeeAndYouGo.dish;

import com.SeeAndYouGo.SeeAndYouGo.menu.Menu;
import com.SeeAndYouGo.SeeAndYouGo.menuDish.MenuDish;
import com.SeeAndYouGo.SeeAndYouGo.review.Review;
import lombok.*;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Dish {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dish_id")
    private Long id;
    private String name;
    @Enumerated(EnumType.STRING)
    private DishType dishType;

    @OneToMany(mappedBy = "dish")
    private List<MenuDish> menuDishes = new ArrayList<>();

    @Builder
    public Dish(String name, DishType dishType) {
        this.name = name;
        this.dishType = dishType;
    }

    @Override
    public String toString(){
        return name;
    }

    public double getRateByDish(){
        List<Menu> menusByDish = new ArrayList<>();
        for (MenuDish menuDish : menuDishes) {
            menusByDish.add(menuDish.getMenu());
        }

        return getAvgRate(menusByDish);
    }

    private double getAvgRate(List<Menu> menusByDish) {
        int count = 0;
        double sum = 0.0;

        for (Menu menu : menusByDish) {
            if(menu.getRate() != 0.0){
                count++;
                sum+= menu.getRate();
            }
        }

        return count == 0 ? 0.0 : (sum / count);
    }


    public List<Review> getReviews() {
        List<Review> reviewsByDish = new ArrayList<>();
        for (MenuDish menuDish : menuDishes) {
            reviewsByDish.addAll(menuDish.getMenu().getReviewList());
        }

        return reviewsByDish;
    }

    public void updateMainDish(){
        this.dishType = DishType.MAIN;
    }

    public void updateSideDish(){
        this.dishType = DishType.SIDE;
    }

    // 양방향 관계를 위한 헬퍼 메서드
    public void addMenuDish(MenuDish menuDish) {
        this.menuDishes.add(menuDish);
        menuDish.setDish(this);  // 양방향 관계 설정
    }
}