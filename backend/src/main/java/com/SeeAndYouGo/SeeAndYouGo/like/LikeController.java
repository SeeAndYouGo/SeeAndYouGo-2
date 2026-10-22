package com.SeeAndYouGo.SeeAndYouGo.like;

import com.SeeAndYouGo.SeeAndYouGo.global.response.ApiResponse;
import com.SeeAndYouGo.SeeAndYouGo.like.dto.LikeResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.user.AuthorizationService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class LikeController {
    private static final String LIKED_MESSAGE = "리뷰에 공감했습니다.";
    private static final String UNLIKED_MESSAGE = "리뷰 공감을 해제했습니다.";
    private static final String OWN_REVIEW_MESSAGE = "내가 쓴 리뷰는 공감할 수 없습니다.";

    private final LikeService likeService;
    private final AuthorizationService authorizationService;

    @PostMapping("/reviews/{reviewId}/likes")
    public ApiResponse<LikeResponseDto> postLikeCount(@PathVariable("reviewId") Long reviewId,
                                                      @Parameter(hidden = true) @AuthenticationPrincipal String email){
        authorizationService.getLoginUser(email);
        // 만약 해당 유저가 공감을 누른 상태였다면, 공감 삭제
        // 공감을 누르지 않은 상태였다면 공감 추가이다
        LikeResponseDto response = likeService.postLikeCount(reviewId, email);
        return ApiResponse.success(response, resultMessage(response));
    }

    private String resultMessage(LikeResponseDto response) {
        if (response.isMine()) {
            return OWN_REVIEW_MESSAGE;
        }
        return response.isLike() ? LIKED_MESSAGE : UNLIKED_MESSAGE;
    }
}