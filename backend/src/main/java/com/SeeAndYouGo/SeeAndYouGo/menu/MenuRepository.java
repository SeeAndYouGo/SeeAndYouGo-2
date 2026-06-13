package com.SeeAndYouGo.SeeAndYouGo.menu;

import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    Optional<Menu> findByDateAndDeptAndRestaurantAndMenuType(String date, Dept dept, Restaurant restaurant, MenuType menuType);
    boolean existsByDateAndDeptAndRestaurantAndMenuType(String date, Dept dept, Restaurant restaurant, MenuType menuType);

    List<Menu> findByRestaurantAndDate(Restaurant restaurant, String date);

    void deleteByRestaurantAndDate(Restaurant restaurant, String date);

    boolean existsByDate(String string);

    boolean existsByRestaurantAndDate(Restaurant restaurant, String date);

    List<Menu> findByRestaurantAndDateGreaterThanEqual(Restaurant restaurant, String date);

    List<Menu> findByRestaurantAndDateBetween(Restaurant restaurant, String startDate, String endDate);
}
