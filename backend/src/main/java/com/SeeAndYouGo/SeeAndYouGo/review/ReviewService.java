package com.SeeAndYouGo.SeeAndYouGo.review;

import com.SeeAndYouGo.SeeAndYouGo.menu.Dept;
import com.SeeAndYouGo.SeeAndYouGo.menu.Menu;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuRepository;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuService;
import com.SeeAndYouGo.SeeAndYouGo.rate.Rate;
import com.SeeAndYouGo.SeeAndYouGo.rate.RateRepository;
import com.SeeAndYouGo.SeeAndYouGo.rate.RateService;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import lombok.RequiredArgsConstructor;
import org.imgscalr.Scalr;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewService {
    private final RateService rateService;
    private final MenuService menuService;
    private final ReviewRepository reviewRepository;
    private final ReviewHistoryRepository reviewHistoryRepository;
    private final RateRepository rateRepository;
    private final MenuRepository menuRepository;
    private static final int TOP_REVIEW_NUMBER_OF_CRITERIA = 3; // top-review에서 각 DEPT별 리뷰를 몇개까지 살릴 것인가?
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional
    public Long registerReview(ReviewData data) {
        LocalDateTime time = LocalDateTime.now();

        Restaurant restaurant = Restaurant.valueOf(data.getRestaurant());
        Objects.requireNonNull(restaurant, "Restaurant not fount for name: " + data.getRestaurant());

        // 연관관계 존재
//        Menu menu = findMenuByRestaurantAndDept(restaurant, Dept.valueOf(data.getDept()), data.getMenuName());
        Menu menu = menuRepository.getReferenceById(data.getMenuId());
        Review review = Review.createEntity(data, restaurant, menu, time.format(formatter));
        menu.addReviewAndUpdateRate(review);
        rateService.updateRateByRestaurant(restaurant, menu, data.getRate());
        reviewRepository.save(review);

        return review.getId();
    }

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
        Restaurant restaurant = Restaurant.valueOf(Restaurant.parseName(restaurantName));

        if(restaurant.equals(Restaurant.제1학생회관)){
            // 1학의 경우 아래의 로직대로 하면 너무 오래 걸리므로 그냥 1학 리뷰는 싹다 가져오게 진행한다.
            return reviewRepository.findByRestaurant(restaurant);
        }

        List<Menu> menus = menuRepository.findByRestaurantAndDate(restaurant, date);

        List<Menu> param = new ArrayList<>();
        // menus의 각 menu에서 mainDish에 해당하는 Dish를 갖고 있는 다른 menu들도 불러온다.
        for (Menu menu : menus) {
            param.addAll(menuService.findAllMenuByMainDish(menu));
        }
        return reviewRepository.findByRestaurantAndMenuIn(restaurant, param);
//        return reviewRepository.findRestaurantReviews(restaurant.getId(), date);
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

        review.getMenu().deleteReview(review);

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
    public boolean deleteReview(String userEmail, Long reviewId) {
        Review review = reviewRepository.findById(reviewId).get();
        Restaurant restaurant = review.getRestaurant();

        if(review.getWriterEmail().equals(userEmail)){
            deleteById(reviewId);

            Rate rateByRestaurant = rateRepository.findByRestaurantAndDept(restaurant, review.getMenu().getDept().toString());
            rateByRestaurant.exceptRate(review.getReviewRate());

            // 1학의 경우 실제 dept를 가지고 있는 데이터도 갱신하지만, 각 메뉴에 대한 정보를 갖고 있는 데이터에도 반영해야한다.
            if(restaurant.equals(Restaurant.제1학생회관)){
                Rate rateByMenu = rateRepository.findByRestaurantAndDept(restaurant, review.getMenu().getMenuName());
                rateByMenu.exceptRate(review.getReviewRate());
            }

            return true;
        }

        return false;
    }

    public BufferedImage resize(File file) throws Exception {
        BufferedImage bi = ImageIO.read(file);

        int originalWidth = bi.getWidth();
        int originalHeight = bi.getHeight();

        int targetWidth = originalWidth;
        int targetHeight = (originalWidth * 3) / 4;

        if (targetHeight > originalHeight) {
            targetHeight = originalHeight;
            targetWidth = (originalHeight * 4) / 3;
        }

        int x = (originalWidth - targetWidth) / 2;
        int y = (originalHeight - targetHeight) / 2;

        BufferedImage croppedImage = bi.getSubimage(x, y, targetWidth, targetHeight);

        // 리사이즈해서 리턴
        return resizeImage(croppedImage, 800, 600);
    }

    BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws Exception {
        return Scalr.resize(originalImage, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_EXACT, targetWidth, targetHeight, Scalr.OP_ANTIALIAS);
    }
}