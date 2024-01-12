package com.SeeAndYouGo.SeeAndYouGo.Dish;

import com.SeeAndYouGo.SeeAndYouGo.Menu.Dept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DishRepository extends JpaRepository<Dish, Long> {

//    Dish findByRestaurant_NameAndNameAndDeptAndDate(String restaurantName, String mainDishName, Dept dept, String date);
    Dish findByName(String name);

}
