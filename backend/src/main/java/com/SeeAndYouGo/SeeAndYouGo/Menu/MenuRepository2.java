package com.SeeAndYouGo.SeeAndYouGo.Menu;

import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository2 extends JpaRepository<Menu, Long> {

    List<Menu> findByDateAndDeptAndRestaurantAndMenuType(String date, Dept dept, Restaurant restaurant, MenuType menuType);
}
