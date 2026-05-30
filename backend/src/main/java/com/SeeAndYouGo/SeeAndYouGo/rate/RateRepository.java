package com.SeeAndYouGo.SeeAndYouGo.rate;

import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RateRepository extends JpaRepository<Rate, Long> {
    List<Rate> findAllByRestaurant(Restaurant restaurant);

    Optional<Rate> findByRestaurantAndDept(Restaurant restaurant, String dept);

    List<Rate> findByDept(String dishName);

    boolean existsByDept(String name);
}
