package com.SeeAndYouGo.SeeAndYouGo.menu.mainCache;

import com.SeeAndYouGo.SeeAndYouGo.dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishRepository;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishType;
import com.SeeAndYouGo.SeeAndYouGo.menu.Dept;
import com.SeeAndYouGo.SeeAndYouGo.menu.Menu;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuRepository;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuType;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Historical Cache 시스템의 DB 쿼리 메서드 테스트
 *
 * 주의: ID 기준 캐싱으로 변경되었으나, Redis 통합 테스트는 별도로 필요
 * 현재 테스트는 MenuRepository.findByRestaurantAndDateBetween() 메서드만 검증
 */
@DataJpaTest
class HistoricalCacheTest {

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private DishRepository dishRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private Dish mainDish;

    @BeforeEach
    void setUp() {
        mainDish = Dish.builder()
                .name("김치찌개")
                .dishType(DishType.MAIN)
                .build();
        dishRepository.save(mainDish);
    }

    @Test
    @DisplayName("findByRestaurantAndDateBetween - 날짜 범위로 메뉴 조회")
    void findByRestaurantAndDateBetween() {
        // given - 3일치 메뉴 생성
        String date1 = LocalDate.now().minusDays(3).format(DATE_FORMATTER);
        String date2 = LocalDate.now().minusDays(2).format(DATE_FORMATTER);
        String date3 = LocalDate.now().minusDays(1).format(DATE_FORMATTER);

        createMenu(Restaurant.제2학생회관, date1);
        createMenu(Restaurant.제2학생회관, date2);
        createMenu(Restaurant.제2학생회관, date3);

        // when - 날짜 범위로 조회
        List<Menu> menus = menuRepository.findByRestaurantAndDateBetween(
                Restaurant.제2학생회관,
                date1,
                date3
        );

        // then - 3개 조회됨
        assertThat(menus).hasSize(3);
        assertThat(menus).extracting(Menu::getDate)
                .containsExactlyInAnyOrder(date1, date2, date3);
    }

    @Test
    @DisplayName("findByRestaurantAndDateBetween - 레스토랑 필터링 확인")
    void filterByRestaurant() {
        // given
        String date = LocalDate.now().minusDays(1).format(DATE_FORMATTER);
        createMenu(Restaurant.제2학생회관, date);
        createMenu(Restaurant.제3학생회관, date);

        // when
        List<Menu> menus = menuRepository.findByRestaurantAndDateBetween(
                Restaurant.제2학생회관,
                date,
                date
        );

        // then - 2학생회관만 조회
        assertThat(menus).hasSize(1);
        assertThat(menus.get(0).getRestaurant()).isEqualTo(Restaurant.제2학생회관);
    }

    @Test
    @DisplayName("findByRestaurantAndDateBetween - 범위 밖 데이터는 조회 안됨")
    void outOfRangeNotIncluded() {
        // given
        String beforeRange = LocalDate.now().minusDays(10).format(DATE_FORMATTER);
        String inRange = LocalDate.now().minusDays(5).format(DATE_FORMATTER);
        String afterRange = LocalDate.now().minusDays(1).format(DATE_FORMATTER);

        createMenu(Restaurant.제2학생회관, beforeRange);
        createMenu(Restaurant.제2학생회관, inRange);
        createMenu(Restaurant.제2학생회관, afterRange);

        // when - inRange 날짜만 조회
        List<Menu> menus = menuRepository.findByRestaurantAndDateBetween(
                Restaurant.제2학생회관,
                inRange,
                inRange
        );

        // then - 1개만 조회됨
        assertThat(menus).hasSize(1);
        assertThat(menus.get(0).getDate()).isEqualTo(inRange);
    }

    @Test
    @DisplayName("findByRestaurantAndDateBetween - 빈 결과")
    void emptyResult() {
        // given - 다른 레스토랑만 존재
        String date = LocalDate.now().minusDays(1).format(DATE_FORMATTER);
        createMenu(Restaurant.제3학생회관, date);

        // when - 2학생회관 조회
        List<Menu> menus = menuRepository.findByRestaurantAndDateBetween(
                Restaurant.제2학생회관,
                date,
                date
        );

        // then - 빈 결과
        assertThat(menus).isEmpty();
    }

    @Test
    @DisplayName("findByRestaurantAndDateBetween - 대량 데이터 조회 (N+1 방지)")
    void bulkDataQuery() {
        // given - 10일치 메뉴 생성
        LocalDate startDate = LocalDate.now().minusDays(10);
        for (int i = 0; i < 10; i++) {
            String date = startDate.plusDays(i).format(DATE_FORMATTER);
            createMenu(Restaurant.제2학생회관, date);
        }

        // when - 한 번의 쿼리로 10일치 조회
        List<Menu> menus = menuRepository.findByRestaurantAndDateBetween(
                Restaurant.제2학생회관,
                startDate.format(DATE_FORMATTER),
                startDate.plusDays(9).format(DATE_FORMATTER)
        );

        // then - 10개 조회됨
        assertThat(menus).hasSize(10);
    }

    @Test
    @DisplayName("findByIdIn - ID 리스트로 Dish 조회 (신메뉴 이름 변환용)")
    void findDishByIdList() {
        // given - 여러 Dish 생성
        Dish dish1 = Dish.builder()
                .name("김치찌개")
                .dishType(DishType.MAIN)
                .build();
        Dish dish2 = Dish.builder()
                .name("제육볶음")
                .dishType(DishType.MAIN)
                .build();
        Dish dish3 = Dish.builder()
                .name("된장찌개")
                .dishType(DishType.MAIN)
                .build();

        dishRepository.save(dish1);
        dishRepository.save(dish2);
        dishRepository.save(dish3);

        // when - ID 리스트로 조회
        List<Long> ids = List.of(dish1.getId(), dish3.getId());
        List<Dish> dishes = dishRepository.findByIdIn(ids);

        // then - 정확히 2개 조회되고, 이름이 일치
        assertThat(dishes).hasSize(2);
        assertThat(dishes).extracting(Dish::getName)
                .containsExactlyInAnyOrder("김치찌개", "된장찌개");
    }

    @Test
    @DisplayName("findByIdIn - 빈 ID 리스트")
    void findDishByEmptyIdList() {
        // when - 빈 리스트로 조회
        List<Dish> dishes = dishRepository.findByIdIn(List.of());

        // then - 빈 결과
        assertThat(dishes).isEmpty();
    }

    private void createMenu(Restaurant restaurant, String date) {
        Menu menu = Menu.builder()
                .price(6000)
                .date(date)
                .dept(Dept.STUDENT)
                .menuType(MenuType.LUNCH)
                .restaurant(restaurant)
                .isOpen(true)
                .build();
        menu.addDish(mainDish);
        menuRepository.save(menu);
    }
}
