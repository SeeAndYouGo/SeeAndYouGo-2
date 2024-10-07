package com.SeeAndYouGo.SeeAndYouGo.statistics;

import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.List;

public interface StatisticsRepository extends JpaRepository<Statistics, Long> {

    List<Statistics> findByRestaurant(Restaurant restaurant);

    // @Query(value = "SELECT AVG(c.connected) avgConnection, SUBSTRING(c.time , 11, 6) time, r.name restaurantName, COUNT(*) accumulatedCount" +
    //         "FROM connection c " +
    //         "JOIN restaurant r ON c.restaurant_id = r.restaurant_id " +
    //         "GROUP BY SUBSTRING(c.time , 11, 6), r.name " +
    //         "HAVING r.name = :restaurantName;", nativeQuery = true)
    // List<ConnectionStatisticsDto> findAvgConnection(@Param("restaurantName") String restaurantName);

    Statistics findByRestaurantAndTime(Restaurant restaurant, LocalTime time);
}
