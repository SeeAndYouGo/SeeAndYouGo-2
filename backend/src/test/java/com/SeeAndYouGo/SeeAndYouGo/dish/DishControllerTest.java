package com.SeeAndYouGo.SeeAndYouGo.dish;

import com.SeeAndYouGo.SeeAndYouGo.menu.Dept;
import com.SeeAndYouGo.SeeAndYouGo.menu.Menu;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuRepository;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuType;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Transactional
public class DishControllerTest {
    @Autowired
    private DishController dishController;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private MenuRepository menuRepository;

    @BeforeEach
    public void init(){
        // dish 정보를 미리 세팅한다.
        // 제2학생회관의 정보를 미리 세팅.
        initSetting();
    }

    @DisplayName("메인메뉴 업데이트")
    @Test
    void 메인메뉴_업데이트() throws Exception {
        // given
        List<MainDishRequestDto> mainDishRequestDtos = getMainDishRequestDtos();

        // when
        dishController.updateMainDish(mainDishRequestDtos);

        // then
        Assert.assertEquals(dishRepository.findByName("햄버거").getDishType(), DishType.MAIN);
        Assert.assertEquals(dishRepository.findByName("감자튀김").getDishType(), DishType.SIDE);
        Assert.assertEquals(dishRepository.findByName("김치찌개").getDishType(), DishType.MAIN);
        Assert.assertEquals(dishRepository.findByName("현미밥").getDishType(), DishType.SIDE);
    }



    private void initSetting() {
        Dish 김치찌개 = Dish.builder()
                .name("김치찌개")
                .dishType(DishType.SIDE)
                .build();

        Dish 현미밥 = Dish.builder()
                .name("현미밥")
                .dishType(DishType.SIDE)
                .build();

        Menu menu = Menu.builder()
                .price(4000)
                .date("2024-10-22")
                .dept(Dept.STUDENT)
                .menuType(MenuType.BREAKFAST)
                .restaurant(Restaurant.제2학생회관)
                .build();

        Dish 감자튀김 = Dish.builder()
                .name("감자튀김")
                .dishType(DishType.MAIN)
                .build();

        Dish 햄버거 = Dish.builder()
                .name("햄버거")
                .dishType(DishType.SIDE)
                .build();

        Menu menu1 = Menu.builder()
                .price(6000)
                .date("2024-10-22")
                .dept(Dept.STUDENT)
                .menuType(MenuType.DINNER)
                .restaurant(Restaurant.상록회관)
                .build();

        List<Dish> dishes = new ArrayList<>();
        dishes.add(김치찌개);
        dishes.add(현미밥);

        List<Dish> dishes1 = new ArrayList<>();
        dishes1.add(감자튀김);
        dishes1.add(햄버거);

        menu.setDishList(dishes);
        menu1.setDishList(dishes1);

        dishRepository.saveAll(dishes);
        dishRepository.saveAll(dishes1);
        menuRepository.save(menu);
        menuRepository.save(menu1);
    }

    private List<MainDishRequestDto> getMainDishRequestDtos() {
        List<String> mainDishes1 = new ArrayList<>();
        mainDishes1.add("김치찌개");
        List<String> subDishes1 = new ArrayList<>();
        subDishes1.add("현미밥");

        MainDishRequestDto mainDishRequestDto = new MainDishRequestDto("제2학생회관", "STAFF", "2024-10-22", mainDishes1, subDishes1);

        List<String> mainDishes2 = new ArrayList<>();
        mainDishes2.add("햄버거");
        List<String> subDishes2 = new ArrayList<>();
        subDishes2.add("감자튀김");

        MainDishRequestDto mainDishRequestDto1 = new MainDishRequestDto("상록회관", "STUDENT", "2024-10-22", mainDishes2, subDishes2);

        List<MainDishRequestDto> mainDishRequestDtos = new ArrayList<>();
        mainDishRequestDtos.add(mainDishRequestDto);
        mainDishRequestDtos.add(mainDishRequestDto1);
        return mainDishRequestDtos;
    }
}
