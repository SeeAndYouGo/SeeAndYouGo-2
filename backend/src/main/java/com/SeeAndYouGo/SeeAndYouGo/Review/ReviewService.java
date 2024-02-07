package com.SeeAndYouGo.SeeAndYouGo.Review;

import com.SeeAndYouGo.SeeAndYouGo.Dish.Dish;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewService {
    private final RestaurantRepository restaurantRepository;
    private final MenuService menuService;
    private final ReviewRepository reviewRepository;
    private final ReviewHistoryRepository reviewHistoryRepository;
    static private final int TOP_REVIEW_NUMBER_OF_CRITERIA = 3; // top-review에서 각 메뉴별 리뷰를 몇개까지 살릴 것인가?

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

    public List<Review> findRestaurantReviews(String restaurantName, String date) {
        Restaurant restaurant = restaurantRepository.findByNameAndDate(restaurantName, date).get(0);
        List<Review> reviewsOfSameMainDish = new ArrayList<>();

        for (Menu menu : restaurant.getMenuList()) {
            List<Review> reviews = getReviewsByMainDish(menu.getMainDish(), restaurantName);
            reviewsOfSameMainDish.addAll(reviews);
        }

        return reviewsOfSameMainDish;
//        return reviewRepository.findTop5ReviewsByRestaurantAndDate(restaurantName, date);
    }

    private List<Review> getReviewsByMainDish(Dish mainDish, String restaurantName) {
        List<Review> reviewsOfSameMainDish = new ArrayList<>();

        for (Menu menu : mainDish.getMenus()) {
            if(menu.getRestaurant().getName().equals(restaurantName))
                reviewsOfSameMainDish.addAll(menu.getReviewList());
        }

        return reviewsOfSameMainDish;
    }

//    public List<Review> findRestaurantReviews(String restaurantName, String date) {
//        Restaurant restaurant = restaurantRepository.findByNameAndDate(restaurantName, date).get(0);
//
//
//        return reviewRepository.findReviewsByRestaurantAndDate(restaurantName, date);
//    }

    @Transactional
    public Integer updateReportCount(Long reviewId) {
        Review review = reviewRepository.findById(reviewId).get();
        return review.incrementReportCount();
    }

    @Transactional
    public void deleteById(Long reviewId) {
        Review review = reviewRepository.getReferenceById(reviewId);
        reviewRepository.deleteById(reviewId);
        ReviewHistory reviewHistory = new ReviewHistory(review);
        reviewHistoryRepository.save(reviewHistory);
    }

    public List<Review> findReviewsByWriter(String userEmail) {
        return reviewRepository.findByWriterEmail(userEmail);
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

        if(review.getWriterEmail().equals(userEmail)){
            deleteById(reviewId);
            return true;
        }

        return false;
    }

    /**
     * Top-review는 학생 최대 3개, 교직원 최대 3개 최신순으로만 살린다. 이 특징을 살려주자
     * @param restaurantName
     * @param date
     * @return
     */
    public List<Review> findRestaurantTopReviews(String restaurantName, String date) {
        List<Review> restaurantReviews = findRestaurantReviews(restaurantName, date);

        List<Review> staffReviews = new ArrayList<>();
        List<Review> studentReviews = new ArrayList<>();
        splitReviews(restaurantReviews, staffReviews, studentReviews);

        sortReviewsByDate(staffReviews);
        sortReviewsByDate(studentReviews);

        removeIfExceedCriteria(staffReviews);
        removeIfExceedCriteria(studentReviews);

        List<Review> topReviews = new ArrayList<>();
        topReviews.addAll(staffReviews);
        topReviews.addAll(studentReviews);

        return topReviews;
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

    private void removeIfExceedCriteria(List<Review> reviews) {
        if(reviews.size() <= TOP_REVIEW_NUMBER_OF_CRITERIA) return;

//        List<Review> cutReviews = new ArrayList<>();
        for(int i=0; i<TOP_REVIEW_NUMBER_OF_CRITERIA; i++){
            reviews.remove(reviews.size()-1);
            if(reviews.size() <= TOP_REVIEW_NUMBER_OF_CRITERIA) break;
        }
    }

    /**
     * 리뷰 묶음을 학생식당과 교직원 식당으로 나눈다.
     * @param restaurantReviews
     * @param staffReviews
     * @param studentReviews
     */
    private void splitReviews(List<Review> restaurantReviews, List<Review> staffReviews, List<Review> studentReviews) {
        for (Review restaurantReview : restaurantReviews) {
            if(restaurantReview.getMenu().getDept().equals(Dept.STAFF)){
                staffReviews.add(restaurantReview);
            }else{
                studentReviews.add(restaurantReview);
            }
        }
    }
}
