package com.SeeAndYouGo.SeeAndYouGo.dish;

import com.SeeAndYouGo.SeeAndYouGo.dish.dto.DishResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.menu.*;
import com.SeeAndYouGo.SeeAndYouGo.menuDish.MenuDish;
import com.SeeAndYouGo.SeeAndYouGo.menuDish.MenuDishRepository;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class DishService {
    private final DishRepository dishRepository;
    private final MenuService menuService;
    private final MenuDishRepository menuDishRepository;
    @Transactional
    public void updateMainDish(List<MainDishRequestDto> mainDishRequestDtos) {

        for (MainDishRequestDto mainDishRequestDto : mainDishRequestDtos) {
            List<String> mainDishNames = mainDishRequestDto.getMainDishList();

            for (String mainDishName : mainDishNames) {
                Optional<Dish> dish = dishRepository.findByName(mainDishName);

                if (dish.isPresent()) {
                    dish.get().updateMainDish();
                }else{
                    throw new EntityNotFoundException(mainDishName+"에 해당하는 dish를 찾을 수 없습니다.");
                }

            }

            // for (String sideDishName : mainDishRequestDto.getSideDishList()) {
            //     Dish sideDish = dishRepository.findByName(sideDishName);
            //     sideDish.updateSideDish();
            // }
        }
    }

    public List<DishResponseDto> getWeeklyDish(LocalDate monday, LocalDate sunday) {
        Set<Dish> dishes = new HashSet<>();

        for (Restaurant restaurant : Restaurant.getNonFixedMenuRestaurant()) {
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

    @Transactional
    public boolean deleteDish(Long id) {
        try{
            menuDishRepository.deleteByDishId(id);
            dishRepository.deleteById(id);
        }catch (Exception e){
            log.error("Failed to delete dish with id: {}", id, e);
            return false;
        }

        return true;
    }

    @Transactional
    public boolean updateDishName(long id, String newName) {
        // 입력 검증
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("새로운 요리명은 비어있을 수 없습니다.");
        }

        // 대상 요리 조회 및 존재 확인
        Dish targetDish = dishRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ID " + id + "에 해당하는 요리를 찾을 수 없습니다."));

        // 이미 같은 이름이면 변경 불필요
        if (targetDish.getName().equals(newName.trim())) {
            return false;
        }

        // 동일한 이름의 요리가 이미 존재하는 경우
        Optional<Dish> existingDish = dishRepository.findByName(newName.trim());
        if (existingDish.isPresent()) {
            mergeWithExistingDish(targetDish, existingDish.get());
        } else {
            // 단순 이름 변경
            targetDish.updateDishName(newName.trim());
            dishRepository.save(targetDish);
        }

        return true;
    }

    /**
     * 기존 요리와 병합하는 private 메서드
     */
    private void mergeWithExistingDish(Dish targetDish, Dish existingDish) {
        // 메뉴-요리 연결 정보를 기존 요리로 변경
        List<MenuDish> menuDishes = menuDishRepository.findByDish(targetDish);
        menuDishes.forEach(menuDish -> menuDish.setDish(existingDish));
        menuDishRepository.saveAll(menuDishes);

        // 대상 요리 삭제
        dishRepository.delete(targetDish);
    }
}
