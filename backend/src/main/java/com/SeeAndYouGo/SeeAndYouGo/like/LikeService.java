package com.SeeAndYouGo.SeeAndYouGo.like;

import com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt.TokenProvider;
import com.SeeAndYouGo.SeeAndYouGo.review.Review;
import com.SeeAndYouGo.SeeAndYouGo.review.ReviewRepository;
import com.SeeAndYouGo.SeeAndYouGo.like.dto.LikeResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.user.User;
import com.SeeAndYouGo.SeeAndYouGo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LikeService {
    private final ReviewRepository reviewRepository;
    private final TokenProvider tokenProvider;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final RedisTemplate redisTemplate;

    @Transactional

    public LikeResponseDto postLikeCount(Long reviewId, String tokenId) {
        Review review = reviewRepository.findById(reviewId).get();
        String userEmail = tokenProvider.decodeToEmail(tokenId);
        User user = userRepository.findByEmail(userEmail);

        if (review.getWriterEmail().equals(userEmail)) {
            return LikeResponseDto.builder()
                    .mine(true)
                    .like(false)
                    .build();
        }

        boolean isLike = likeRepository.countByReviewAndUser(review, user) > 0;

        if (isLike) {
            return deleteLike(review, user);
        } else {
            return postLike(review, user);
        }

    }

    private LikeResponseDto postLike(Review review, User user) {
        Like like = Like.builder()
                .review(review)
                .user(user)
                .build();

        likeRepository.save(like);
        review.incrementLikeCount();
        reviewRepository.save(review);
        return LikeResponseDto.builder()
                .mine(false)
                .like(true)
                .build();
    }

    private LikeResponseDto deleteLike(Review review, User user) {
        likeRepository.deleteByReviewAndUser(review, user);
        review.decrementLikeCount();
        reviewRepository.save(review);
        return LikeResponseDto.builder()
                .mine(false)
                .like(false)
                .build();
    }

    public boolean isLike(Review review, String userEmail) {
        if (userEmail.equals("")) return false;

        User user = userRepository.findByEmail(userEmail);
        if (likeRepository.existsByReviewAndUser(review, user)) {
            return true;
        }

        return false;
    }

    public String getWriter(Long reviewId) {
        Review review = reviewRepository.findById(reviewId).get();
        return review.getWriterEmail();
    }

//    @Scheduled(fixedRate = 60000 * 60) // 1시간마다
//    public void backupLikeCachingData() {
//        HashOperations redisHash = redisTemplate.opsForHash();
//        Set<String> keys = redisTemplate.keys("review:like:*");
//
//        Map<User, Review> likes = new HashMap<>();
//        Map<User, Review> unlikes = new HashMap<>();
//
//        if (keys != null) {
//            for (String key : keys) {
//                Map<String, Integer> userLikes = redisHash.entries(key); // 각 키의 모든 해시 가져오기
//
//                for (Map.Entry<String, String> entry : userLikes.entrySet()) {
//                    String userEmail = entry.getKey();
//                    Integer value = entry.getValue();
//
//                    User user = userRepository.findByEmail(userEmail);
//
//
//                    if (value.equals(1)) {
//                        // User와 Review 객체를 생성하고 likes에 추가
//                        likes.put(user, new Review(key.replace("review:like:", "")));
//                    } else if (value.equals(0)) {
//                        // User와 Review 객체를 생성하고 unlikes에 추가
//                        unlikes.put(user, new Review(key.replace("review:like:", "")));
//                    }
//                }
//            }
//        }
//
//        // 캐시 데이터 List<Like>
//
//
//        HashOperations ops = redisHash;
//        ops.get
//
//        likeRepository.saveAll(backupTargets);
//    }
}
