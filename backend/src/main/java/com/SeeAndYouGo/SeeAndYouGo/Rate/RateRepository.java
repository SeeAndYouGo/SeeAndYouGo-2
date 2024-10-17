package com.SeeAndYouGo.SeeAndYouGo.Rate;

import com.SeeAndYouGo.SeeAndYouGo.Menu.Dept;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RateRepository extends JpaRepository<Rate, Long> {
    List<Rate> findAllByRestaurant(Restaurant restaurant);

    Rate findByRestaurantAndDept(Restaurant restaurant, Dept dept);
}
