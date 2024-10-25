package com.SeeAndYouGo.SeeAndYouGo.like;

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
import com.SeeAndYouGo.SeeAndYouGo.user.User;
import com.SeeAndYouGo.SeeAndYouGo.user.UserRepository;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @MockBean
    private TokenProvider tokenProvider;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private ReviewController reviewController;

    @BeforeEach
    public void init() {
        // decodeToEmail 수동 설정.
        Mockito.doReturn("test1@daum.net")
                .when(tokenProvider)
                .decodeToEmail("test1");

        Mockito.doReturn("test2@daum.net")
                .when(tokenProvider)
                .decodeToEmail("test2");

        User user1 = User.builder()
                .email("test1@daum.net")
                .nickname("test1")
                .socialType(Social.KAKAO)
                .build();

        User user2 = User.builder()
                .email("test2@daum.net")
                .nickname("test2")
                .socialType(Social.KAKAO)
                .build();

        userRepository.save(user1);
        userRepository.save(user2);

        Dish 김치찌개 = Dish.builder()
                .name("김치찌개")
                .dishType(DishType.MAIN)
                .build();

        Dish 현미밥 = Dish.builder()
                .name("현미밥")
                .dishType(DishType.SIDE)
                .build();

        Menu menu = Menu.builder()
                .price(4000)
                .date(LocalDate.now().toString())
                .dept(Dept.STUDENT)
                .menuType(MenuType.BREAKFAST)
                .restaurant(Restaurant.제2학생회관)
                .build();

        List<Dish> dishes = new ArrayList<>();
        dishes.add(김치찌개);
        dishes.add(현미밥);

        menu.addDish(김치찌개);
        menu.addDish(현미밥);

        dishRepository.saveAll(dishes);
        menuRepository.save(menu);

        Review review = Review.builder()
                                .writerEmail("test1@daum.net")
                                .writerNickname("익명")
                                .madeTime(LocalDateTime.now().toString())
                                .likeCount(0)
                                .menu(menu)
                                .comment("존맛탱")
                                .imgLink(null)
                                .reviewRate(3.5)
                                .restaurant(Restaurant.제2학생회관)
                                .reportCount(0)
                                .build();

        reviewRepository.save(review);
    }

    @DisplayName("자신 공감 테스트")
    @Test
    void 자신_공감() {
        // when
        LikeResponseDto likeResponse = likeController.postLikeCount(1l, "test1");// 리뷰는 1개밖에 없으므로 ID가 1일 것이다.

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
        LikeResponseDto likeResponse = likeController.postLikeCount(1l, "test2");// 리뷰는 1개밖에 없으므로 ID가 1일 것이다.

        // then
        // 공감 성공
        Assert.assertEquals(likeResponse.isLike(), true);
        Assert.assertEquals(likeResponse.isMine(), false);

        // 로그인하여 리뷰 조회 시, 공감 표시
        // test2 유저가 바라보는 리뷰 목록 조회
        List<ReviewResponseDto> restaurantReviews = reviewController.getRestaurantReviews("제2학생회관", "test2");
        ReviewResponseDto reviewResponseDto = restaurantReviews.get(0);

        Assert.assertEquals(reviewResponseDto.isLike(), true);
    }

    @DisplayName("공감 취소 테스트")
    @Test
    void 공감_취소() {
        // given
        // test2 유저가 좋아요를 누른다.
        likeController.postLikeCount(1l, "test2");// 리뷰는 1개밖에 없으므로 ID가 1일 것이다.

        // when
        // test2 유저가 좋아요를 다시 누른다.
        LikeResponseDto likeResponse = likeController.postLikeCount(1l, "test2");// 리뷰는 1개밖에 없으므로 ID가 1일 것이다.

        // then
        // 공감 취소 성공
        Assert.assertEquals(likeResponse.isLike(), false);
        Assert.assertEquals(likeResponse.isMine(), false);

        // 로그인하여 리뷰 조회 시, 공감 표시 없음
        // test2 유저가 바라보는 리뷰 목록 조회
        List<ReviewResponseDto> restaurantReviews = reviewController.getRestaurantReviews("제2학생회관", "test2");
        ReviewResponseDto reviewResponseDto = restaurantReviews.get(0);

        Assert.assertEquals(reviewResponseDto.isLike(), false);
    }
}
