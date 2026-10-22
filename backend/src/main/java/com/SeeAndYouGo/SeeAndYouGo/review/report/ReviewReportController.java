package com.SeeAndYouGo.SeeAndYouGo.review.report;

import com.SeeAndYouGo.SeeAndYouGo.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class ReviewReportController {
    private static final String REPORT_SUCCESS_MESSAGE = "리뷰 신고가 완료되었습니다.";

    private final ReviewReportService reviewReportService;

    @PostMapping("/reviews/{reviewId}/reports")
    public ApiResponse<Void> reportReview(@PathVariable Long reviewId,
                                          @Parameter(hidden = true) @AuthenticationPrincipal String email) {
        reviewReportService.report(reviewId, email);

        return ApiResponse.success(null, REPORT_SUCCESS_MESSAGE);
    }
}
