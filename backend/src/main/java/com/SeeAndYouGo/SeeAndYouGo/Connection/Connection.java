package com.SeeAndYouGo.SeeAndYouGo.Connection;

import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Connection {
    @Id @GeneratedValue
    @Column(name = "connection_id")
    private Long id;

    private Integer connected;

    private String time;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    // ####### 생성 메서드 ############
    public static Connection createNewConnection(Integer connected, String time, Restaurant restaurant) {
        Connection connection = new Connection();
        connection.setConnected(connected);
        connection.setTime(time);
        connection.setRestaurant(restaurant);
        return connection;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
        restaurant.getConnectionList().add(this);
    }
}
