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
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewService {
    private final RestaurantRepository restaurantRepository;
    private final MenuService menuService;
    private final ReviewRepository reviewRepository;
    private final ReviewHistoryRepository reviewHistoryRepository;

    @Transactional
    public Long registerReview(Review review, String restaurantName, String dept, String menuName) {
        restaurantName = menuService.parseRestaurantName(restaurantName);
        Restaurant restaurant = restaurantRepository.findByNameAndDate(restaurantName, LocalDate.now().toString()).get(0);
        if (restaurant == null) {
            throw new IllegalArgumentException("Restaurant not found for name: " + restaurantName);
        }

        review.setRestaurant(restaurant);
        Dept changeStringToDept = Dept.valueOf(dept);
        Menu menu;
        menu = findMenuByRestaurantAndDept(restaurant, changeStringToDept, menuName);
        review.setMenu(menu);
        reviewRepository.save(review);
        return review.getId();
    }

    private Menu findMenuByRestaurantAndDept(Restaurant restaurant, Dept dept, String menuName) {
        if(restaurant.getName().contains("1")){
            for (Menu menu : restaurant.getMenuList()) {
                if(menu.getMenuName().equals(menuName)) return menu;
            }
        }

        for (Menu menu : restaurant.getMenuList()) {
            if(menu.getDept().equals(dept))
                return menu;
        }
        throw new RuntimeException("[ERROR] : 해당 일자에 일치하는 메뉴가 없습니다.");
    }

    public List<Review> findAllReviews(String date) {
        return reviewRepository.findAllByMadeTime(date);
    }

    @Transactional
    public void increaseLikeCount(Long reviewId) {
        Review review = reviewRepository.getReferenceById(reviewId);
        if (review == null) {
            throw new IllegalArgumentException("Review not found for ID: " + reviewId);
        }
        review.setLikeCount(review.getLikeCount() + 1);
    }

    @Transactional
    public void updateReviewContent(Long reviewId, String newContent) {
        Review review = reviewRepository.getReferenceById(reviewId);
        if (review == null) {
            throw new IllegalArgumentException("Review not found for ID: " + reviewId);
        }
        review.setComment(newContent);
    }

    public List<Review> findTopReviewsByRestaurantAndDate(String restaurantName, String date) {
        return reviewRepository.findTop5ReviewsByRestaurantAndDate(restaurantName, date);
    }

    public List<Review> findRestaurantReviews(String restaurant, String date) {
        return reviewRepository.findReviewsByRestaurantAndDate(restaurant, date);
    }

    @Transactional
    public Integer updateReportCount(Long reviewId) {
        Review review = reviewRepository.findById(reviewId).get();
        return review.incrementReportCount();
    }

    @Transactional
    public void deleteById(Long reviewId) {
        Review review = reviewRepository.getReferenceById(reviewId);
        reviewRepository.deleteById(reviewId);
        ReviewHistory reviewHistory = review.toReviewHistory();
        reviewHistoryRepository.save(reviewHistory);
    }

    public List<Review> findReviewsByWriter(String userEmail) {
        return reviewRepository.findByWriter(userEmail);
    }

    /**
     * 리뷰 작성자와 요청자가 일치하는지 검증 후, 리뷰를 삭제한다.
     * @param userEmail
     * @param reviewId
     * @return
     */
    @Transactional
    public boolean deleteReview(String userEmail, Long reviewId) {
        Review review = reviewRepository.findById(reviewId).get();

        if(review.writer.equals(userEmail)){
            deleteById(reviewId);
            return true;
        }

        return false;
    }
}
