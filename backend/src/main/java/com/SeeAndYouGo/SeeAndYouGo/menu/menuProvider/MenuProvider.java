package com.SeeAndYouGo.SeeAndYouGo.menu.menuProvider;

import com.SeeAndYouGo.SeeAndYouGo.menu.Menu;
import com.SeeAndYouGo.SeeAndYouGo.menu.dto.MenuVO;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;

import java.time.LocalDate;
import java.util.List;

public interface MenuProvider {
    List<MenuVO> getWeeklyMenu(Restaurant restaurant, LocalDate monday, LocalDate sunday) throws Exception;
    String getWeeklyMenuToString(LocalDate monday, LocalDate sunday) throws Exception;
}
