package com.SeeAndYouGo.SeeAndYouGo.review.report;

import com.SeeAndYouGo.SeeAndYouGo.global.exception.ApiException;
import com.SeeAndYouGo.SeeAndYouGo.global.exception.ErrorCode;
import com.SeeAndYouGo.SeeAndYouGo.review.Review;
import com.SeeAndYouGo.SeeAndYouGo.review.ReviewReader;
import com.SeeAndYouGo.SeeAndYouGo.review.ReviewService;
import com.SeeAndYouGo.SeeAndYouGo.user.AdminAuthorizationService;
import com.SeeAndYouGo.SeeAndYouGo.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewReportService {
    private final ReviewReader reviewReader;
    private final ReviewService reviewService;
    private final AdminAuthorizationService authorizationService;

    @Value("${app.review.report-delete-threshold}")
    private int reportDeleteThreshold;

    /**
     * 리뷰를 신고한다. 신고 누적 여부와 삭제는 서버에서만 판단하며,
     * 신고 수가 임계치에 도달하면 리뷰를 삭제한다.
     */
    @Transactional
    public void report(Long reviewId, String email) {
        User reporter = authorizationService.getLoginUser(email);
        Review review = reviewReader.getById(reviewId, "신고하려는 리뷰를 찾을 수 없습니다.");
        validateReportable(review, reporter);

        review.incrementReportCount();

        if (review.hasReachedReportLimit(reportDeleteThreshold)) {
            log.info("[리뷰신고] 신고 누적으로 리뷰 삭제 reviewId={}", reviewId);
            reviewService.removeReview(review);
        }
    }

    private void validateReportable(Review review, User reporter) {
        if (review.isWrittenBy(reporter.getEmail())) {
            throw new ApiException(ErrorCode.REVIEW_SELF_REPORT);
        }
    }
}
