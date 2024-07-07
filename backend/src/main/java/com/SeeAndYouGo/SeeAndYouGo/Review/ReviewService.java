package com.SeeAndYouGo.SeeAndYouGo.Review;

import com.SeeAndYouGo.SeeAndYouGo.Menu.Dept;
import com.SeeAndYouGo.SeeAndYouGo.Menu.Menu;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewService {
    private final RestaurantRepository restaurantRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewHistoryRepository reviewHistoryRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional
    @Caching( evict = {
            @CacheEvict(value="getTotalRestaurantRate", key="#data.restaurant"),
            @CacheEvict(value="getDetailRestaurantRate", key="#data.restaurant")})
    public Long registerReview(ReviewData data) {
        LocalDateTime time = LocalDateTime.now();

        Restaurant restaurant = restaurantRepository.findByNameAndDate(data.getRestaurant(),
                LocalDate.of(time.getYear(), time.getMonth(), time.getDayOfMonth()).toString());
        Objects.requireNonNull(restaurant, "Restaurant not fount for name: " + data.getRestaurant());

        // 연관관계 존재
        Menu menu = findMenuByRestaurantAndDept(restaurant, Dept.valueOf(data.getDept()), data.getMenuName());
        Review review = Review.createEntity(data, restaurant, menu, time.format(formatter));
        menu.addReviewAndUpdateRate(review);
        restaurant.updateTotalRate();
        reviewRepository.save(review);

        return review.getId();
    }

    /**
     * 1학은 메뉴 이름으로 메뉴를 찾고, 그 외에는 Dept를 기준으로 메뉴를 찾는다.
     */
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

    public List<Review> findRestaurantReviews(String restaurantName, String date) {
        restaurantName = Restaurant.parseName(restaurantName); // restaurant1 이런ㄱ ㅔ아니라 1학생회관 이런 식으로 이쁘게 이름을 바꿔줌.
        Restaurant restaurant = restaurantRepository.findByNameAndDate(restaurantName, date);

        return reviewRepository.findRestaurantReviews(restaurant.getId(), date);
    }

    @Transactional
    public Integer updateReportCount(Long reviewId) {
        Review review = reviewRepository.findById(reviewId).get();
        return review.incrementReportCount();
    }

    @Transactional
    @Caching( evict = {
            @CacheEvict(value="getTotalRestaurantRate", allEntries = true),
            @CacheEvict(value="getDetailRestaurantRate", allEntries = true)})
    public void deleteById(Long reviewId) {
        Review review = reviewRepository.getReferenceById(reviewId);

        review.getMenu().deleteReview(review);
        reviewRepository.deleteById(reviewId);

        ReviewHistory reviewHistory = new ReviewHistory(review);
        reviewHistoryRepository.save(reviewHistory);
    }

    public List<Review> findReviewsByWriter(String userEmail) {
        return reviewRepository.findByWriterEmailOrderByMadeTimeDesc(userEmail);
    }

    /**
     * 리뷰 작성자와 요청자가 일치하는지 검증 후, 리뷰를 삭제한다.
     * @param userEmail
     * @param reviewId
     * @return
     */
    @Transactional
    @Caching( evict = {
            @CacheEvict(value="getTotalRestaurantRate", allEntries = true),
            @CacheEvict(value="getDetailRestaurantRate", allEntries = true)})
    public boolean deleteReview(String userEmail, Long reviewId) {
        Review review = reviewRepository.findById(reviewId).get();

        if(review.getWriterEmail().equals(userEmail)){
            deleteById(reviewId);

            review.getRestaurant().updateTotalRate();
            return true;
        }

        return false;
    }
}
