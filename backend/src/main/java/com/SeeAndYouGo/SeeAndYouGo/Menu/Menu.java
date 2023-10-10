package com.SeeAndYouGo.SeeAndYouGo.Menu;

import com.SeeAndYouGo.SeeAndYouGo.Dish.Dish;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu {
    @Id @GeneratedValue
    @Column(name = "menu_id")
    private Long id;

    private String name;

    private Integer price;

    @OneToMany(mappedBy = "dish", cascade = CascadeType.ALL)
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
    private Type type;

    // 메뉴 문자열을 설정하는 메서드
    public void setMenu(List<Dish> dishList) {
        StringBuilder menuBuilder = new StringBuilder();
        for (Dish dish : dishList) {
            menuBuilder.append(dish.getName()).append("-");
        }
        this.name = menuBuilder.toString();
    }

    // 메뉴 문자열을 반환하는 메서드
//    public String getMenu() {
//        return menu;
//    }


    // 생성자
    public Menu(List<Dish> dishList, Integer price, String date, Dept dept) {
        this.dishList = dishList;
        this.price = price;
        this.date = date;
        this.dept = dept;
//        setMenu(dishList); // 메뉴 문자열 설정
    }
}
