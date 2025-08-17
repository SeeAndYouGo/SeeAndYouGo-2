package com.SeeAndYouGo.SeeAndYouGo.like;

import com.SeeAndYouGo.SeeAndYouGo.review.Review;
import com.SeeAndYouGo.SeeAndYouGo.review.ReviewRepository;
import com.SeeAndYouGo.SeeAndYouGo.like.dto.LikeResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.user.User;
import com.SeeAndYouGo.SeeAndYouGo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LikeService {
    private final ReviewRepository reviewRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;

    @Transactional
    public LikeResponseDto postLikeCount(Long reviewId, String email) {
        log.info("Processing like/unlike for review ID: {}", reviewId);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("Review not found with ID: " + reviewId));
        User user = userRepository.findByEmail(email);

        if (user == null) {
            log.error("User not found for like operation. Review ID: {}", reviewId);
            throw new javax.persistence.EntityNotFoundException("User not found for like operation");
        }

        if(review.getWriterEmail().equals(email)){
            log.warn("User attempted to like their own review. Review ID: {}", reviewId);
            return new LikeResponseDto(false, true);
        }

        boolean isLiked = likeRepository.existsByReviewAndUser(review, user);

        if(isLiked){
            return deleteLike(review, user);
        }else{
            return postLike(review, user);
        }
    }

    private LikeResponseDto postLike(Review review, User user) {
        log.info("Adding like for review ID: {} from user ID: {}", review.getId(), user.getId());
        Like like = Like.builder()
                .review(review)
                .user(user)
                .build();

        likeRepository.save(like);
        review.incrementLikeCount();
        return new LikeResponseDto(true, false);
    }

    private LikeResponseDto deleteLike(Review review, User user) {
        log.info("Removing like for review ID: {} from user ID: {}", review.getId(), user.getId());
        likeRepository.deleteByReviewAndUser(review, user);
        review.decrementLikeCount();
        return new LikeResponseDto(false, false);
    }

    public boolean isLike(Review review, String userEmail) {
        if(userEmail == null || userEmail.isEmpty() || userEmail.equals("none")) return false;

        User user = userRepository.findByEmail(userEmail);
        if (user == null) {
            return false;
        }

        return likeRepository.existsByReviewAndUser(review, user);
    }
}
