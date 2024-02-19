package com.SeeAndYouGo.SeeAndYouGo.like;

import com.SeeAndYouGo.SeeAndYouGo.OAuth.jwt.TokenProvider;
import com.SeeAndYouGo.SeeAndYouGo.Review.Review;
import com.SeeAndYouGo.SeeAndYouGo.Review.ReviewRepository;
import com.SeeAndYouGo.SeeAndYouGo.Review.ReviewService;
import com.SeeAndYouGo.SeeAndYouGo.like.dto.LikeResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.user.User;
import com.SeeAndYouGo.SeeAndYouGo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LikeService {
    private final ReviewRepository reviewRepository;
    private final TokenProvider tokenProvider;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;

    @Transactional
    public LikeResponseDto postLikeCount(Long reviewId, String tokenId) {
        Review review = reviewRepository.findById(reviewId).get();
        String userEmail = tokenProvider.decodeToEmail(tokenId);
        User user = userRepository.findByEmail(userEmail).get(0);

        if(review.getWriterEmail().equals(userEmail)){
            return LikeResponseDto.builder()
                    .mine(true)
                    .like(false)
                    .build();
        }

        boolean isLike = likeRepository.countByReviewAndUser(review, user)>0? true : false;

        if(isLike){
            return deleteLike(review, user);
        }else{
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
        if(userEmail.equals("")) return false;

        User user = userRepository.findByEmail(userEmail).get(0);
        if(likeRepository.existsByReviewAndUser(review, user)){
            return true;
        }

        return false;
    }
}
