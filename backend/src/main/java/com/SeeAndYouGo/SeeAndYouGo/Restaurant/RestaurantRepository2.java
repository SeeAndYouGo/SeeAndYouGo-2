package com.SeeAndYouGo.SeeAndYouGo.Restaurant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface RestaurantRepository2 extends JpaRepository<Restaurant, Long> {
    boolean existsByDate(String date);
}
