package com.SeeAndYouGo.SeeAndYouGo.dish;

import lombok.Getter;

@Getter
public class DishVO {
    private String name;
    private DishType dishType;

    public DishVO(String name, DishType dishType) {
        this.name = name;
        this.dishType = dishType;
    }
}
