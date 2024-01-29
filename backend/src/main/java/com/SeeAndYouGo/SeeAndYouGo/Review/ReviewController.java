package com.SeeAndYouGo.SeeAndYouGo.Review;

import com.SeeAndYouGo.SeeAndYouGo.Menu.MenuService;
import com.SeeAndYouGo.SeeAndYouGo.OAuth.jwt.TokenProvider;
import com.SeeAndYouGo.SeeAndYouGo.Review.dto.ReviewDeleteResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.Review.dto.ReviewRequestDto;
import com.SeeAndYouGo.SeeAndYouGo.Review.dto.ReviewResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.user.UserService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
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
    private final TokenProvider tokenProvider;
    private final UserService userService;
    private static final Integer REPORT_CRITERION = 10;

    // 탑 리뷰 조회
    @GetMapping("/topReview/{restaurant}")
    public ResponseEntity<List<ReviewResponseDto>> getTopReviews(@PathVariable String restaurant) {
        String restaurantName = menuService.parseRestaurantName(restaurant);
        String date = LocalDate.now().toString();
        List<Review> reviews = reviewService.findTopReviewsByRestaurantAndDate(restaurantName, date);
        List<ReviewResponseDto> response = getReviewDtos(reviews);

        return ResponseEntity.ok(response);
    }

    private static List<ReviewResponseDto> getReviewDtos(List<Review> reviews) {
        List<ReviewResponseDto> response = new ArrayList<>();
        reviews.forEach(review -> response.add(new ReviewResponseDto(review)));
        return response;
    }

    @GetMapping("/totalReview")
    public ResponseEntity<List<ReviewResponseDto>> getAllReviews() {
        String date = LocalDate.now().toString();
        List<Review> allReviews = reviewService.findAllReviews(date);

        return ResponseEntity.ok(getReviewDtos(allReviews));
    }

    @GetMapping("/review/{restaurant}")
    public ResponseEntity<List<ReviewResponseDto>> getRestaurantReviews(@PathVariable String restaurant) {
        String date = LocalDate.now().toString();
        String restaurantName = menuService.parseRestaurantName(restaurant);
        List<Review> restaurantReviews = reviewService.findRestaurantReviews(restaurantName, date);
        return ResponseEntity.ok(getReviewDtos(restaurantReviews));
    }

    @PutMapping("/report/{reviewId}")
    public ResponseEntity judgeDeleteReview(@PathVariable Long reviewId){
        Integer reportCount = reviewService.updateReportCount(reviewId);

        if(reportCount >= REPORT_CRITERION){
            reviewService.deleteById(reviewId);
        }

        return ResponseEntity.ok(HttpStatus.OK);
    }

    // 리뷰 게시
    @PostMapping(value = "/review")
    public ResponseEntity<Long> postReview(
            @RequestParam("restaurant") String restaurant,
            @RequestParam("dept") String dept,
            @RequestParam("menuName") String menuName,
            @RequestParam("rate") Double rate,
            @RequestParam("writer") String writer,
            @RequestParam("comment") String comment,
            @RequestParam(name="image", required = false) MultipartFile image) {

         NCloudObjectStorage NCloudObjectStorage = new NCloudObjectStorage();
        String imgUrl = "";
         if (image != null) {
             try {
                 imgUrl = NCloudObjectStorage.imgUpload(image.getInputStream(), image.getContentType());
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }

        Review review = new Review();
        // 원하는 날짜 및 시간 형식을 정의합니다.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss");
        String email = tokenProvider.decode(writer);
        String nickname = userService.findNickname(email);
        review.setWriterEmail(email);
        review.setWriterNickname(nickname);
        review.setReviewRate(rate);
        review.setComment(comment);
        review.setImgLink(imgUrl);
        review.setLikeCount(0);
        review.setMadeTime(LocalDateTime.now().format(formatter)); // 문자열 형태의 madeTime을 그대로 전달

        Long reviewId = reviewService.registerReview(review, restaurant, dept, menuName);

        return new ResponseEntity<>(reviewId, HttpStatus.CREATED);
    }

    @GetMapping("/reviews/{token}")
    public ResponseEntity<List<ReviewResponseDto>> getReviewsByUser(@PathVariable String token){
        String userEmail = tokenProvider.decode(token);
        List<Review> reviews = reviewService.findReviewsByWriter(userEmail);

        return ResponseEntity.ok(getReviewDtos(reviews));
    }

    @DeleteMapping("/reviews/{reviewId}/{token}")
    public ResponseEntity<ReviewDeleteResponseDto> deleteReview(
            @PathVariable("reviewId") Long reviewId,
            @PathVariable("token") String token){

        ReviewDeleteResponseDto responseDto = ReviewDeleteResponseDto.builder()
                .success(false)
                .build();
        String userEmail;
        try{
            userEmail = tokenProvider.decode(token);
        }catch (ArrayIndexOutOfBoundsException e){
            return ResponseEntity.ok(responseDto);
        }

        boolean isWriter = reviewService.deleteReview(userEmail, reviewId);
        if(isWriter){
            responseDto = ReviewDeleteResponseDto.builder()
                    .success(true)
                    .build();
        }

        return ResponseEntity.ok(responseDto);
    }
}
