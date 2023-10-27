package com.SeeAndYouGo.SeeAndYouGo.Dish;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
public class Dish {
    @Id @GeneratedValue
    @Column(name = "dish_id")
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private DishType dishType;
    private String date;


}