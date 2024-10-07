package com.SeeAndYouGo.SeeAndYouGo.MenuDish;

import com.SeeAndYouGo.SeeAndYouGo.Dish.Dish;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuDishRepository extends JpaRepository<MenuDish, Long> {
    List<MenuDish> findByDish(Dish dish);
}
