package com.SeeAndYouGo.SeeAndYouGo.Connection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ConnectionResponseDto {
    private String restaurantName;
    private Integer capacity;
    private Integer connected;
    private String dateTime;

    public ConnectionResponseDto(Connection connection){
        this.restaurantName = connection.getRestaurant().getName();
        this.capacity = connection.getRestaurant().getCapacity();
        this.connected = connection.getConnected();
        this.dateTime = connection.getTime();
    }
}
