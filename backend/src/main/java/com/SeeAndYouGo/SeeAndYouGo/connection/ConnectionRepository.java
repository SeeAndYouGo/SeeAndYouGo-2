package com.SeeAndYouGo.SeeAndYouGo.connection;

import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    Connection findTopByRestaurantOrderByTimeDesc(Restaurant name);

    List<Connection> findByRestaurantAndTimeStartsWith(Restaurant restaurant, String date);

    int countByRestaurant(Restaurant restaurant);
}
