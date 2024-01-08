package com.SeeAndYouGo.SeeAndYouGo.Restaurant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    boolean existsByDate(String date);
    Long countByNameAndDate(String name, String date);
    List<Restaurant> findByNameAndDate(String name, String date);
}
