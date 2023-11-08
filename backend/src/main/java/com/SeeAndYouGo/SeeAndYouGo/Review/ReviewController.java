package com.SeeAndYouGo.SeeAndYouGo.Review;

import com.SeeAndYouGo.SeeAndYouGo.Menu.MenuService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ReviewController {

    private final ReviewService reviewService;
    private final MenuService menuService;

    // 탑 리뷰 조회
    @GetMapping("/topReview/{restaurant}")
    public List<ReviewDto> getTopReviews(@PathVariable String restaurant) {
        String restaurantName = menuService.parseRestaurantName(restaurant);

        String date = LocalDate.now().toString();
        List<Review> reviews = reviewService.findTopReviewsByRestaurantAndDate(restaurantName, date);

        List<ReviewDto> response = getReviewDtos(reviews);

        return response;
    }

    private static List<ReviewDto> getReviewDtos(List<Review> reviews) {
        List<ReviewDto> response = new ArrayList<>();
        reviews.forEach(review -> response.add(ReviewDto.of(review)));
        return response;
    }

    @GetMapping("/totalReview")
    public List<ReviewDto> getAllReviews() {
        String date = LocalDate.now().toString();
        List<Review> allReviews = reviewService.findAllReviews(date);

        return getReviewDtos(allReviews);
    }

    @GetMapping("/review/{restaurant}")
    public List<ReviewDto> getRestaurantReviews(@PathVariable String restaurant) {
        String date = LocalDate.now().toString();
        String restaurantName = menuService.parseRestaurantName(restaurant);
        List<Review> restaurantReviews = reviewService.findRestaurantReviews(restaurantName, date);
        return getReviewDtos(restaurantReviews);
    }

    // 리뷰 게시
    @PostMapping(value = "/review", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Long> postReview(@RequestPart(value = "review") ReviewRequestDto requestDto, @RequestPart(value = "image",required = false) MultipartFile imgFile) throws Exception {
        Review review = new Review();
        NCloudObjectStorage NCloudObjectStorage = new NCloudObjectStorage();
        String imgUrl = "";
        if (imgFile != null){
            imgUrl = NCloudObjectStorage.imgUpload(imgFile.getInputStream(), imgFile.getContentType());
        }

        // 원하는 날짜 및 시간 형식을 정의합니다.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss");

        review.setWriter(requestDto.getWriter());
        review.setReviewRate(requestDto.getRate());
        review.setComment(requestDto.getComment());
        review.setImgLink(imgUrl);
        review.setLikeCount(0);

        review.setMadeTime(LocalDateTime.now().format(formatter)); // 문자열 형태의 madeTime을 그대로 전달

        Long reviewId = reviewService.registerReview(review, requestDto.getRestaurant());

        return new ResponseEntity<>(reviewId, HttpStatus.CREATED);
    }




    // 리뷰 삭제
//    @DeleteMapping("/review/{reviewId}")
//    public ResponseEntity<String> deleteReview(@PathVariable Long reviewId) {
//        Review review = reviewService.findOne(reviewId);
//        if (review == null) {
//            return ResponseEntity.notFound().build();
//        }
//        reviewService.delete(review);
//        return ResponseEntity.ok("Review deleted successfully.");
//    }
}
