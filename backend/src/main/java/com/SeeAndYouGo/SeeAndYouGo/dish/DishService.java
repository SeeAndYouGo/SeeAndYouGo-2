package com.SeeAndYouGo.SeeAndYouGo.dish;

import com.SeeAndYouGo.SeeAndYouGo.dish.dto.DishResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.menu.*;
import com.SeeAndYouGo.SeeAndYouGo.menuDish.MenuDishRepository;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DishService {
    private final DishRepository dishRepository;
    private final MenuService menuService;
    private final MenuDishRepository menuDishRepository;
    @Transactional
    public void updateMainDish(List<MainDishRequestDto> mainDishRequestDtos) {

        for (MainDishRequestDto mainDishRequestDto : mainDishRequestDtos) {
            List<String> mainDishNames = mainDishRequestDto.getMainDishList();

            for (String mainDishName : mainDishNames) {
                Dish dish = dishRepository.findByName(mainDishName);
                dish.updateMainDish();
            }

            // for (String sideDishName : mainDishRequestDto.getSideDishList()) {
            //     Dish sideDish = dishRepository.findByName(sideDishName);
            //     sideDish.updateSideDish();
            // }
        }
    }

    public List<DishResponseDto> getWeeklyDish(LocalDate monday, LocalDate sunday) {
        Set<Dish> dishes = new HashSet<>();

        for (Restaurant restaurant : Restaurant.values()) {
            Set<Dish> dishList = getWeeklyDishByRestaurant(restaurant, monday, sunday);
            dishes.addAll(dishList);
        }

        return convertWeeklyDishDto(dishes);
    }

    private List<DishResponseDto> convertWeeklyDishDto(Set<Dish> dishes) {
        return dishes.stream()
                .map(dish -> new DishResponseDto(
                        dish.getId(),
                        dish.getName()
                ))
                .collect(Collectors.toList());
    }

    private Set<Dish> getWeeklyDishByRestaurant(Restaurant restaurant, LocalDate monday, LocalDate sunday) {
        List<Menu>[] menus = menuService.getOneWeekRestaurantMenu(restaurant.name(), monday.toString());

        Set<Dish> dishes = new HashSet<>();
        for (List<Menu> menuByRestaurant : menus) {
            for (Menu menu : menuByRestaurant) {
                dishes.addAll(menu.getDishList());
            }
        }

        return dishes;
    }

    public boolean duplicateDishName(String name) {
        return dishRepository.existsByName(name);
    }

    public boolean deleteDish(Long id) {
        try{
            menuDishRepository.deleteByDishId(id);
            dishRepository.deleteById(id);
        }catch (Exception e){
            return false;
        }

        return true;
    }
}
