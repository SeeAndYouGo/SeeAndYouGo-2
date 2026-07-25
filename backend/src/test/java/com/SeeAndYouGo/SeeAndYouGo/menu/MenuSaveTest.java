package com.SeeAndYouGo.SeeAndYouGo.menu;

import com.SeeAndYouGo.SeeAndYouGo.dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishRepository;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishType;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishVO;
import com.SeeAndYouGo.SeeAndYouGo.menu.dto.MenuVO;
import com.SeeAndYouGo.SeeAndYouGo.menu.mainCache.NewDishCacheService;
import com.SeeAndYouGo.SeeAndYouGo.menu.menuProvider.MenuProvider;
import com.SeeAndYouGo.SeeAndYouGo.menu.menuProvider.MenuProviderFactory;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("메뉴 저장 - 주간 메뉴 중복 방지")
class MenuSaveTest {

    @Mock private DishRepository dishRepository;
    @Mock private MenuRepository menuRepository;
    @Mock private MenuProviderFactory menuProviderFactory;
    @Mock private NewDishCacheService newDishCacheService;
    @Mock private MenuProvider menuProvider;

    @InjectMocks private MenuService menuService;

    @Test
    @DisplayName("비고정 식당의 동일 날짜·부서·식사유형 메뉴는 다시 저장하지 않는다")
    void weekly_doesNotSaveExistingNonFixedMenu() throws Exception {
        LocalDate monday = LocalDate.of(2026, 6, 8);
        LocalDate sunday = LocalDate.of(2026, 6, 14);
        Restaurant restaurant = Restaurant.제3학생회관;

        Menu existingMenu = menu(
                restaurant,
                "2026-06-13",
                Dept.STUDENT,
                MenuType.LUNCH,
                "기존 메뉴"
        );
        MenuVO fetchedMenu = menuVO(
                restaurant,
                "2026-06-13",
                Dept.STUDENT,
                MenuType.LUNCH,
                "새로 조회된 메뉴"
        );

        given(menuProviderFactory.createMenuProvider(restaurant)).willReturn(menuProvider);
        given(menuProvider.getWeeklyMenu(restaurant)).willReturn(List.of(fetchedMenu));
        given(menuRepository.findByRestaurantAndDateBetween(
                restaurant,
                monday.toString(),
                sunday.toString()
        )).willReturn(List.of(existingMenu));

        menuService.saveWeeklyMenu(restaurant, monday, sunday);

        verify(menuRepository, never()).save(org.mockito.ArgumentMatchers.any(Menu.class));
    }

    @Test
    @DisplayName("고정 메뉴 식당은 같은 코너라도 음식이 다르면 각각 저장한다")
    void weekly_preservesDistinctFixedMenus() throws Exception {
        LocalDate monday = LocalDate.of(2026, 6, 8);
        LocalDate sunday = LocalDate.of(2026, 6, 14);
        Restaurant restaurant = Restaurant.제1학생회관;

        Menu existingMenu = menu(
                restaurant,
                "2026-06-08",
                Dept.NOODLE,
                MenuType.LUNCH,
                "라면"
        );
        MenuVO duplicateMenu = menuVO(
                restaurant,
                "2026-06-08",
                Dept.NOODLE,
                MenuType.LUNCH,
                "라면"
        );
        MenuVO distinctMenu = menuVO(
                restaurant,
                "2026-06-08",
                Dept.NOODLE,
                MenuType.LUNCH,
                "김밥"
        );
        Dish kimbap = Dish.builder().name("김밥").dishType(DishType.SIDE).build();

        given(menuProviderFactory.createMenuProvider(restaurant)).willReturn(menuProvider);
        given(menuProvider.getWeeklyMenu(restaurant)).willReturn(List.of(duplicateMenu, distinctMenu));
        given(menuRepository.findByRestaurantAndDateBetween(
                restaurant,
                monday.toString(),
                sunday.toString()
        )).willReturn(List.of(existingMenu));
        given(dishRepository.findByName("김밥")).willReturn(Optional.of(kimbap));

        menuService.saveWeeklyMenu(restaurant, monday, sunday);

        ArgumentCaptor<Menu> menuCaptor = ArgumentCaptor.forClass(Menu.class);
        verify(menuRepository).save(menuCaptor.capture());
        assertThat(menuCaptor.getValue().getDishList())
                .extracting(Dish::getName)
                .containsExactly("김밥");
    }

    private Menu menu(
            Restaurant restaurant,
            String date,
            Dept dept,
            MenuType menuType,
            String dishName
    ) {
        Menu menu = Menu.builder()
                .price(0)
                .date(date)
                .dept(dept)
                .menuType(menuType)
                .restaurant(restaurant)
                .isOpen(true)
                .build();
        menu.addDish(Dish.builder().name(dishName).dishType(DishType.SIDE).build());
        return menu;
    }

    private MenuVO menuVO(
            Restaurant restaurant,
            String date,
            Dept dept,
            MenuType menuType,
            String dishName
    ) {
        MenuVO menuVO = new MenuVO(0, date, dept, restaurant, menuType);
        menuVO.addDishVO(new DishVO(dishName, DishType.SIDE));
        return menuVO;
    }
}
