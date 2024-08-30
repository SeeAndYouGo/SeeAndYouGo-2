package com.SeeAndYouGo.SeeAndYouGo.Restaurant;


import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum Restaurant {
    제1학생회관(486, Location.of(36.367838, 127.343160)),
    제2학생회관(392, Location.of(36.365959, 127.345828)),
    제3학생회관(273, Location.of(36.371479, 127.344841)),
    상록회관(140, Location.of(36.368605, 127.350374)),
    생활과학대(190, Location.of(36.376309, 127.343158));

    private final Integer capacity;
    private final Location location;

    Restaurant(int capacity, Location location) {
        this.capacity = capacity;
        this.location = location;
    }

    Restaurant(int capacity, double longitude, double latitude) {
        this.capacity = capacity;
        this.location = Location.of(longitude, latitude);
    }


    public static String parseName(String name){
        if (name.contains("1")) return "제1학생회관";
        else if (name.contains("2")) return "제2학생회관";
        else if (name.contains("3")) return "제3학생회관";
        else if (name.contains("4") || name.contains("상록회관")) return "상록회관";
        else if (name.contains("5") || name.contains("생활과학대") ) return "생활과학대";
        return name;
    }
}
