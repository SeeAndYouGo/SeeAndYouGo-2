package com.SeeAndYouGo.SeeAndYouGo.Menu;

import com.SeeAndYouGo.SeeAndYouGo.Dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
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

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL)
    private List<Dish> dishList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

//    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL)
//    private List<Review> reviewList = new ArrayList<>();

    private String date;
    private Long likeCount;
    @Enumerated(EnumType.STRING)
    private Dept dept;
    @Enumerated(EnumType.STRING)
    private MenuType menuType;

    // 메뉴 문자열을 설정하는 메서드
//    public void setMenu(List<Dish> dishList) {
//        StringBuilder menuBuilder = new StringBuilder();
//        for (Dish dish : dishList) {
//            menuBuilder.append(dish.getName()).append("-");
//        }
//        this.name = menuBuilder.toString();
//    }

    // 메뉴 문자열을 반환하는 메서드
//    public String getMenu() {
//        return menu;
//    }


    // 생성자

    public Menu(Integer price, String date, Dept dept, MenuType menuType, Restaurant restaurant) {
        this.price = price;
        this.date = date;
        this.dept = dept;
        this.likeCount = 0L;
        this.menuType = menuType;
        this.restaurant = restaurant;
        restaurant.getMenuList().add(this);
    }



    @Override
    public String toString(){
        return price+" "+date+" "+dept.toString()+" "+menuType.toString()+" "+restaurant.getName()+" "+dishList.toString();
    }
}
