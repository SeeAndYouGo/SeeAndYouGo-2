package com.SeeAndYouGo.SeeAndYouGo.menuDish;

import com.SeeAndYouGo.SeeAndYouGo.dish.Dish;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuDishRepository extends JpaRepository<MenuDish, Long> {
    List<MenuDish> findByDish(Dish dish);
}
