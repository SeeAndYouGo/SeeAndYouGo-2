package com.SeeAndYouGo.SeeAndYouGo.review;

import com.SeeAndYouGo.SeeAndYouGo.global.exception.EntityNotFoundException;
import com.SeeAndYouGo.SeeAndYouGo.global.exception.ErrorCode;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuRepository;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuService;
import com.SeeAndYouGo.SeeAndYouGo.rate.RateRepository;
import com.SeeAndYouGo.SeeAndYouGo.rate.RateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("신고 리뷰 삭제 - ReviewService.deleteReportedReview")
class ReviewReportDeleteTest {

    @Mock private RateService rateService;
    @Mock private MenuService menuService;
    @Mock private ReviewRepository reviewRepository;
    @Mock private ReviewHistoryRepository reviewHistoryRepository;
    @Mock private RateRepository rateRepository;
    @Mock private MenuRepository menuRepository;
    @Mock private ReviewReader reviewReader;

    @InjectMocks private ReviewService reviewService;

    @Test
    @DisplayName("존재하는 리뷰는 삭제된다")
    void deletesExistingReview() {
        Review review = mock(Review.class);
        given(reviewReader.getById(1L)).willReturn(review);

        reviewService.deleteReportedReview(1L);

        verify(reviewRepository).delete(review);
    }

    @Test
    @DisplayName("없는 리뷰면 REVIEW_001 예외가 나고 삭제하지 않는다")
    void throwsReviewNotFoundWhenMissing() {
        given(reviewReader.getById(99L))
                .willThrow(new EntityNotFoundException(ErrorCode.REVIEW_NOT_FOUND, "id=99"));

        assertThatThrownBy(() -> reviewService.deleteReportedReview(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
        verify(reviewRepository, never()).delete(any());
    }
}
