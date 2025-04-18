package com.SeeAndYouGo.SeeAndYouGo.like;

import com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt.TokenProvider;
import com.SeeAndYouGo.SeeAndYouGo.review.Review;
import com.SeeAndYouGo.SeeAndYouGo.review.ReviewRepository;
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
    public LikeResponseDto postLikeCount(Long reviewId, String email) {
        Review review = reviewRepository.findById(reviewId).get();
        User user = userRepository.findByEmail(email);

        if(review.getWriterEmail().equals(email)){
            return LikeResponseDto.builder()
                    .mine(true)
                    .like(false)
                    .build();
        }

        boolean isLike = likeRepository.countByReviewAndUser(review, user) > 0;

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
        if(userEmail.equals("") || userEmail.equals("none")) return false;

        User user = userRepository.findByEmail(userEmail);
        if(likeRepository.existsByReviewAndUser(review, user)){
            return true;
        }

        return false;
    }
}