package com.SeeAndYouGo.SeeAndYouGo.Connection;

import lombok.AllArgsConstructor;
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
        this.restaurantName = connection.getRestaurant().getName();
        this.capacity = connection.getRestaurant().getCapacity();
        this.connected = connection.getConnected();
        this.dateTime = connection.getTime();
    }

    public ConnectionResponseDto(Connection connection, String place){
        if(connection == null) {
            restaurantName = changeRestaurantName(place);
            capacity = getCapacity(restaurantName);
            this.connected = 0;
            this.dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            return;
        }
        this.restaurantName = connection.getRestaurant().getName();
        this.capacity = connection.getRestaurant().getCapacity();
        this.connected = connection.getConnected();
        this.dateTime = connection.getTime();
    }

    public String changeRestaurantName(String name){
        if(name.contains("1")) return "1학생회관";
        else if(name.contains("2")) return "2학생회관";
        else if(name.contains("3")) return "3학생회관";
        else if(name.contains("4")) return "상록회관";
        else if(name.contains("5")) return "생활과학대";
        return "Null";
    }

    private Integer getCapacity(String name){
        if(name.contains("1학")) return 486;
        else if(name.contains("2학")) return 392;
        else if(name.contains("3학")) return 273;
        else if(name.contains("상록")) return 140;
        else return 190;
    }
}
