package com.SeeAndYouGo.SeeAndYouGo.like;

import com.SeeAndYouGo.SeeAndYouGo.aop.ValidateToken;
import com.SeeAndYouGo.SeeAndYouGo.like.dto.LikeResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class LikeController {
    private final LikeService likeService;
    private final RedisTemplate redisTemplate;
    private final TokenProvider tokenProvider;
    private HashOperations<String, String, String> redisHash;

    @PostConstruct
    public void init() {
        this.redisHash = redisTemplate.opsForHash();
    }

    @PostMapping("/review/like/{review_id}/{token_id}")
    @ValidateToken
    public LikeResponseDto postLikeCount(@PathVariable("review_id") Long reviewId,
                                         @PathVariable("token_id") String tokenId) {

        String userEmail = tokenProvider.decodeToEmail(tokenId);
        String writer = redisHash.get("review:writer", reviewId);

        // not exist caching data
        if (writer == null) {
            LikeResponseDto dto = likeService.postLikeCount(reviewId, tokenId);
            writer = likeService.getWriter(reviewId);
            redisHash.put("review:writer", String.valueOf(reviewId), writer);
        }

        if (writer.equals(userEmail))
            return new LikeResponseDto(false, true);

        if (redisHash.hasKey("review:like:" + reviewId, userEmail)) {  // 캐싱건 있음
            String isLike = redisHash.get("review:like:" + reviewId, userEmail);
            if (isLike.equals("1")) {
                redisHash.put("review:like:" + reviewId, userEmail, "0");
                return new LikeResponseDto(false, false);
            } else {
                redisHash.put("review:like:" + reviewId, userEmail, "1");
                return new LikeResponseDto(true, false);
            }
        }
        else {  // no caching data & not mine
            LikeResponseDto dto = likeService.postLikeCount(reviewId, tokenId);
            if (dto.isLike()) {
                redisHash.put("review:like:" + reviewId, userEmail, "1");
            } else {
                redisHash.put("review:like:" + reviewId, userEmail, "0");
            }
            return dto;
        }
    }
}