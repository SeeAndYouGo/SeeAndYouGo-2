package com.SeeAndYouGo.SeeAndYouGo.connection;

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

    public ConnectionResponseDto(Connection connection){
        this.restaurantName = connection.getRestaurant().toString();
        this.capacity = connection.getRestaurant().getCapacity();
        this.connected = connection.getConnected();
        this.dateTime = connection.getTime();
    }

    public ConnectionResponseDto(Connection connection, String restaurantName){
        if(connection == null) {
            this.restaurantName = Restaurant.parseName(restaurantName);
            Restaurant restaurant = Restaurant.valueOf(this.restaurantName);
            capacity = restaurant.getCapacity();
            this.connected = 0;
            this.dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            return;
        }
        this.restaurantName = connection.getRestaurant().toString();
        this.capacity = connection.getRestaurant().getCapacity();
        this.connected = connection.getConnected();
        this.dateTime = connection.getTime();
    }
}