package com.SeeAndYouGo.SeeAndYouGo.Review;

import com.SeeAndYouGo.SeeAndYouGo.Menu.Dept;
import com.SeeAndYouGo.SeeAndYouGo.Menu.Menu;
import com.SeeAndYouGo.SeeAndYouGo.Menu.MenuRepository;
import com.SeeAndYouGo.SeeAndYouGo.Rate.Rate;
import com.SeeAndYouGo.SeeAndYouGo.Rate.RateRepository;
import com.SeeAndYouGo.SeeAndYouGo.Rate.RateService;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewService {
    private final RateService rateService;
    private final ReviewRepository reviewRepository;
    private final ReviewHistoryRepository reviewHistoryRepository;
    private final RateRepository rateRepository;
    private final MenuRepository menuRepository;
    private static final int TOP_REVIEW_NUMBER_OF_CRITERIA = 3; // top-review에서 각 DEPT별 리뷰를 몇개까지 살릴 것인가?
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional
    @Caching( evict = {
            @CacheEvict(value="getTotalRestaurantRate", key="#data.restaurant"),
            @CacheEvict(value="getDetailRestaurantRate", key="#data.restaurant")})
    public Long registerReview(ReviewData data) {
        LocalDateTime time = LocalDateTime.now();

        Restaurant restaurant = Restaurant.valueOf(data.getRestaurant());
        Objects.requireNonNull(restaurant, "Restaurant not fount for name: " + data.getRestaurant());

        // 연관관계 존재
//        Menu menu = findMenuByRestaurantAndDept(restaurant, Dept.valueOf(data.getDept()), data.getMenuName());
        Menu menu = menuRepository.getReferenceById(data.getMenuId());
        Review review = Review.createEntity(data, restaurant, menu, time.format(formatter));
        menu.addReviewAndUpdateRate(review);
        rateService.updateRateByRestaurant(restaurant, data.getRate());
        reviewRepository.save(review);

        return review.getId();
    }
//
//    /**
//     * 1학은 메뉴 이름으로 메뉴를 찾고, 그 외에는 Dept를 기준으로 메뉴를 찾는다.
//     */
//    private Menu findMenuByRestaurantAndDept(Restaurant restaurant, Dept dept, String menuName) {
//        if(restaurant.name().contains("1")){
//            for (Menu menu : restaurant.getMenuList()) {
//                if(menu.getMenuName().equals(menuName)) return menu;
//            }
//        }
//
//        for (Menu menu : restaurant.getMenuList()) {
//            if(menu.getDept().equals(dept))
//                return menu;
//        }
//        throw new RuntimeException("[ERROR] : 해당 일자에 일치하는 메뉴가 없습니다.");
//    }

        /**
     * top-review는 각 DEPT 별로 3개씩의 review를 불러온다.
     * 여기서 1학생회관은 모든 DEPT를 STUDENT로 취급한다. 즉, 모든 메뉴에서 딱 3개만 불러오기
     */
    public List<Review> findTopReviewsByRestaurantAndDate(String restaurantName, String date) {
        List<Review> restaurantReviews = findRestaurantReviews(restaurantName, date);
        // 시간순으로 최근꺼가 먼저 오게 정렬하여 3개만 가져오자.
        sortReviewsByDate(restaurantReviews);

        List<Review> studentReviews = new ArrayList<>();
        List<Review> staffReviews = new ArrayList<>();

        splitStudentAndStaff(restaurantReviews, studentReviews, staffReviews);

        List<Review> result = new ArrayList<>();
        addReviewsByTopReviewRule(result, studentReviews);
        addReviewsByTopReviewRule(result, staffReviews);

        return result;
    }

    /**
     * top-review 정책에 의해서 각 review를 알맞게 result에 담는다.
     */
    private void addReviewsByTopReviewRule(List<Review> result, List<Review> reviews) {
        int count = 0;

        for (Review review : reviews) {
            if(count >= TOP_REVIEW_NUMBER_OF_CRITERIA) return;

            result.add(review);
            count++;
        }
    }

    public void splitStudentAndStaff(List<Review> restaurantReviews, List<Review> studentReviews, List<Review> staffReviews) {
        for (Review review : restaurantReviews) {
            if(review.getMenu().getDept().equals(Dept.STAFF)){
                staffReviews.add(review);
                continue;
            }

            studentReviews.add(review);
        }
    }

    private void sortReviewsByDate(List<Review> reviews) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Collections.sort(reviews, new Comparator<Review>() {
            // 가장 최신꺼가 가장 위로 온다!!
            @Override
            public int compare(Review o1, Review o2) {

                LocalDateTime o1Time = LocalDateTime.parse(o1.getMadeTime(), formatter);
                LocalDateTime o2Time = LocalDateTime.parse(o2.getMadeTime(), formatter);

                if(o1Time.isEqual(o2Time)){
                    return 0;
                }else if(o1Time.isBefore(o2Time)){
                    return 1;
                }else {
                    return -1;
                }

            }
        });
    }

    public List<Review> findRestaurantReviews(String restaurantName, String date) {
        String parseRestaurantName = Restaurant.parseName(restaurantName); // restaurant1 이런ㄱ ㅔ아니라 1학생회관 이런 식으로 이쁘게 이름을 바꿔줌.
        Restaurant restaurant = Restaurant.valueOf(parseRestaurantName);
        return reviewRepository.findByRestaurantAndMadeTimeStartingWith(restaurant, date);
//        return reviewRepository.findRestaurantReviews(restaurant.getId(), date);
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
        Restaurant restaurant = review.getRestaurant();

        if(review.getWriterEmail().equals(userEmail)){
            deleteById(reviewId);

            Rate rateByRestaurant = rateRepository.findByRestaurant(restaurant);
            rateByRestaurant.exceptRate(review.getReviewRate());
            return true;
        }

        return false;
    }
}
