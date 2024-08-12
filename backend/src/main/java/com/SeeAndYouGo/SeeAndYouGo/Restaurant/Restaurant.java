package com.SeeAndYouGo.SeeAndYouGo.Restaurant;


import lombok.Getter;

@Getter
public enum Restaurant {
    제1학생회관(99),
    제2학생회관(99),
    제3학생회관(99),
    상록회관(99),
    생활과학대(99);

    private final Integer capacity;

    Restaurant(Integer capacity) {
        this.capacity = capacity;
    }

    public static String parseName(String name){
        if (name.contains("1")) return "1학생회관";
        else if (name.contains("2")) return "2학생회관";
        else if (name.contains("3")) return "3학생회관";
        else if (name.contains("4") || name.contains("상록회관")) return "상록회관";
        else if (name.contains("5") || name.contains("생활과학대") ) return "생활과학대";
        return name;
    }
}
