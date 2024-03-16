package com.SeeAndYouGo.SeeAndYouGo.Review;

import com.SeeAndYouGo.SeeAndYouGo.AOP.ValidateToken;
import com.SeeAndYouGo.SeeAndYouGo.Menu.MenuController;
import com.SeeAndYouGo.SeeAndYouGo.OAuth.jwt.TokenProvider;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import com.SeeAndYouGo.SeeAndYouGo.Review.dto.ReviewDeleteResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.Review.dto.ReviewRequestDto;
import com.SeeAndYouGo.SeeAndYouGo.Review.dto.ReviewResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.like.LikeService;
import com.SeeAndYouGo.SeeAndYouGo.user.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ReviewController {

    private final ReviewService reviewService;
    private final TokenProvider tokenProvider;
    private final UserService userService;
    private final LikeService likeService;
    private static final Integer REPORT_CRITERION = 10;
    private static final List<String> restaurantNames = List.of("1학생회관", "2학생회관", "3학생회관", "상록회관", "생활과학대");

    // 탑 리뷰 조회
    @GetMapping(value = "/top-review/{restaurant}")
    public ResponseEntity<List<ReviewResponseDto>> getTopReviews(@PathVariable("restaurant") String restaurant) {
        String restaurantName = Restaurant.parseName(restaurant);
        String date = MenuController.getTodayDate();
        List<Review> reviews = reviewService.findTopReviewsByRestaurantAndDate(restaurantName, date);
        List<ReviewResponseDto> response = getReviewDtos(reviews, "");
        return ResponseEntity.ok(response);
    }

    private List<ReviewResponseDto> getReviewDtos(List<Review> reviews, String userEmail) {
        // userEmail이 빈 string이라면 로그인하지 않은 사용자!!
        List<ReviewResponseDto> response = new ArrayList<>();
        for(Review review: reviews){
            if(likeService.isLike(review, userEmail)){
                response.add(new ReviewResponseDto(review, true));
            }else{
                response.add(new ReviewResponseDto(review, false));
            }
        }
        return response;
    }

    @GetMapping(value = {"/total-review/{token_id}", "/total-review"})
    @ValidateToken
    public ResponseEntity<List<ReviewResponseDto>> getAllReviews(@PathVariable(value = "token_id", required = false) String tokenId) {
        String date = MenuController.getTodayDate();
        List<Review> allReviews = new ArrayList<>();
        String userEmail = tokenProvider.decodeToEmail(tokenId);
        for (String restaurantName : restaurantNames) {
            List<Review> restaurantReviews = reviewService.findRestaurantReviews(restaurantName, date);
            allReviews.addAll(restaurantReviews);
        }

        return ResponseEntity.ok(getReviewDtos(allReviews, userEmail));
    }

    @GetMapping(value = {"/review/{restaurant}/{token_id}", "/review/{restaurant}"})
    @ValidateToken
    public ResponseEntity<List<ReviewResponseDto>> getRestaurantReviews(@PathVariable("restaurant") String restaurant,
                                                                        @PathVariable(value = "token_id", required = false) String tokenId) {
        String date = MenuController.getTodayDate();
        String restaurantName = Restaurant.parseName(restaurant);
        List<Review> restaurantReviews = reviewService.findRestaurantReviews(restaurantName, date);
        String userEmail = tokenProvider.decodeToEmail(tokenId);
        return ResponseEntity.ok(getReviewDtos(restaurantReviews, userEmail));
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
    @ValidateToken
    public ResponseEntity<Long> postReview(
            @RequestParam("restaurant") String restaurant,
            @RequestParam("dept") String dept,
            @RequestParam("menuName") String menuName,
            @RequestParam("rate") Double rate,
            @RequestParam("writer") String tokenId,
            @RequestParam("comment") String comment,
            @RequestParam("anonymous") boolean anonymous,
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

        // 원하는 날짜 및 시간 형식을 정의합니다.
        String email = tokenProvider.decodeToEmail(tokenId);
        String nickname = userService.findNickname(email);
        String restaurantName = Restaurant.parseName(restaurant);

        ReviewRequestDto reviewDto = ReviewRequestDto.builder()
                .restaurant(restaurantName)
                .dept(dept)
                .menuName(menuName)
                .rate(rate)
                .writer(email)
                .nickName(anonymous ? "익명" : nickname)
                .comment(comment)
                .imgUrl(imgUrl)
                .build();

        Long reviewId = reviewService.registerReview(reviewDto);

        return new ResponseEntity<>(reviewId, HttpStatus.CREATED);
    }

    @GetMapping("/reviews/{token}")
    @ValidateToken
    public ResponseEntity<List<ReviewResponseDto>> getReviewsByUser(@PathVariable("token") String tokenId){
        String userEmail = tokenProvider.decodeToEmail(tokenId);
        List<Review> reviews = reviewService.findReviewsByWriter(userEmail);

        return ResponseEntity.ok(getReviewDtos(reviews, userEmail));
    }

    @DeleteMapping("/reviews/{reviewId}/{token}")
    @ValidateToken
    public ReviewDeleteResponseDto deleteReview(
            @PathVariable("reviewId") Long reviewId,
            @PathVariable("token") String tokenId){

        ReviewDeleteResponseDto responseDto = ReviewDeleteResponseDto.builder()
                .success(false)
                .build();
        try{
            String userEmail = tokenProvider.decodeToEmail(tokenId);
            boolean isWriter = reviewService.deleteReview(userEmail, reviewId);
            if(isWriter){
                responseDto = ReviewDeleteResponseDto.builder()
                        .success(true)
                        .build();
            }
        }catch (ArrayIndexOutOfBoundsException e){
            return responseDto;
        }
        return responseDto;
    }
}
