package com.SeeAndYouGo.SeeAndYouGo.Restaurant;


import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum Restaurant {
    제1학생회관(486, Location.of(36.367838, 127.343160), 1),
    제2학생회관(392, Location.of(36.365959, 127.345828), 2),
    제3학생회관(273, Location.of(36.371479, 127.344841), 3),
    상록회관(140, Location.of(36.368605, 127.350374), 4),
    생활과학대(190, Location.of(36.376309, 127.343158), 5);

    private final Integer capacity;
    private final Location location;
    private final int number;

    Restaurant(int capacity, Location location, int number) {
        this.capacity = capacity;
        this.location = location;
        this.number = number;
    }

    public static String parseName(String name){
        for (Restaurant restaurant : Restaurant.values()) {
            if (name.contains(String.valueOf(restaurant.number)) || name.contains(restaurant.name()))
                return restaurant.name();
        }
        throw new IllegalArgumentException("[ERROR] 해당하는 레스토랑명이 없음: input: " + name);
    }
}