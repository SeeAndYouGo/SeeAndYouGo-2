package com.SeeAndYouGo.SeeAndYouGo.rate;

import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RateRepository extends JpaRepository<Rate, Long> {
    List<Rate> findAllByRestaurant(Restaurant restaurant);

    Rate findByRestaurantAndDept(Restaurant restaurant, String dept);

    List<Rate> findByDept(String dishName);

    boolean existsByDept(String name);
}
