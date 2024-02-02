package com.SeeAndYouGo.SeeAndYouGo.like;

import com.SeeAndYouGo.SeeAndYouGo.Review.Review;
import com.SeeAndYouGo.SeeAndYouGo.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Long countByReviewAndUser(Review review, User user);

    void deleteByReviewAndUser(Review review, User user);
}
