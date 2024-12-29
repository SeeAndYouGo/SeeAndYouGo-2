package com.SeeAndYouGo.SeeAndYouGo.review;

import com.SeeAndYouGo.SeeAndYouGo.aop.InvalidTokenException;
import com.SeeAndYouGo.SeeAndYouGo.aop.ValidateToken;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuController;
import com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt.TokenProvider;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import com.SeeAndYouGo.SeeAndYouGo.review.dto.ReviewDeleteResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.review.dto.ReviewRequestDto;
import com.SeeAndYouGo.SeeAndYouGo.review.dto.ReviewResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.like.LikeService;
import com.SeeAndYouGo.SeeAndYouGo.user.UserService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
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
    private final TokenProvider tokenProvider;
    private final UserService userService;
    private final LikeService likeService;
    private static final Integer REPORT_CRITERION = 10;
    private final Executor executor;

    public ReviewController(ReviewService reviewService, TokenProvider tokenProvider, UserService userService, LikeService likeService, @Qualifier("asyncTaskExecutor") Executor executor) {
        this.reviewService = reviewService;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
        this.likeService = likeService;
        this.executor = executor;
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
    public List<ReviewResponseDto> getAllReviews(@PathVariable(value = "token_id", required = false) String tokenId) {
        String date = MenuController.getTodayDate();
        List<Review> allReviews = new ArrayList<>();
        String userEmail = tokenProvider.decodeToEmail(tokenId);
        for (Restaurant restaurant : Restaurant.values()) {
            List<Review> restaurantReviews = reviewService.findRestaurantReviews(restaurant.toString(), date);
            allReviews.addAll(restaurantReviews);
        }

        return getReviewDtos(allReviews, userEmail);
    }

    // 탑 리뷰 조회
    @GetMapping(value = "/top-review/{restaurant}")
    public List<ReviewResponseDto> getTopReviews(@PathVariable("restaurant") String restaurant) {
        String restaurantName = Restaurant.parseName(restaurant);
        String date = MenuController.getTodayDate();
        List<Review> reviews = reviewService.findTopReviewsByRestaurantAndDate(restaurantName, date);

        return getReviewDtos(reviews, "");
    }

    @GetMapping(value = {"/review/{restaurant}/{token_id}", "/review/{restaurant}"})
    @ValidateToken
    public List<ReviewResponseDto> getRestaurantReviews(@PathVariable("restaurant") String restaurant,
                                                                        @PathVariable(value = "token_id", required = false) String tokenId) {
        String date = MenuController.getTodayDate();
        List<Review> restaurantReviews = reviewService.findRestaurantReviews(restaurant, date);
        String userEmail = tokenProvider.decodeToEmail(tokenId);
        return getReviewDtos(restaurantReviews, userEmail);
    }

    @PutMapping("/report/{reviewId}")
    public String judgeDeleteReview(@PathVariable Long reviewId){
        Integer reportCount = reviewService.updateReportCount(reviewId);

        if(reportCount >= REPORT_CRITERION){
            reviewService.deleteById(reviewId);
        }

        return "The Review report was successful.";
    }

    // 리뷰 게시
    private static final String IMAGE_DIR = "imageStorage";
    @PostMapping(value = "/review")
    @ResponseStatus(HttpStatus.CREATED)
    public Long postReview(@RequestPart(value = "dto") ReviewRequestDto dto,
                                           @RequestPart(value = "image", required = false) MultipartFile image) {
        String tokenId = dto.getWriter();
        if (!tokenProvider.validateToken(tokenId)) throw new InvalidTokenException("Invalid Token");
        String email = tokenProvider.decodeToEmail(tokenId);
        String nickname = userService.findNickname(email);

        String imgUrl = "";
        if (image != null) {
            String imgName = UUID.randomUUID() + LocalDateTime.now().toString().replace(".", "").replace(":", "") + ".png";  // 테스트 완료: jpg 업로드 후 png 임의저장해도 잘 보여짐!
            File file = createTempFileFromMultipart(image);
            saveImage(file, imgName);
            imgUrl = "/api/images/" + imgName;
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

        return reviewService.registerReview(data);
    }

    private File createTempFileFromMultipart(MultipartFile image) {
        File dir = new File("./tmpImage");

        File file = new File(String.format("%s/%s.jpg", dir.getPath(), UUID.randomUUID()));
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(image.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return file;
    }

    private void saveImage(File image, String imgName) {
        Runnable runnable = () -> {
            try {
                Files.createDirectories(Paths.get(IMAGE_DIR));
                Path targetPath = Paths.get(IMAGE_DIR, imgName);
                BufferedImage resized = reviewService.resize(image);
                ImageIO.write(resized, "jpg", new File(targetPath.toUri()));
                image.delete();
            } catch (Exception e) {
                log.error("[리뷰업로드] 오류 {}", e.getMessage());
            }
        };
        executor.execute(runnable);
    }

    @ResponseBody
    @GetMapping("/images/{imgName}")
    @Cacheable(value="review:image:", key="#imgName")
    public byte[] showImage(@PathVariable String imgName) throws Exception {
        File file = new File(IMAGE_DIR + "/" + imgName);
        return Files.readAllBytes(file.toPath());
    }

    @GetMapping("/reviews/{token}")
    @ValidateToken
    public List<ReviewResponseDto> getReviewsByUser(@PathVariable("token") String tokenId){
        String userEmail = tokenProvider.decodeToEmail(tokenId);
        List<Review> reviews = reviewService.findReviewsByWriter(userEmail);

        return getReviewDtos(reviews, userEmail);
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
