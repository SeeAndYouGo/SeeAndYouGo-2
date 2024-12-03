package com.SeeAndYouGo.SeeAndYouGo.connection.dto;

import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import lombok.Getter;

@Getter
public class ConnectionVO {

    private Integer connected;
    // 2023-11-23 22:02:01
    private String time;
    private Restaurant restaurant;

    public ConnectionVO(Integer connected, String time, Restaurant restaurant) {
        this.connected = connected;
        this.time = time;
        this.restaurant = restaurant;
    }
}
