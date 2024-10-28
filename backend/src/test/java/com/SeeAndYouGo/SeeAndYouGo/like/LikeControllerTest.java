package com.SeeAndYouGo.SeeAndYouGo.like;

import com.SeeAndYouGo.SeeAndYouGo.TestSetUp;
import com.SeeAndYouGo.SeeAndYouGo.dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishRepository;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishType;
import com.SeeAndYouGo.SeeAndYouGo.like.dto.LikeResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.menu.Dept;
import com.SeeAndYouGo.SeeAndYouGo.menu.Menu;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuRepository;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuType;
import com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt.TokenProvider;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import com.SeeAndYouGo.SeeAndYouGo.review.Review;
import com.SeeAndYouGo.SeeAndYouGo.review.ReviewController;
import com.SeeAndYouGo.SeeAndYouGo.review.ReviewRepository;
import com.SeeAndYouGo.SeeAndYouGo.review.dto.ReviewResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.user.Social;
import com.SeeAndYouGo.SeeAndYouGo.user.UserRepository;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@Transactional
@TestPropertySource(properties = "spring.aop.auto=false") // 테스트코드에서는 aop를 사용하지 않음.
public class LikeControllerTest {
    @Autowired
    private LikeController likeController;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private ReviewController reviewController;

    @MockBean
    private TokenProvider tokenProvider;

    private final String writerEmail = "writer@daum.net";
    private final String writerToken = "writer";
    private final String writerNickname = "writer";
    private final Social writerSocial = Social.KAKAO;
    private final String notWriterEmail = "not-writer@daum.net";
    private final String notWriterToken = "not-writer";
    private final String notWriterNickname = "not-writer";
    private final Social notWriterSocial = Social.KAKAO;

    private final String mainDishName = "김치찌개";
    private final String sideDishName = "현미밥";

    private final Integer menuPrice = 4000;
    private final LocalDate menuDate = LocalDate.now();
    private final Dept menuDept = Dept.STUDENT;
    private final MenuType menuMenuType = MenuType.BREAKFAST;
    private final Restaurant menuRestaurant = Restaurant.제2학생회관;

    private final String reviewNickname = "익명";
    private final LocalDateTime reviewCreateTime = LocalDateTime.now();
    private final Integer reviewLikeCnt = 0;
    private final String reviewComment = "존맛탱";
    private final String reviewImgUrl = null;
    private final double reviewRate = 3.5;
    private final Restaurant reviewRestaurant = Restaurant.제2학생회관;
    private final Integer reviewReportCnt = 0;

    @BeforeEach
    public void init() {
        // decodeToEmail 수동 설정.
        TestSetUp.stubDecodeToEmail(writerEmail, tokenProvider, writerToken);
        TestSetUp.stubDecodeToEmail(notWriterEmail, tokenProvider, notWriterToken);

        // user 저장
        TestSetUp.saveUser(userRepository, writerEmail, writerNickname, writerSocial);
        TestSetUp.saveUser(userRepository, notWriterEmail, notWriterNickname, notWriterSocial);

        // dish 저장
        Dish 김치찌개 = TestSetUp.saveDish(dishRepository, mainDishName, DishType.MAIN);
        Dish 현미밥 = TestSetUp.saveDish(dishRepository, sideDishName, DishType.SIDE);

        // menu 저장
        Menu menu = TestSetUp.saveMenu(menuRepository, menuPrice, menuDate, menuDept, menuMenuType, menuRestaurant, 김치찌개, 현미밥);

        // review 저장
        TestSetUp.saveReview(reviewRepository, writerEmail, reviewNickname, reviewCreateTime, reviewLikeCnt,
                menu, reviewComment, reviewImgUrl, reviewRate, reviewRestaurant, reviewReportCnt);
    }

    @DisplayName("자신 공감 테스트")
    @Test
    void 자신_공감() {
        // when
        Review review = reviewRepository.findAll().get(0); // 리뷰는 1개밖에 없으므로 .get(0) 함.
        LikeResponseDto likeResponse = likeController.postLikeCount(review.getId(), writerToken);

        // then
        // 자신의 리뷰이므로 공감할 수 없다.
        Assert.assertEquals(likeResponse.isLike(), false);
        Assert.assertEquals(likeResponse.isMine(), true);
    }

    @DisplayName("공감 테스트")
    @Test
    void 공감() {
        // when
        // test2 유저가 좋아요를 누른다.
        Review review = reviewRepository.findAll().get(0); // 리뷰는 1개밖에 없으므로 .get(0) 함.
        LikeResponseDto likeResponse = likeController.postLikeCount(review.getId(), notWriterToken);// 리뷰는 1개밖에 없으므로 ID가 1일 것이다.

        // then
        // 공감 성공
        Assert.assertEquals(likeResponse.isLike(), true);
        Assert.assertEquals(likeResponse.isMine(), false);

        // 로그인하여 리뷰 조회 시, 공감 표시
        // test2 유저가 바라보는 리뷰 목록 조회
        List<ReviewResponseDto> restaurantReviews = reviewController.getRestaurantReviews(reviewRestaurant.toString(), notWriterToken);
        ReviewResponseDto reviewResponseDto = restaurantReviews.get(0);

        Assert.assertEquals(reviewResponseDto.isLike(), true);
    }

    @DisplayName("공감 취소 테스트")
    @Test
    void 공감_취소() {
        // given
        // test2 유저가 좋아요를 누른다.
        Review review = reviewRepository.findAll().get(0); // 리뷰는 1개밖에 없으므로 .get(0) 함.
        likeController.postLikeCount(review.getId(), notWriterToken);// 리뷰는 1개밖에 없으므로 ID가 1일 것이다.

        // when
        // test2 유저가 좋아요를 다시 누른다.
        LikeResponseDto likeResponse = likeController.postLikeCount(review.getId(), notWriterToken);// 리뷰는 1개밖에 없으므로 ID가 1일 것이다.

        // then
        // 공감 취소 성공
        Assert.assertEquals(likeResponse.isLike(), false);
        Assert.assertEquals(likeResponse.isMine(), false);

        // 로그인하여 리뷰 조회 시, 공감 표시 없음
        // test2 유저가 바라보는 리뷰 목록 조회
        List<ReviewResponseDto> restaurantReviews = reviewController.getRestaurantReviews(reviewRestaurant.toString(), notWriterToken);
        ReviewResponseDto reviewResponseDto = restaurantReviews.get(0);

        Assert.assertEquals(reviewResponseDto.isLike(), false);
    }
}
