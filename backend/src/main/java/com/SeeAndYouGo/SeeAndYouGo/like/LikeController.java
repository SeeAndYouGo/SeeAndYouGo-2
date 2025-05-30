package com.SeeAndYouGo.SeeAndYouGo.like;

import com.SeeAndYouGo.SeeAndYouGo.like.dto.LikeResponseDto;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class LikeController {
    private final LikeService likeService;
    @PostMapping("/review/like/{review_id}")
    public LikeResponseDto postLikeCount(@PathVariable("review_id") Long reviewId,
                                         @Parameter(hidden = true) @AuthenticationPrincipal String email){
        // 만약 해당 유저가 공감을 누른 상태였다면, 공감 삭제
        // 공감을 누르지 않은 상태였다면 공감 추가이다
        return likeService.postLikeCount(reviewId, email);
    }
}