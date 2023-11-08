package com.SeeAndYouGo.SeeAndYouGo.Review;

import com.SeeAndYouGo.SeeAndYouGo.Menu.Dept;
import com.SeeAndYouGo.SeeAndYouGo.Menu.Menu;
import com.SeeAndYouGo.SeeAndYouGo.Menu.MenuService;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuService menuService;

    @Transactional
    public Long registerReview(Review review, String restaurantName, String dept) {
        restaurantName = menuService.parseRestaurantName(restaurantName);
        Restaurant restaurant = restaurantRepository.findTodayRestaurant(restaurantName, LocalDate.now().toString());
        if (restaurant == null) {
            throw new IllegalArgumentException("Restaurant not found for name: " + restaurantName);
        }

        review.setRestaurant(restaurant);
        Dept changeStringToDept = Dept.valueOf(dept);
        Menu menu;
        menu = findMenuByRestaurantAndDept(restaurant, changeStringToDept);
        review.setMenu(menu);
        reviewRepository.save(review);
        return review.getId();
    }

    private Menu findMenuByRestaurantAndDept(Restaurant restaurant, Dept dept) {
        for (Menu menu : restaurant.getMenuList()) {
            if(menu.getDept().equals(dept))
                return menu;
        }
        throw new RuntimeException("[ERROR] : 해당 일자에 일치하는 메뉴가 없습니다.");
    }

//    @Transactional
//    public void deleteReview(Long reviewId) {
//        Review review = reviewRepository.findOne(reviewId);
//        if (review != null) {
//            reviewRepository.delete(review);
//        }
//    }


    public Review findOne(Long id) {
        Optional<Review> reviewOptional = reviewRepository.findById(id);
        return reviewOptional.orElse(null);
    }


    public List<Review> findAllReviews(String date) {
        return reviewRepository.findAllByMadeTime(date);
    }

    public List<Review> findReviewsByWriter(String writer) {
        return reviewRepository.findByWriter(writer);
    }

    public List<Review> findReviewsByMenu(Menu menu) {
        return reviewRepository.findByMenu(menu);
    }

    public List<Review> findReviewsWithRatingAbove(Double reviewRate) {
        return reviewRepository.findByReviewRateGreaterThan(reviewRate);
    }

    public List<Review> findAllReviewsSortedByLikes() {
        return reviewRepository.findAllOrderByLikeCountDesc();
    }

    public List<Review> findReviewsAfterTime(LocalDateTime time) {
        return reviewRepository.findByMadeTimeAfter(time);
    }

    public List<Review> findReviewsWithImageLink() {
        return reviewRepository.findByImgLinkIsNotNull();
    }

    @Transactional
    public void increaseLikeCount(Long reviewId) {
        Review review = reviewRepository.findOne(reviewId);
        if (review == null) {
            throw new IllegalArgumentException("Review not found for ID: " + reviewId);
        }
        review.setLikeCount(review.getLikeCount() + 1);
    }

    @Transactional
    public void updateReviewContent(Long reviewId, String newContent) {
        Review review = reviewRepository.findOne(reviewId);
        if (review == null) {
            throw new IllegalArgumentException("Review not found for ID: " + reviewId);
        }
        review.setComment(newContent);
    }

    public List<Review> findTopReviewsByRestaurantAndDate(String restaurantName, String date) {
        return reviewRepository.findTopReviewsByRestaurantAndDate(restaurantName, date);
    }

    public List<Review> findRestaurantReviews(String restaurant, String date) {
        return reviewRepository.findReviewsByRestaurantAndDate(restaurant, date);
    }
//    public void delete(Review review) {
//        reviewRepository.delete(review);
//    }
}
