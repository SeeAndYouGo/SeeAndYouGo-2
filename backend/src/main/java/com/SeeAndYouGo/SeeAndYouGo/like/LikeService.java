package com.SeeAndYouGo.SeeAndYouGo.like;

import com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt.TokenProvider;
import com.SeeAndYouGo.SeeAndYouGo.review.Review;
import com.SeeAndYouGo.SeeAndYouGo.review.ReviewReader;
import com.SeeAndYouGo.SeeAndYouGo.review.ReviewRepository;
import com.SeeAndYouGo.SeeAndYouGo.like.dto.LikeResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.user.User;
import com.SeeAndYouGo.SeeAndYouGo.user.UserReader;
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
    private final UserReader userReader;
    private final ReviewReader reviewReader;

    @Transactional
    public LikeResponseDto postLikeCount(Long reviewId, String email) {
        Review review = reviewReader.getById(reviewId, "좋아요를 처리할 리뷰를 찾을 수 없습니다.");
        User user = userReader.getByEmail(email, "좋아요를 처리할 사용자 정보를 찾을 수 없습니다. 다시 로그인해주세요.");

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

        return userReader.findByEmail(userEmail)
                .map(user -> likeRepository.existsByReviewAndUser(review, user))
                .orElse(false);
    }
}