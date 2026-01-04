package com.SeeAndYouGo.SeeAndYouGo.menu;

import com.SeeAndYouGo.SeeAndYouGo.dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishType;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import com.SeeAndYouGo.SeeAndYouGo.review.Review;
import com.SeeAndYouGo.SeeAndYouGo.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MenuRateTest {

    private Menu menu;
    private User user;

    @BeforeEach
    void setUp() {
        menu = Menu.builder()
                .price(6000)
                .date("2025-11-01")
                .dept(Dept.STUDENT)
                .menuType(MenuType.LUNCH)
                .restaurant(Restaurant.제2학생회관)
                .isOpen(true)
                .build();

        Dish mainDish = Dish.builder()
                .name("김치찌개")
                .dishType(DishType.MAIN)
                .build();

        menu.addDish(mainDish);

        user = User.builder()
                .email("test@test.com")
                .nickname("테스터")
                .build();
    }

    @Test
    @DisplayName("평균 계산 - 첫 리뷰 추가")
    void addFirstReview() {
        // given
        Review review = createReview(5.0);

        // when
        menu.addReviewAndUpdateRate(review);

        // then
        assertThat(menu.getRate()).isEqualTo(5.0);
    }

    @Test
    @DisplayName("평균 계산 - 여러 리뷰 추가 시 올바른 평균")
    void addMultipleReviews() {
        // given
        Review review1 = createReview(4.0);
        Review review2 = createReview(5.0);
        Review review3 = createReview(3.0);

        // when
        menu.addReviewAndUpdateRate(review1);
        menu.addReviewAndUpdateRate(review2);
        menu.addReviewAndUpdateRate(review3);

        // then
        // (4.0 + 5.0 + 3.0) / 3 = 4.0
        assertThat(menu.getRate()).isEqualTo(4.0);
    }

    @Test
    @DisplayName("평균 계산 오류 수정 검증 - 분석 문서의 예시")
    void verifyFixedAverageCalculation() {
        // given - 현재 평균 4.0 (리뷰 2개)
        Review review1 = createReview(4.0);
        Review review2 = createReview(4.0);
        menu.addReviewAndUpdateRate(review1);
        menu.addReviewAndUpdateRate(review2);
        assertThat(menu.getRate()).isEqualTo(4.0);

        // when - 새 리뷰 5.0 추가
        Review review3 = createReview(5.0);
        menu.addReviewAndUpdateRate(review3);

        // then - 올바른 평균: (4.0 × 2 + 5.0) / 3 = 4.33
        assertThat(menu.getRate()).isCloseTo(4.33, org.assertj.core.data.Offset.offset(0.01));
    }

    @Test
    @DisplayName("리뷰 삭제 후 평균 재계산")
    void deleteReviewAndRecalculateRate() {
        // given
        Review review1 = createReview(4.0);
        Review review2 = createReview(5.0);
        Review review3 = createReview(3.0);
        menu.addReviewAndUpdateRate(review1);
        menu.addReviewAndUpdateRate(review2);
        menu.addReviewAndUpdateRate(review3);
        assertThat(menu.getRate()).isEqualTo(4.0);

        // when - 5.0 리뷰 삭제
        menu.deleteReview(review2);

        // then - (4.0 + 3.0) / 2 = 3.5
        assertThat(menu.getRate()).isEqualTo(3.5);
    }

    @Test
    @DisplayName("모든 리뷰 삭제 시 평균 0")
    void deleteAllReviews() {
        // given
        Review review = createReview(5.0);
        menu.addReviewAndUpdateRate(review);

        // when
        menu.deleteReview(review);

        // then
        assertThat(menu.getRate()).isEqualTo(0.0);
    }

    private Review createReview(double rate) {
        return Review.builder()
                .menu(menu)
                .reviewRate(rate)
                .comment("테스트 리뷰")
                .writerNickname("테스터")
                .writerEmail(user.getEmail())
                .imgLink("")
                .restaurant(Restaurant.제2학생회관)
                .likeCount(0)
                .reportCount(0)
                .madeTime("2025-11-01 12:00:00")
                .build();
    }
}
