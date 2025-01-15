package com.SeeAndYouGo.SeeAndYouGo.connection.dto;

import com.SeeAndYouGo.SeeAndYouGo.connection.Connection;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class ConnectionResponseDto {
    private String restaurantName;
    private Integer capacity;
    private Integer connected;
    private String dateTime;

    public ConnectionResponseDto(ConnectionVO connectionVO){
        this.restaurantName = connectionVO.getRestaurant().name();
        this.capacity = connectionVO.getRestaurant().getCapacity();
        this.connected = connectionVO.getConnected();
        this.dateTime = connectionVO.getTime();
    }
}