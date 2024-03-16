package com.SeeAndYouGo.SeeAndYouGo.Menu;

import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    Menu findByDateAndDeptAndRestaurantAndMenuType(String date, Dept dept, Restaurant restaurant, MenuType menuType);
    boolean existsByDateAndDeptAndRestaurantAndMenuType(String date, Dept dept, Restaurant restaurant, MenuType menuType);

    List<Menu> findByRestaurantAndDate(Restaurant restaurant, String date);

}
