package com.SeeAndYouGo.SeeAndYouGo.Dish;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Dish {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dish_id")
    private Long id;
    private String name;
    @Enumerated(EnumType.STRING)
    private DishType dishType;

    @Override
    public String toString(){
        return name;
    }
}