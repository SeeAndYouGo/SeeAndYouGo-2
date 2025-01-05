package com.SeeAndYouGo.SeeAndYouGo.dish;

import com.SeeAndYouGo.SeeAndYouGo.menu.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DishService {
    private final DishRepository dishRepository;
    private final MenuService menuService;



    @Transactional
    public void updateMainDish(List<MainDishRequestDto> mainDishRequestDtos) {

        for (MainDishRequestDto mainDishRequestDto : mainDishRequestDtos) {
            List<String> mainDishNames = mainDishRequestDto.getMainDishList();

            for (String mainDishName : mainDishNames) {
                Dish dish = dishRepository.findByName(mainDishName);
                dish.updateMainDish();
            }

            for (String sideDishName : mainDishRequestDto.getSideDishList()) {
                Dish sideDish = dishRepository.findByName(sideDishName);
                sideDish.updateSideDish();
            }
        }
    }

}