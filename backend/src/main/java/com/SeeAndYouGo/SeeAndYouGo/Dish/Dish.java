package com.SeeAndYouGo.SeeAndYouGo.Dish;

import com.SeeAndYouGo.SeeAndYouGo.MenuDish.MenuDish;
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
}