package com.SeeAndYouGo.SeeAndYouGo.dish;

import com.SeeAndYouGo.SeeAndYouGo.TestSetUp;
import com.SeeAndYouGo.SeeAndYouGo.menu.Dept;
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

import java.time.LocalDate;
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

    private final String menu1Dish1Name = "김치찌개";
    private final String menu1Dish2Name = "현미밥";
    private final Integer menu1Price = 4000;
    private final LocalDate menu1Date = LocalDate.now();
    private final Dept menu1Dept = Dept.STAFF;
    private final MenuType menu1MenuType = MenuType.BREAKFAST;
    private final Restaurant menu1Restaurant = Restaurant.제2학생회관;

    private final String menu2Dish1Name = "햄버거";
    private final String menu2Dish2Name = "감자튀김";
    private final Integer menu2Price = 6000;
    private final LocalDate menu2Date = LocalDate.now();
    private final Dept menu2Dept = Dept.STUDENT;
    private final MenuType menu2MenuType = MenuType.DINNER;
    private final Restaurant menu2Restaurant = Restaurant.상록회관;

    @BeforeEach
    public void init(){
        // dish 정보를 미리 세팅한다.
        // 제2학생회관의 정보를 미리 세팅.
        Dish 김치찌개 = TestSetUp.saveDish(dishRepository, menu1Dish1Name, DishType.SIDE);
        Dish 현미밥 = TestSetUp.saveDish(dishRepository, menu1Dish2Name, DishType.SIDE);

        TestSetUp.saveMenu(menuRepository, menu1Price, menu1Date, menu1Dept, menu1MenuType, menu1Restaurant, 김치찌개, 현미밥);

        Dish 감자튀김 = TestSetUp.saveDish(dishRepository, menu2Dish1Name, DishType.MAIN);
        Dish 햄버거 = TestSetUp.saveDish(dishRepository, menu2Dish2Name, DishType.SIDE);

        TestSetUp.saveMenu(menuRepository, menu2Price, menu2Date, menu2Dept, menu2MenuType, menu2Restaurant, 감자튀김, 햄버거);
    }

    @DisplayName("메인메뉴 업데이트")
    @Test
    void 메인메뉴_업데이트() throws Exception {
        // given
        List<MainDishRequestDto> mainDishRequestDtos = getMainDishRequestDtos();

        // when
        dishController.updateMainDish(mainDishRequestDtos);

        // then
        Assert.assertEquals(dishRepository.findByName(menu1Dish1Name).getDishType(), DishType.MAIN);
        Assert.assertEquals(dishRepository.findByName(menu1Dish2Name).getDishType(), DishType.SIDE);

        Assert.assertEquals(dishRepository.findByName(menu2Dish1Name).getDishType(), DishType.MAIN);
        Assert.assertEquals(dishRepository.findByName(menu2Dish2Name).getDishType(), DishType.SIDE);
    }

    private List<MainDishRequestDto> getMainDishRequestDtos() {
        List<String> mainDishes1 = new ArrayList<>();
        mainDishes1.add(menu1Dish1Name);
        List<String> sideDishes1 = new ArrayList<>();
        sideDishes1.add(menu1Dish2Name);

        MainDishRequestDto mainDishRequestDto = new MainDishRequestDto(menu1Restaurant.toString(), menu1Dept.toString(), menu1Date.toString(), mainDishes1, sideDishes1);

        List<String> mainDishes2 = new ArrayList<>();
        mainDishes2.add(menu2Dish1Name);
        List<String> subDishes2 = new ArrayList<>();
        subDishes2.add(menu2Dish2Name);

        MainDishRequestDto mainDishRequestDto1 = new MainDishRequestDto(menu2Restaurant.toString(), menu2Dept.toString(), menu2Date.toString(), mainDishes2, subDishes2);

        List<MainDishRequestDto> mainDishRequestDtos = new ArrayList<>();
        mainDishRequestDtos.add(mainDishRequestDto);
        mainDishRequestDtos.add(mainDishRequestDto1);
        return mainDishRequestDtos;
    }
}
