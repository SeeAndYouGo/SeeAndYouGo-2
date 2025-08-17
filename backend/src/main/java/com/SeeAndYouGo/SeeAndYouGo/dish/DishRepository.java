package com.SeeAndYouGo.SeeAndYouGo.dish;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DishRepository extends JpaRepository<Dish, Long> {
    Optional<Dish> findByName(String name);
    boolean existsByName(String name);
}
