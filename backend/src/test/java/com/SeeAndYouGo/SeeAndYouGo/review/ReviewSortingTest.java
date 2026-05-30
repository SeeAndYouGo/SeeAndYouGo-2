package com.SeeAndYouGo.SeeAndYouGo.review;

import com.SeeAndYouGo.SeeAndYouGo.dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishRepository;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishType;
import com.SeeAndYouGo.SeeAndYouGo.menu.Dept;
import com.SeeAndYouGo.SeeAndYouGo.menu.Menu;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuRepository;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuType;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReviewSortingTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private DishRepository dishRepository;

    private Menu testMenu;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @BeforeEach
    void setUp() {
        // 테스트용 Dish 생성
        Dish mainDish = Dish.builder()
                .name("김치찌개")
                .dishType(DishType.MAIN)
                .build();
        dishRepository.save(mainDish);

        // 테스트용 Menu 생성
        testMenu = Menu.builder()
                .price(6000)
                .date("2025-11-01")
                .dept(Dept.STUDENT)
                .menuType(MenuType.LUNCH)
                .restaurant(Restaurant.제2학생회관)
                .isOpen(true)
                .build();
        testMenu.addDish(mainDish);
        menuRepository.save(testMenu);
    }

    @Test
    @DisplayName("findByRestaurantOrderByMadeTimeDesc - 실제 DB 정렬 검증")
    void findByRestaurantOrderByMadeTimeDesc() {
        // given - 시간 순서대로 리뷰 3개 저장
        Review oldReview = createAndSaveReview("2025-11-01 10:00:00");
        Review middleReview = createAndSaveReview("2025-11-01 12:00:00");
        Review recentReview = createAndSaveReview("2025-11-01 14:00:00");

        // when - OrderByMadeTimeDesc로 조회
        List<Review> reviews = reviewRepository.findByRestaurantOrderByMadeTimeDesc(Restaurant.제2학생회관);

        // then - 최신 순서대로 정렬되어야 함
        assertThat(reviews).hasSize(3);
        assertThat(reviews.get(0).getId()).isEqualTo(recentReview.getId());
        assertThat(reviews.get(1).getId()).isEqualTo(middleReview.getId());
        assertThat(reviews.get(2).getId()).isEqualTo(oldReview.getId());

        // 실제 시간도 검증
        LocalDateTime firstTime = LocalDateTime.parse(reviews.get(0).getMadeTime(), FORMATTER);
        LocalDateTime secondTime = LocalDateTime.parse(reviews.get(1).getMadeTime(), FORMATTER);
        LocalDateTime thirdTime = LocalDateTime.parse(reviews.get(2).getMadeTime(), FORMATTER);

        assertThat(firstTime).isAfter(secondTime);
        assertThat(secondTime).isAfter(thirdTime);
    }

    @Test
    @DisplayName("findByRestaurantAndMenuInOrderByMadeTimeDesc - 실제 DB 정렬 검증")
    void findByRestaurantAndMenuInOrderByMadeTimeDesc() {
        // given
        Review oldReview = createAndSaveReview("2025-11-01 09:00:00");
        Review middleReview = createAndSaveReview("2025-11-01 11:00:00");
        Review recentReview = createAndSaveReview("2025-11-01 13:00:00");

        // when
        List<Review> reviews = reviewRepository.findByRestaurantAndMenuInOrderByMadeTimeDesc(
                Restaurant.제2학생회관,
                List.of(testMenu)
        );

        // then - 최신순 정렬 확인
        assertThat(reviews).hasSize(3);
        assertThat(reviews.get(0).getId()).isEqualTo(recentReview.getId());
        assertThat(reviews.get(1).getId()).isEqualTo(middleReview.getId());
        assertThat(reviews.get(2).getId()).isEqualTo(oldReview.getId());
    }

    @Test
    @DisplayName("기존 메서드 vs OrderByMadeTimeDesc - 정렬 차이 확인")
    void compareWithAndWithoutOrdering() {
        // given
        createAndSaveReview("2025-11-01 10:00:00");
        createAndSaveReview("2025-11-01 12:00:00");
        createAndSaveReview("2025-11-01 14:00:00");

        // when - 정렬 없는 메서드
        List<Review> unorderedReviews = reviewRepository.findByRestaurant(Restaurant.제2학생회관);

        // when - 정렬 있는 메서드
        List<Review> orderedReviews = reviewRepository.findByRestaurantOrderByMadeTimeDesc(Restaurant.제2학생회관);

        // then - 개수는 같음
        assertThat(unorderedReviews).hasSameSizeAs(orderedReviews);

        // then - OrderByMadeTimeDesc는 최신이 먼저
        LocalDateTime firstTime = LocalDateTime.parse(orderedReviews.get(0).getMadeTime(), FORMATTER);
        LocalDateTime lastTime = LocalDateTime.parse(orderedReviews.get(orderedReviews.size() - 1).getMadeTime(), FORMATTER);
        assertThat(firstTime).isAfter(lastTime);
    }

    @Test
    @DisplayName("동일 시간 리뷰도 정상 조회")
    void handleSameTimeReviews() {
        // given - 같은 시간에 작성된 리뷰 2개
        String sameTime = "2025-11-01 12:00:00";
        createAndSaveReview(sameTime);
        createAndSaveReview(sameTime);

        // when
        List<Review> reviews = reviewRepository.findByRestaurantOrderByMadeTimeDesc(Restaurant.제2학생회관);

        // then - 둘 다 조회됨
        assertThat(reviews).hasSize(2);
    }

    @Test
    @DisplayName("다른 레스토랑 필터링 확인")
    void filterByRestaurant() {
        // given - 2학생회관 리뷰
        createAndSaveReview("2025-11-01 12:00:00");

        // given - 3학생회관 메뉴와 리뷰 생성
        Menu menu3 = Menu.builder()
                .price(6000)
                .date("2025-11-01")
                .dept(Dept.STUDENT)
                .menuType(MenuType.LUNCH)
                .restaurant(Restaurant.제3학생회관)
                .isOpen(true)
                .build();
        menuRepository.save(menu3);

        Review review3 = createReview(menu3, "2025-11-01 12:00:00");
        reviewRepository.save(review3);

        // when - 2학생회관만 조회
        List<Review> reviews = reviewRepository.findByRestaurantOrderByMadeTimeDesc(Restaurant.제2학생회관);

        // then - 2학생회관 리뷰만 조회됨
        assertThat(reviews).hasSize(1);
        assertThat(reviews.get(0).getRestaurant()).isEqualTo(Restaurant.제2학생회관);
    }

    private Review createAndSaveReview(String madeTime) {
        Review review = createReview(testMenu, madeTime);
        return reviewRepository.save(review);
    }

    private Review createReview(Menu menu, String madeTime) {
        return Review.builder()
                .menu(menu)
                .reviewRate(5.0)
                .comment("테스트 리뷰")
                .writerNickname("테스터")
                .writerEmail("test@test.com")
                .imgLink("")
                .restaurant(menu.getRestaurant())
                .likeCount(0)
                .reportCount(0)
                .madeTime(madeTime)
                .build();
    }
}
