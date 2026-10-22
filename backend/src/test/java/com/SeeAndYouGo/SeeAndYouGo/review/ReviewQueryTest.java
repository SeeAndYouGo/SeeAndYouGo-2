package com.SeeAndYouGo.SeeAndYouGo.review;

import com.SeeAndYouGo.SeeAndYouGo.global.exception.ApiException;
import com.SeeAndYouGo.SeeAndYouGo.global.exception.ErrorCode;
import com.SeeAndYouGo.SeeAndYouGo.like.LikeService;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import com.SeeAndYouGo.SeeAndYouGo.user.AuthorizationService;
import com.SeeAndYouGo.SeeAndYouGo.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * GET /api/reviews 의 restaurant·sort 조합별 분기를 검증한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("리뷰 조회 - GET /api/reviews")
class ReviewQueryTest {

    private static final String EMAIL = "user@test.com";

    @Mock private ReviewService reviewService;
    @Mock private UserService userService;
    @Mock private LikeService likeService;
    @Mock private AuthorizationService authorizationService;
    @Mock private Executor executor;

    private ReviewController reviewController;

    @BeforeEach
    void setUp() {
        reviewController = new ReviewController(reviewService, userService, likeService, authorizationService, executor);
    }

    @Test
    @DisplayName("restaurant가 없으면 전 식당의 리뷰를 조회한다")
    void noRestaurant_findsAllRestaurants() {
        reviewController.getReviews(null, null, EMAIL);

        verify(reviewService, times(Restaurant.values().length)).findRestaurantReviews(anyString(), anyString());
        verify(reviewService, never()).findTopReviewsByRestaurantAndDate(anyString(), anyString());
    }

    @Test
    @DisplayName("restaurant가 있으면 해당 식당의 리뷰를 조회한다")
    void restaurant_findsRestaurantReviews() {
        reviewController.getReviews("restaurant2", null, EMAIL);

        verify(reviewService).findRestaurantReviews(eq("restaurant2"), anyString());
        verify(reviewService, never()).findTopReviewsByRestaurantAndDate(anyString(), anyString());
    }

    @Test
    @DisplayName("sort=top이면 해당 식당의 탑 리뷰를 조회한다")
    void sortTop_findsTopReviews() {
        reviewController.getReviews("2", "top", EMAIL);

        verify(reviewService).findTopReviewsByRestaurantAndDate(eq(Restaurant.parseName("2")), anyString());
    }

    @Test
    @DisplayName("sort=top인데 restaurant가 없으면 COMMON_002")
    void sortTopWithoutRestaurant_invalidInput() {
        assertInvalidInput(() -> reviewController.getReviews(null, "top", EMAIL));
    }

    @Test
    @DisplayName("지원하지 않는 sort 값이면 COMMON_002")
    void unknownSort_invalidInput() {
        assertInvalidInput(() -> reviewController.getReviews("2", "latest", EMAIL));
    }

    private void assertInvalidInput(Runnable request) {
        assertThatThrownBy(request::run)
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_INPUT_VALUE);

        verifyNoInteractions(reviewService);
    }
}
