package com.SeeAndYouGo.SeeAndYouGo.statistics;

import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.List;

public interface StatisticsRepository extends JpaRepository<Statistics, Long> {

    List<Statistics> findByRestaurant(Restaurant restaurant);

    Statistics findByRestaurantAndTime(Restaurant restaurant, LocalTime time);
}
