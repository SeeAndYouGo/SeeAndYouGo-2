package com.SeeAndYouGo.SeeAndYouGo.Restaurant;

import com.SeeAndYouGo.SeeAndYouGo.Connection.Connection;
import com.SeeAndYouGo.SeeAndYouGo.Menu.Menu;
import com.SeeAndYouGo.SeeAndYouGo.Review.Review;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Restaurant {
    @Id @GeneratedValue
    @Column(name = "restaurant_id")
    private Long Id;
    private String name;
    private Integer capacity;
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<Menu> menuList = new ArrayList<>();
    private String date;
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<Connection> connectionList = new ArrayList<>();
    private Double restaurantRate;
    private Double latitude;
    private Double longitude;
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<Review> reviewList = new ArrayList<>();

    public Restaurant(String name, String date) {
        this.setName(name);
        this.setDate(date);
        setLatitude(name);
        setLongitude(name);
        setCapacity(name);
        this.restaurantRate = 0.0;
    }

    private void setLatitude(String name){
        if(name.contains("1학")) latitude = 1.1;
        else if(name.contains("2학")) latitude = 2.2;
        else if(name.contains("3학")) latitude = 3.3;
        else if(name.contains("상록")) latitude = 4.4;
        else if(name.contains("생활")) latitude = 5.5;
    }

    private void setCapacity(String name){
        if(name.contains("1학")) capacity = 486;
        else if(name.contains("2학")) capacity = 392;
        else if(name.contains("3학")) capacity = 273;
        else if(name.contains("상록")) capacity = 140;
        else if(name.contains("생활")) capacity = 190;
    }

    private void setLongitude(String name){
        if(name.contains("1학")) longitude = 1.1;
        else if(name.contains("2학")) longitude = 2.2;
        else if(name.contains("3학")) longitude = 3.3;
        else if(name.contains("상록")) longitude = 4.4;
        else if(name.contains("생활")) longitude = 5.5;
    }

    private void setRestaurantRate(List<Review> reviewList){
        Double rate = 0.0;
        for (Review review : reviewList) {
            rate += review.getReviewRate();
        }
        this.restaurantRate = rate/reviewList.size();
    }
}
