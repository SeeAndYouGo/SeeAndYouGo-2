package com.SeeAndYouGo.SeeAndYouGo.menu;

import com.SeeAndYouGo.SeeAndYouGo.dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishRepository;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishType;
import com.SeeAndYouGo.SeeAndYouGo.menu.mainCache.NewDishCacheService;
import com.SeeAndYouGo.SeeAndYouGo.menu.menuProvider.MenuProviderFactory;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * 테스트 목표: 일별 / 주간 메뉴 조회 동작 검증.
 *
 *  [일별]
 *  - getOneDayRestaurantMenu: 식당명+날짜 → menuRepository 조회 후 메인 디시가 가장 앞에 오도록 정렬.
 *  - 식당 번호("2") 같은 축약 입력도 Restaurant.parseName 으로 정상 처리.
 *  - 잘못된 식당명은 IllegalArgumentException.
 *
 *  [주간]
 *  - getOneWeekRestaurantMenu: 입력 일자가 속한 주의 월~일 7일치를 인덱스 [0..6] 배열로 반환.
 *  - 주중 어느 요일(월/목/일)을 받든 같은 주의 월~일 범위를 조회.
 *  - 각 일자의 메뉴가 해당 인덱스(월=0 .. 일=6)에 들어간다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("메뉴 조회 - MenuService 일별/주간")
class MenuFetchTest {

    @Mock private DishRepository dishRepository;
    @Mock private MenuRepository menuRepository;
    @Mock private MenuProviderFactory menuProviderFactory;
    @Mock private NewDishCacheService newDishCacheService;

    @InjectMocks private MenuService menuService;

    // ===== 일별 메뉴 =====

    @Test
    @DisplayName("일별: 식당+날짜로 조회한 메뉴는 MAIN 디시가 가장 앞으로 정렬되어 반환된다")
    void daily_sortsMainDishFirst() {
        // given - menuDishes 에 SIDE 가 먼저 들어간 메뉴
        Menu menu = Menu.builder()
                .price(6000).date("2025-11-03").dept(Dept.STUDENT)
                .menuType(MenuType.LUNCH).restaurant(Restaurant.제2학생회관).isOpen(true)
                .build();
        Dish side = Dish.builder().name("김").dishType(DishType.SIDE).build();
        Dish main = Dish.builder().name("김치찌개").dishType(DishType.MAIN).build();
        menu.addDish(side);
        menu.addDish(main);
        // 초기 상태 확인 — SIDE 가 0번째
        assertThat(menu.getDishList().get(0).getDishType()).isEqualTo(DishType.SIDE);

        given(menuRepository.findByRestaurantAndDate(Restaurant.제2학생회관, "2025-11-03"))
                .willReturn(List.of(menu));

        // when
        List<Menu> result = menuService.getOneDayRestaurantMenu("제2학생회관", "2025-11-03");

        // then - 정렬 후 MAIN 이 맨 앞
        assertThat(result).hasSize(1);
        List<Dish> dishes = result.get(0).getDishList();
        assertThat(dishes.get(0).getDishType()).isEqualTo(DishType.MAIN);
        assertThat(dishes.get(0).getName()).isEqualTo("김치찌개");
        assertThat(dishes).extracting(Dish::getName).containsExactly("김치찌개", "김");
    }

    @Test
    @DisplayName("일별: 식당 번호(\"2\") 만으로도 정상 조회된다")
    void daily_acceptsNumericRestaurantName() {
        // given
        given(menuRepository.findByRestaurantAndDate(Restaurant.제2학생회관, "2025-11-03"))
                .willReturn(Collections.emptyList());

        // when
        menuService.getOneDayRestaurantMenu("2", "2025-11-03");

        // then - 파싱된 enum 으로 조회됨
        verify(menuRepository).findByRestaurantAndDate(Restaurant.제2학생회관, "2025-11-03");
    }

    @Test
    @DisplayName("일별: 해당 날짜 메뉴가 없으면 빈 리스트")
    void daily_emptyWhenNoMenu() {
        given(menuRepository.findByRestaurantAndDate(Restaurant.생활과학대, "2025-11-03"))
                .willReturn(Collections.emptyList());

        List<Menu> result = menuService.getOneDayRestaurantMenu("생활과학대", "2025-11-03");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("일별: 존재하지 않는 식당명은 IllegalArgumentException 으로 거부된다")
    void daily_invalidRestaurant() {
        assertThatThrownBy(() -> menuService.getOneDayRestaurantMenu("없는식당", "2025-11-03"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ===== 주간 메뉴 =====

    @Test
    @DisplayName("주간: 입력일(월요일 2025-11-03)이 속한 주의 월~일 7일치를 순서대로 조회한다")
    void weekly_returnsMonToSun_whenInputIsMonday() {
        // given - 2025-11-03 = 월요일
        given(menuRepository.findByRestaurantAndDate(any(Restaurant.class), anyString()))
                .willReturn(Collections.emptyList());

        // when
        List<Menu>[] result = menuService.getOneWeekRestaurantMenu("제2학생회관", "2025-11-03");

        // then - 7개 배열로 반환
        assertThat(result).hasSize(7);

        // 호출 순서대로 월~일 날짜 검증
        ArgumentCaptor<String> dateCaptor = ArgumentCaptor.forClass(String.class);
        verify(menuRepository, times(7))
                .findByRestaurantAndDate(eq(Restaurant.제2학생회관), dateCaptor.capture());

        assertThat(dateCaptor.getAllValues()).containsExactly(
                "2025-11-03", "2025-11-04", "2025-11-05", "2025-11-06",
                "2025-11-07", "2025-11-08", "2025-11-09"
        );
    }

    @Test
    @DisplayName("주간: 입력일이 주중(목요일 2025-11-06)이어도 같은 주의 월~일을 조회한다")
    void weekly_returnsMonToSun_whenInputIsThursday() {
        // given - 2025-11-06 = 목요일
        given(menuRepository.findByRestaurantAndDate(any(Restaurant.class), anyString()))
                .willReturn(Collections.emptyList());

        // when
        menuService.getOneWeekRestaurantMenu("제2학생회관", "2025-11-06");

        // then - 같은 주의 월요일/일요일 모두 호출됨
        verify(menuRepository).findByRestaurantAndDate(Restaurant.제2학생회관, "2025-11-03"); // 월
        verify(menuRepository).findByRestaurantAndDate(Restaurant.제2학생회관, "2025-11-09"); // 일
        verify(menuRepository, times(7))
                .findByRestaurantAndDate(eq(Restaurant.제2학생회관), anyString());
    }

    @Test
    @DisplayName("주간: 입력일이 일요일이어도 같은 주의 월~일을 조회한다 (월~일 기준)")
    void weekly_returnsMonToSun_whenInputIsSunday() {
        // given - 2025-11-09 = 일요일
        given(menuRepository.findByRestaurantAndDate(any(Restaurant.class), anyString()))
                .willReturn(Collections.emptyList());

        // when
        List<Menu>[] result = menuService.getOneWeekRestaurantMenu("제2학생회관", "2025-11-09");

        // then
        assertThat(result).hasSize(7);
        verify(menuRepository).findByRestaurantAndDate(Restaurant.제2학생회관, "2025-11-03"); // 월
        verify(menuRepository).findByRestaurantAndDate(Restaurant.제2학생회관, "2025-11-09"); // 일
    }

    @Test
    @DisplayName("주간: 각 일자의 메뉴가 해당 인덱스(월=0, 금=4)에 담겨 반환된다")
    void weekly_resultsArrangedByDayIndex() {
        // given
        Menu monMenu = sampleMenu("2025-11-03");
        Menu friMenu = sampleMenu("2025-11-07");

        given(menuRepository.findByRestaurantAndDate(any(Restaurant.class), anyString()))
                .willReturn(Collections.emptyList());
        given(menuRepository.findByRestaurantAndDate(Restaurant.제2학생회관, "2025-11-03"))
                .willReturn(List.of(monMenu));
        given(menuRepository.findByRestaurantAndDate(Restaurant.제2학생회관, "2025-11-07"))
                .willReturn(List.of(friMenu));

        // when
        List<Menu>[] result = menuService.getOneWeekRestaurantMenu("제2학생회관", "2025-11-03");

        // then
        assertThat(result[0]).containsExactly(monMenu); // 월
        assertThat(result[1]).isEmpty();                 // 화
        assertThat(result[2]).isEmpty();                 // 수
        assertThat(result[3]).isEmpty();                 // 목
        assertThat(result[4]).containsExactly(friMenu); // 금
        assertThat(result[5]).isEmpty();                 // 토
        assertThat(result[6]).isEmpty();                 // 일
    }

    @Test
    @DisplayName("주간: 식당 번호로 조회해도 정상 동작한다 (\"3\" → 제3학생회관)")
    void weekly_acceptsNumericRestaurantName() {
        given(menuRepository.findByRestaurantAndDate(any(Restaurant.class), anyString()))
                .willReturn(Collections.emptyList());

        menuService.getOneWeekRestaurantMenu("3", "2025-11-03");

        verify(menuRepository, times(7))
                .findByRestaurantAndDate(eq(Restaurant.제3학생회관), anyString());
    }

    // ===== helpers =====

    private Menu sampleMenu(String date) {
        return Menu.builder()
                .price(6000).date(date).dept(Dept.STUDENT)
                .menuType(MenuType.LUNCH).restaurant(Restaurant.제2학생회관).isOpen(true)
                .build();
    }
}
