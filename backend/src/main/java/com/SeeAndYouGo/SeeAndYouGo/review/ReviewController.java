package com.SeeAndYouGo.SeeAndYouGo.review;

import com.SeeAndYouGo.SeeAndYouGo.like.LikeService;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuController;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import com.SeeAndYouGo.SeeAndYouGo.review.dto.ReportCountResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.review.dto.ReviewDeleteResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.review.dto.ReviewRequestDto;
import com.SeeAndYouGo.SeeAndYouGo.review.dto.ReviewResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.user.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

@Slf4j
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;
    private final LikeService likeService;
    private final Executor executor;

    public ReviewController(ReviewService reviewService, UserService userService, LikeService likeService, @Qualifier("asyncTaskExecutor") Executor executor) {
        this.reviewService = reviewService;
        this.userService = userService;
        this.likeService = likeService;
        this.executor = executor;
    }

    private List<ReviewResponseDto> getReviewDtos(List<Review> reviews, String userEmail) {
        // userEmail이 빈 string이라면 로그인하지 않은 사용자!!
        List<ReviewResponseDto> response = new ArrayList<>();
        boolean isLoggedIn = userEmail != null && !userEmail.isEmpty();
        for(Review review: reviews){
            boolean userLiked = isLoggedIn && likeService.isLike(review, userEmail);
            response.add(new ReviewResponseDto(review, userLiked));
        }
        return response;
    }

    @GetMapping("/total-review")
    public List<ReviewResponseDto> getAllReviews(@Parameter(hidden = true) @AuthenticationPrincipal String email) {
        String date = MenuController.getTodayDate();
        List<Review> allReviews = new ArrayList<>();
        for (Restaurant restaurant : Restaurant.values()) {
            List<Review> restaurantReviews = reviewService.findRestaurantReviews(restaurant.toString(), date);
            allReviews.addAll(restaurantReviews);
        }
        return getReviewDtos(allReviews, email);
    }

    // 탑 리뷰 조회
    @GetMapping(value = "/top-review/{restaurant}")
    public List<ReviewResponseDto> getTopReviews(@PathVariable("restaurant") String restaurant) {
        String restaurantName = Restaurant.parseName(restaurant);
        String date = MenuController.getTodayDate();
        List<Review> reviews = reviewService.findTopReviewsByRestaurantAndDate(restaurantName, date);
        return getReviewDtos(reviews, "");
    }

    @GetMapping("/review/{restaurant}")
    public List<ReviewResponseDto> getRestaurantReviews(@PathVariable("restaurant") String restaurant,
                                                        @Parameter(hidden = true) @AuthenticationPrincipal String email) {
        String date = MenuController.getTodayDate();
        List<Review> restaurantReviews = reviewService.findRestaurantReviews(restaurant, date);
        return getReviewDtos(restaurantReviews, email);
    }

    @PutMapping("/report/{reviewId}")
    public ReportCountResponseDto judgeDeleteReview(@PathVariable Long reviewId){
        log.info("Request to report review with ID: {}", reviewId);
        Integer reportCount = reviewService.updateReportCount(reviewId);
        log.info("Review ID: {} now has {} reports.", reviewId, reportCount);
        return new ReportCountResponseDto(reportCount);
    }

    // 리뷰 게시
    private static final String IMAGE_DIR = "imageStorage";
    @PostMapping(value = "/review")
    @ResponseStatus(HttpStatus.CREATED)
    public Long postReview(@RequestPart(value = "dto") ReviewRequestDto dto,
                           @RequestPart(value = "image", required = false) MultipartFile image,
                           @Parameter(hidden = true) @AuthenticationPrincipal String email) {
        log.info("Request to post a new review for restaurant: {}, menu: {}", dto.getRestaurant(), dto.getMenuName());
        String nickname = userService.findNickname(email);

        String imgUrl = "";
        if (image != null && !image.isEmpty()) {
            String imgName = UUID.randomUUID() + "_" + LocalDateTime.now().toString().replace(".", "").replace(":", "") + ".png";
            File file = createTempFileFromMultipart(image);
            saveImage(file, imgName);
            imgUrl = "/api/images/" + imgName;
            log.info("Image attached, URL will be: {}", imgUrl);
        }

        ReviewData data = ReviewData.builder()
                .restaurant(Restaurant.parseName(dto.getRestaurant()))
                .menuId(dto.getMenuId())
                .dept(dto.getDept())
                .menuName(dto.getMenuName())
                .rate(dto.getRate())
                .email(email)
                .nickName(dto.isAnonymous() ? "익명" : nickname)
                .comment(dto.getComment())
                .imgUrl(imgUrl)
                .build();

        Long reviewId = reviewService.registerReview(data);
        log.info("Successfully posted new review with ID: {}", reviewId);
        return reviewId;
    }

    private File createTempFileFromMultipart(MultipartFile image) {
        File dir = new File("./tmpImage");
        if (!dir.exists()) {
            dir.mkdirs(); // 디렉토리 생성
        }

        File file = new File(String.format("%s/%s.png", dir.getPath(), UUID.randomUUID()));
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(image.getBytes());
        } catch (IOException e) {
            log.error("Failed to create temp file from multipart file.", e);
            throw new RuntimeException(e);
        }
        return file;
    }

    private void saveImage(File image, String imgName) {
        Runnable runnable = () -> {
            try {
                log.info("Starting async image save for: {}", imgName);
                Files.createDirectories(Paths.get(IMAGE_DIR));
                Path targetPath = Paths.get(IMAGE_DIR, imgName);
                BufferedImage resized = reviewService.resize(image);
                ImageIO.write(resized, "png", new File(targetPath.toUri()));
                image.delete();
                log.info("Successfully saved and resized image: {}", imgName);
            } catch (Exception e) {
                log.error("Error during async image saving for '{}'", imgName, e);
            }
        };
        executor.execute(runnable);
    }

    @ResponseBody
    @GetMapping("/images/{imgName}")
    public byte[] showImage(@PathVariable String imgName) throws Exception {
        File file = new File(IMAGE_DIR + "/" + imgName);
        return Files.readAllBytes(file.toPath());
    }

    @GetMapping("/reviews/{token}")
    public List<ReviewResponseDto> getReviewsByUser(@Parameter(hidden = true) @AuthenticationPrincipal String email){
        List<Review> reviews = reviewService.findReviewsByWriter(email);
        return getReviewDtos(reviews, email);
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ReviewDeleteResponseDto deleteReview(
            @PathVariable("reviewId") Long reviewId,
            @Parameter(hidden = true) @AuthenticationPrincipal String email){
        log.info("Request to delete review ID: {} by the writer.", reviewId);
        ReviewDeleteResponseDto responseDto = ReviewDeleteResponseDto.builder().success(false).build();
        try{
            boolean isWriter = reviewService.deleteReview(email, reviewId);
            if(isWriter){
                responseDto = ReviewDeleteResponseDto.builder().success(true).build();
                log.info("Successfully deleted review ID: {} by the writer.", reviewId);
            } else {
                log.warn("Failed to delete review ID: {}. User is not the writer.", reviewId);
            }
        } catch (Exception e) {
            log.error("Error deleting review ID: {}", reviewId, e);
            throw new RuntimeException("Error deleting review ID: " + reviewId, e);
        }
        return responseDto;
    }

    @DeleteMapping("/review/report/{reviewId}")
    public ReviewDeleteResponseDto deleteReportedReview(@PathVariable("reviewId") Long reviewId){
        log.warn("Request to delete a reported review with ID: {}", reviewId);
        boolean result = reviewService.deleteReportedReview(reviewId);
        log.info("Reported review ID: {} deletion result: {}", reviewId, result);
        return new ReviewDeleteResponseDto(result);
    }
}
