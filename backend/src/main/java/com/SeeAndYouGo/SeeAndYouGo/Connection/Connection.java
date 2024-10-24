package com.SeeAndYouGo.SeeAndYouGo.connection;

import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Connection {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "connection_id")
    private Long id;

    private Integer connected;

    private String time;

    @Enumerated(value = EnumType.STRING)
    private Restaurant restaurant;

    // ####### 생성 메서드 ############
    @Builder
    public Connection(Integer connected, String time, Restaurant restaurant) {
        this.connected = connected;
        this.time = time;
        this.restaurant = restaurant;
    }

    public void updateRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public static String parseRestaurantNameForCache(String name){
        if(name.contains("Je1")) return "제1학생회관";
        else if(name.contains("제2학생회관")) return "제2학생회관";
        else if(name.contains("Je3_Hak") || name.contains("3학생")) return "제3학생회관";
        else if(name.contains("제4학생")) return "상록회관";
        else if(name.contains("생활과학대 1F")) return "생활과학대";
        else return "NULL";
    }
}
