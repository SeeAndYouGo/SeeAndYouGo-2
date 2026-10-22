package com.SeeAndYouGo.SeeAndYouGo.review;

import com.SeeAndYouGo.SeeAndYouGo.global.response.ApiResponse;
import com.SeeAndYouGo.SeeAndYouGo.like.LikeService;
import com.SeeAndYouGo.SeeAndYouGo.global.exception.ApiException;
import com.SeeAndYouGo.SeeAndYouGo.global.exception.ErrorCode;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuController;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import com.SeeAndYouGo.SeeAndYouGo.review.dto.ReviewDeleteResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.review.dto.ReviewRequestDto;
import com.SeeAndYouGo.SeeAndYouGo.review.dto.ReviewResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.user.AuthorizationService;
import com.SeeAndYouGo.SeeAndYouGo.user.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
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

import static com.SeeAndYouGo.SeeAndYouGo.review.ImageConstants.*;

@Slf4j
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class ReviewController {

    private static final String SORT_TOP = "top";
    private static final String REVIEW_POSTED_MESSAGE = "리뷰가 등록되었습니다.";
    private static final String REVIEW_DELETED_MESSAGE = "리뷰가 삭제되었습니다.";

    private final ReviewService reviewService;
    private final UserService userService;
    private final LikeService likeService;
    private final AuthorizationService authorizationService;
    private final Executor executor;

    public ReviewController(ReviewService reviewService, UserService userService, LikeService likeService,
                            AuthorizationService authorizationService, @Qualifier("asyncTaskExecutor") Executor executor) {
        this.reviewService = reviewService;
        this.userService = userService;
        this.likeService = likeService;
        this.authorizationService = authorizationService;
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

    /**
     * 오늘의 리뷰를 조회한다.
     * restaurant가 없으면 전 식당, 있으면 해당 식당의 리뷰를 반환하고, sort=top이면 해당 식당의 탑 리뷰를 반환한다.
     */
    @GetMapping("/reviews")
    public List<ReviewResponseDto> getReviews(@RequestParam(value = "restaurant", required = false) String restaurant,
                                              @RequestParam(value = "sort", required = false) String sort,
                                              @Parameter(hidden = true) @AuthenticationPrincipal String email) {
        if (sort != null) {
            validateTopSort(restaurant, sort);
            return getTopReviews(restaurant);
        }
        if (restaurant == null) {
            return getAllReviews(email);
        }
        return getRestaurantReviews(restaurant, email);
    }

    private void validateTopSort(String restaurant, String sort) {
        if (!SORT_TOP.equals(sort)) {
            throw new ApiException(ErrorCode.INVALID_INPUT_VALUE, "지원하지 않는 정렬 방식입니다: " + sort);
        }
        if (restaurant == null) {
            throw new ApiException(ErrorCode.INVALID_INPUT_VALUE, "탑 리뷰는 식당을 지정해야 조회할 수 있습니다.");
        }
    }

    private List<ReviewResponseDto> getAllReviews(String email) {
        String date = MenuController.getTodayDate();
        List<Review> allReviews = new ArrayList<>();
        for (Restaurant restaurant : Restaurant.values()) {
            List<Review> restaurantReviews = reviewService.findRestaurantReviews(restaurant.toString(), date);
            allReviews.addAll(restaurantReviews);
        }

        return getReviewDtos(allReviews, email);
    }

    // 탑 리뷰 조회
    private List<ReviewResponseDto> getTopReviews(String restaurant) {
        String restaurantName = Restaurant.parseName(restaurant);
        String date = MenuController.getTodayDate();
        List<Review> reviews = reviewService.findTopReviewsByRestaurantAndDate(restaurantName, date);

        return getReviewDtos(reviews, "");
    }

    private List<ReviewResponseDto> getRestaurantReviews(String restaurant, String email) {
        String date = MenuController.getTodayDate();
        List<Review> restaurantReviews = reviewService.findRestaurantReviews(restaurant, date);
        return getReviewDtos(restaurantReviews, email);
    }

    // 리뷰 게시
    @PostMapping(value = "/reviews")
    public ApiResponse<Long> postReview(@RequestPart(value = "dto") ReviewRequestDto dto,
                                        @RequestPart(value = "image", required = false) MultipartFile image,
                                        @Parameter(hidden = true) @AuthenticationPrincipal String email) {
        authorizationService.getLoginUser(email);
        String nickname = userService.getNicknameByEmail(email);

        String imgUrl = "";
        if (image != null) {
            String imgName = UUID.randomUUID() + LocalDateTime.now().toString().replace(".", "").replace(":", "") + ".png";  // 테스트 완료: jpg 업로드 후 png 임의저장해도 잘 보여짐!
            File file = createTempFileFromMultipart(image);
            saveImage(file, imgName);
            imgUrl = IMAGE_API_PREFIX + imgName;
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

        return ApiResponse.success(reviewService.registerReview(data), REVIEW_POSTED_MESSAGE);
    }

    private File createTempFileFromMultipart(MultipartFile image) {
        File dir = new File(TEMP_IMAGE_DIR);
        if (!dir.exists()) {
            dir.mkdirs(); // 디렉토리 생성
        }

        File file = new File(String.format("%s/%s.png", dir.getPath(), UUID.randomUUID()));
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(image.getBytes());
        } catch (IOException e) {
            log.error("Failed to save temporary image file: {}", file.getPath(), e);
            throw new RuntimeException(e);
        }

        return file;
    }

    private void saveImage(File image, String imgName) {
        Runnable runnable = () -> {
            try {
                Files.createDirectories(Paths.get(IMAGE_STORAGE_DIR));
                Path targetPath = Paths.get(IMAGE_STORAGE_DIR, imgName);
                BufferedImage resized = reviewService.resize(image);
                ImageIO.write(resized, "png", new File(targetPath.toUri()));
                image.delete();
            } catch (Exception e) {
                log.error("[리뷰업로드] 오류 {}", e.getMessage());
            }
        };
        executor.execute(runnable);
    }

    @ResponseBody
    @GetMapping("/images/{imgName}")
    public byte[] showImage(@PathVariable String imgName) throws Exception {
        // 파일명 검증
        if (imgName.contains("..") || imgName.contains("/") || imgName.contains("\\")) {
            throw new ApiException(ErrorCode.INVALID_INPUT_VALUE, "올바르지 않은 이미지 파일명입니다.");
        }

        Path imagePath = Paths.get(IMAGE_STORAGE_DIR, imgName).normalize();

        if (!Files.exists(imagePath)) {
            throw new FileNotFoundException("Image not found");
        }

        return Files.readAllBytes(imagePath);
    }

    @GetMapping("/reviews/me")
    public List<ReviewResponseDto> getReviewsByUser(@Parameter(hidden = true) @AuthenticationPrincipal String email){
        authorizationService.getLoginUser(email);
        List<Review> reviews = reviewService.findReviewsByWriter(email);

        return getReviewDtos(reviews, email);
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ApiResponse<ReviewDeleteResponseDto> deleteReview(
            @PathVariable("reviewId") Long reviewId,
            @Parameter(hidden = true) @AuthenticationPrincipal String email){
        authorizationService.getLoginUser(email);

        boolean isWriter = reviewService.deleteReview(email, reviewId);
        if(!isWriter){
            throw new ApiException(ErrorCode.REVIEW_DELETE_FORBIDDEN);
        }

        ReviewDeleteResponseDto response = ReviewDeleteResponseDto.builder()
                .success(true)
                .build();
        return ApiResponse.success(response, REVIEW_DELETED_MESSAGE);
    }
}
