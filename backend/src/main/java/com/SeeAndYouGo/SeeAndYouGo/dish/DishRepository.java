package com.SeeAndYouGo.SeeAndYouGo.dish;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DishRepository extends JpaRepository<Dish, Long> {
    Dish findByName(String name);
    boolean existsByName(String name);
}
