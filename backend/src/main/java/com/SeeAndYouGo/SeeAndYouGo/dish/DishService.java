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

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DishService {
    private final DishRepository dishRepository;
    private final MenuService menuService;
    private final MenuDishRepository menuDishRepository;
    @Transactional
    public void updateMainDish(List<MainDishRequestDto> mainDishRequestDtos) {
        log.info("Starting to update main dishes.");
        for (MainDishRequestDto mainDishRequestDto : mainDishRequestDtos) {
            List<String> mainDishNames = mainDishRequestDto.getMainDishList();
            for (String mainDishName : mainDishNames) {
                try {
                    Dish dish = dishRepository.findByName(mainDishName)
                            .orElseThrow(() -> new EntityNotFoundException(mainDishName + "에 해당하는 dish를 찾을 수 없습니다."));
                    dish.updateMainDish();
                } catch (EntityNotFoundException e) {
                    log.error("Error updating main dish: {}", e.getMessage());
                    throw e;
                }
            }
        }
        log.info("Finished updating main dishes.");
    }

    public List<DishResponseDto> getWeeklyDish(LocalDate monday, LocalDate sunday) {
        Set<Dish> dishes = new HashSet<>();

        for (Restaurant restaurant : Restaurant.values()) {
            // 1학생회관은 제외하고 전송
            if(restaurant.equals(Restaurant.제1학생회관)) continue;

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
        log.warn("Attempting to delete dish with ID: {}", id);
        try {
            menuDishRepository.deleteByDishId(id);
            dishRepository.deleteById(id);
            log.info("Successfully deleted dish with ID: {}", id);
        } catch (Exception e) {
            log.error("Error deleting dish with ID: {}", id, e);
            throw new RuntimeException("Error deleting dish with ID: " + id, e);
        }
        return true;
    }

    @Transactional
    public boolean updateDishName(long id, String newName) {
        // 입력 검증
        log.info("Attempting to update dish name for ID: {} to '{}'", id, newName);
        if (newName == null || newName.trim().isEmpty()) {
            log.error("New dish name cannot be empty for ID: {}", id);
            throw new IllegalArgumentException("새로운 요리명은 비어있을 수 없습니다.");
        }

        // 대상 요리 조회 및 존재 확인
        Dish targetDish = dishRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ID " + id + "에 해당하는 요리를 찾을 수 없습니다."));

        // 이미 같은 이름이면 변경 불필요
        if (targetDish.getName().equals(newName.trim())) {
            log.warn("Dish name for ID: {} is already '{}'. No update needed.", id, newName.trim());
            return false;
        }

        // 동일한 이름의 요리가 이미 존재하는 경우
        Optional<Dish> existingDishOpt = dishRepository.findByName(newName.trim());
        if (existingDishOpt.isPresent()) {
            log.warn("Dish with name '{}' already exists. Merging dish ID: {} into existing dish ID: {}.", newName.trim(), id, existingDishOpt.get().getId());
            mergeWithExistingDish(targetDish, existingDishOpt.get());
        } else {
            // 단순 이름 변경
            log.info("Updating dish ID: {} name to '{}'.", id, newName.trim());
            targetDish.updateDishName(newName.trim());
            dishRepository.save(targetDish);
        }
        log.info("Successfully updated dish name for ID: {}.", id);
        return true;
    }

    /**
     * 기존 요리와 병합하는 private 메서드
     */
    private void mergeWithExistingDish(Dish targetDish, Dish existingDish) {
        // 메뉴-요리 연결 정보를 기존 요리로 변경
        log.info("Merging dish {} into {}.", targetDish.getId(), existingDish.getId());
        List<MenuDish> menuDishes = menuDishRepository.findByDish(targetDish);
        menuDishes.forEach(menuDish -> menuDish.setDish(existingDish));
        menuDishRepository.saveAll(menuDishes);

        dishRepository.delete(targetDish);
        log.info("Deleted target dish {} after merging.", targetDish.getId());
    }
}
