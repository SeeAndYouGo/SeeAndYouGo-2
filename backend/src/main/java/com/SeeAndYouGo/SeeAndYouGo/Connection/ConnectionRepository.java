package com.SeeAndYouGo.SeeAndYouGo.Connection;

import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    Connection findTopByRestaurantOrderByTimeDesc(Restaurant name);

//    @Query("SELECT MAX(ct.time) FROM Connection ct")
    Connection findTopByOrderByTimeDesc();

    List<Connection> findByRestaurantAndTimeStartsWith(Restaurant restaurant, String date);
}
