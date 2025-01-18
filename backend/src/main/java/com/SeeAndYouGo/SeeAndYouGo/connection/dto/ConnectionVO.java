package com.SeeAndYouGo.SeeAndYouGo.connection.dto;

import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // Jackson 라이브러리에서 직렬화할 때 사용
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
