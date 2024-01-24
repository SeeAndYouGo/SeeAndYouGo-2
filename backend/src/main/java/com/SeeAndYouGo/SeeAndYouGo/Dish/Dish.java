package com.SeeAndYouGo.SeeAndYouGo.Dish;

import lombok.*;
import javax.persistence.*;

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