package com.SeeAndYouGo.SeeAndYouGo.Review;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 탑 리뷰 조회
    @GetMapping("/{restaurant}/review/{date}")
    public ResponseEntity<List<Review>> getTopReviews(@PathVariable String restaurant, @PathVariable String date) {
        List<Review> reviews = reviewService.findTopReviewsByRestaurantAndDate(restaurant, date);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping
    public List<Review> getAllReviews() {
        return reviewService.findAllReviews();
    }

    // 리뷰 게시
    @PostMapping("/review")
    public ResponseEntity<Long> postReview(@RequestBody ReviewResponse requestDto) {
        Review review = new Review();
        review.setWriter(requestDto.getWriter());
        review.setReviewRate(requestDto.getRate());
        review.setComment(requestDto.getComment());
        review.setImgLink(requestDto.getImage());

        review.setMadeTime(requestDto.getMadeTime()); // 문자열 형태의 madeTime을 그대로 전달

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