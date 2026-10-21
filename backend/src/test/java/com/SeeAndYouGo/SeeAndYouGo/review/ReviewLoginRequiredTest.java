package com.SeeAndYouGo.SeeAndYouGo.review;

import com.SeeAndYouGo.SeeAndYouGo.like.LikeService;
import com.SeeAndYouGo.SeeAndYouGo.review.dto.ReviewRequestDto;
import com.SeeAndYouGo.SeeAndYouGo.user.AuthorizationService;
import com.SeeAndYouGo.SeeAndYouGo.user.UserReader;
import com.SeeAndYouGo.SeeAndYouGo.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * 비로그인 사용자는 리뷰 작성·내 리뷰 조회·리뷰 삭제를 할 수 없다.
 * 비로그인 요청은 JwtFilter가 email "none"으로 넘긴다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("리뷰 작성·조회·삭제 로그인 필수")
class ReviewLoginRequiredTest {

    private static final String GUEST = "none";

    @Mock private ReviewService reviewService;
    @Mock private UserService userService;
    @Mock private LikeService likeService;
    @Mock private UserReader userReader;
    @Mock private Executor executor;

    private ReviewController reviewController;

    @BeforeEach
    void setUp() {
        AuthorizationService authorizationService = new AuthorizationService(userReader);
        reviewController = new ReviewController(reviewService, userService, likeService, authorizationService, executor);
    }

    @Test
    @DisplayName("비로그인 리뷰 작성은 401이고 저장하지 않는다")
    void postReview_guest_unauthorized() {
        assertUnauthorized(() -> reviewController.postReview(mock(ReviewRequestDto.class), null, GUEST));

        verifyNoInteractions(reviewService, userService);
    }

    @Test
    @DisplayName("비로그인 내 리뷰 조회는 401이다")
    void getReviewsByUser_guest_unauthorized() {
        assertUnauthorized(() -> reviewController.getReviewsByUser(GUEST));

        verifyNoInteractions(reviewService);
    }

    @Test
    @DisplayName("비로그인 리뷰 삭제는 401이고 삭제하지 않는다")
    void deleteReview_guest_unauthorized() {
        assertUnauthorized(() -> reviewController.deleteReview(1L, GUEST));

        verifyNoInteractions(reviewService);
    }

    private void assertUnauthorized(Runnable request) {
        assertThatThrownBy(request::run)
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatus())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
