package com.SeeAndYouGo.SeeAndYouGo.review.report;

import com.SeeAndYouGo.SeeAndYouGo.global.exception.ApiException;
import com.SeeAndYouGo.SeeAndYouGo.global.exception.ErrorCode;
import com.SeeAndYouGo.SeeAndYouGo.review.Review;
import com.SeeAndYouGo.SeeAndYouGo.review.ReviewReader;
import com.SeeAndYouGo.SeeAndYouGo.review.ReviewService;
import com.SeeAndYouGo.SeeAndYouGo.user.AdminAuthorizationService;
import com.SeeAndYouGo.SeeAndYouGo.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("리뷰 신고 - ReviewReportService.report")
class ReviewReportServiceTest {

    @Mock private ReviewReader reviewReader;
    @Mock private ReviewService reviewService;
    @Mock private AdminAuthorizationService authorizationService;

    @InjectMocks private ReviewReportService reviewReportService;

    private static final Long REVIEW_ID = 1L;
    private static final String WRITER_EMAIL = "writer@test.com";
    private static final String REPORTER_EMAIL = "reporter@test.com";
    private static final int THRESHOLD = 10;

    private User reporter;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(reviewReportService, "reportDeleteThreshold", THRESHOLD);
        reporter = mock(User.class);
    }

    private Review reviewWithReportCount(int reportCount) {
        return Review.builder()
                .writerEmail(WRITER_EMAIL)
                .reportCount(reportCount)
                .build();
    }

    private void givenReporterAndReview(Review review) {
        given(authorizationService.getLoginUser(REPORTER_EMAIL)).willReturn(reporter);
        given(reporter.getEmail()).willReturn(REPORTER_EMAIL);
        given(reviewReader.getById(eq(REVIEW_ID), anyString())).willReturn(review);
    }

    @Test
    @DisplayName("신고 시 신고 수가 증가하며, 임계치 미만이면 삭제되지 않는다")
    void report_belowThreshold_doesNotDelete() {
        Review review = reviewWithReportCount(0);
        givenReporterAndReview(review);

        reviewReportService.report(REVIEW_ID, REPORTER_EMAIL);

        assertThat(review.getReportCount()).isEqualTo(1);
        verify(reviewService, never()).removeReview(any());
    }

    @Test
    @DisplayName("신고 수가 임계치에 도달하면 서버에서 리뷰를 삭제한다")
    void report_reachThreshold_deletesReview() {
        Review review = reviewWithReportCount(THRESHOLD - 1);
        givenReporterAndReview(review);

        reviewReportService.report(REVIEW_ID, REPORTER_EMAIL);

        assertThat(review.getReportCount()).isEqualTo(THRESHOLD);
        verify(reviewService).removeReview(review);
    }

    @Test
    @DisplayName("본인이 작성한 리뷰는 신고할 수 없다")
    void report_ownReview_throwsBadRequest() {
        Review review = reviewWithReportCount(0);
        given(authorizationService.getLoginUser(WRITER_EMAIL)).willReturn(reporter);
        given(reporter.getEmail()).willReturn(WRITER_EMAIL);
        given(reviewReader.getById(eq(REVIEW_ID), anyString())).willReturn(review);

        assertThatThrownBy(() -> reviewReportService.report(REVIEW_ID, WRITER_EMAIL))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.REVIEW_SELF_REPORT);

        assertThat(review.getReportCount()).isZero();
        verify(reviewService, never()).removeReview(any());
    }

    @Test
    @DisplayName("비로그인 사용자는 신고할 수 없다")
    void report_guest_throwsUnauthorized() {
        given(authorizationService.getLoginUser("none"))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));

        assertThatThrownBy(() -> reviewReportService.report(REVIEW_ID, "none"))
                .isInstanceOf(ResponseStatusException.class);

        verify(reviewReader, never()).getById(any(), any());
        verify(reviewService, never()).removeReview(any());
    }
}
