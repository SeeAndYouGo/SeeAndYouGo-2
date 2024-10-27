package com.SeeAndYouGo.SeeAndYouGo.restaurant;

import com.SeeAndYouGo.SeeAndYouGo.menu.Dept;
import lombok.Getter;
import java.util.Arrays;
import java.util.List;

@Getter
public enum Restaurant {
    제1학생회관(486, Location.of(36.367838, 127.343160), 1, Arrays.asList(Dept.NOODLE, Dept.WESTERN, Dept.SNACK, Dept.KOREAN, Dept.JAPANESE, Dept.CHINESE)),
    제2학생회관(392, Location.of(36.365959, 127.345828), 2, Arrays.asList(Dept.STUDENT, Dept.STAFF)),
    제3학생회관(273, Location.of(36.371479, 127.344841), 3, Arrays.asList(Dept.STUDENT, Dept.STAFF)),
    상록회관(140, Location.of(36.368605, 127.350374), 4, Arrays.asList(Dept.STUDENT)),
    생활과학대(190, Location.of(36.376309, 127.343158), 5, Arrays.asList(Dept.STUDENT));

    private final Integer capacity;
    private final Location location;
    private final int number;
    private final List<Dept> possibleDept;

    Restaurant(int capacity, Location location, int number, List<Dept> possibleDept) {
        this.capacity = capacity;
        this.location = location;
        this.number = number;
        this.possibleDept = possibleDept;
    }

    public static String parseName(String name){
        for (Restaurant restaurant : Restaurant.values()) {
            if (name.contains(String.valueOf(restaurant.number)) || name.contains(restaurant.name()))
                return restaurant.name();
        }
        throw new IllegalArgumentException("[ERROR] 해당하는 레스토랑명이 없음: input: " + name);
    }
}