package com.SeeAndYouGo.SeeAndYouGo.like;

import com.SeeAndYouGo.SeeAndYouGo.OAuth.jwt.TokenProvider;
import com.SeeAndYouGo.SeeAndYouGo.like.dto.LikeResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class LikeController {
    private final LikeService likeService;
    @PostMapping("/api/review/like/{review_id}/{token_id}")
    public ResponseEntity<LikeResponseDto> postLikeCount(@PathVariable("review_id") Long reviewId,
                                                         @PathVariable("token_id") String tokenId){
        // 만약 해당 유저가 공감을 누른 상태였다면, 공감 삭제
        // 공감을 누르지 않은 상태였다면 공감 추가이다.
        LikeResponseDto likeResponseDto = likeService.postLikeCount(reviewId, tokenId);
        return ResponseEntity.ok(likeResponseDto);
    }
}
