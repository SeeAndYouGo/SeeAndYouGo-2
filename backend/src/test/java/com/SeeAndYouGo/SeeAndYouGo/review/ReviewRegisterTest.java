package com.SeeAndYouGo.SeeAndYouGo.review;

import com.SeeAndYouGo.SeeAndYouGo.dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishType;
import com.SeeAndYouGo.SeeAndYouGo.menu.Dept;
import com.SeeAndYouGo.SeeAndYouGo.menu.Menu;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuRepository;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuService;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuType;
import com.SeeAndYouGo.SeeAndYouGo.rate.RateRepository;
import com.SeeAndYouGo.SeeAndYouGo.rate.RateService;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 테스트 목표: 사용자가 "메뉴 리뷰 남기기"를 성공적으로 수행할 수 있다.
 *
 * ReviewService#registerReview 의 동작을 검증한다.
 *  - 리뷰가 ReviewRepository 를 통해 저장된다.
 *  - 저장된 Review 의 필드(작성자, 평점, 코멘트, 식당, 메뉴, 카운트, 작성시각)가 올바르다.
 *  - Menu 의 리뷰 리스트와 평균 평점이 갱신된다.
 *  - RateService 를 통해 식당/메뉴별 평점에 반영된다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("메뉴 리뷰 남기기 - ReviewService.registerReview")
class ReviewRegisterTest {

    @Mock private RateService rateService;
    @Mock private MenuService menuService;
    @Mock private ReviewRepository reviewRepository;
    @Mock private ReviewHistoryRepository reviewHistoryRepository;
    @Mock private RateRepository rateRepository;
    @Mock private MenuRepository menuRepository;
    @Mock private ReviewReader reviewReader;

    @InjectMocks private ReviewService reviewService;

    private Menu menu;
    private static final Long MENU_ID = 100L;
    private static final String EMAIL = "user@test.com";
    private static final String NICKNAME = "테스터";

    @BeforeEach
    void setUp() {
        menu = Menu.builder()
                .price(6000)
                .date("2025-11-01")
                .dept(Dept.STUDENT)
                .menuType(MenuType.LUNCH)
                .restaurant(Restaurant.제2학생회관)
                .isOpen(true)
                .build();

        Dish mainDish = Dish.builder()
                .name("김치찌개")
                .dishType(DishType.MAIN)
                .build();
        menu.addDish(mainDish);
    }

    @Test
    @DisplayName("정상 입력 시 리뷰가 저장되고, 작성자/평점/코멘트가 그대로 기록된다")
    void registerReview_savesReviewWithCorrectFields() {
        // given
        given(menuRepository.getReferenceById(MENU_ID)).willReturn(menu);
        ReviewData data = sampleReviewData(4.5, "맛있어요!", "");

        // when
        reviewService.registerReview(data);

        // then
        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(captor.capture());
        Review saved = captor.getValue();

        assertThat(saved.getWriterEmail()).isEqualTo(EMAIL);
        assertThat(saved.getWriterNickname()).isEqualTo(NICKNAME);
        assertThat(saved.getComment()).isEqualTo("맛있어요!");
        assertThat(saved.getReviewRate()).isEqualTo(4.5);
        assertThat(saved.getRestaurant()).isEqualTo(Restaurant.제2학생회관);
        assertThat(saved.getMenu()).isSameAs(menu);
        assertThat(saved.getLikeCount()).isZero();
        assertThat(saved.getReportCount()).isZero();
        assertThat(saved.getMadeTime()).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
    }

    @Test
    @DisplayName("리뷰 등록 시 메뉴의 리뷰 목록과 평균 평점이 갱신된다")
    void registerReview_updatesMenuReviewListAndRate() {
        // given
        given(menuRepository.getReferenceById(MENU_ID)).willReturn(menu);
        ReviewData data = sampleReviewData(4.0, "좋아요", "");

        // when
        reviewService.registerReview(data);

        // then
        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(captor.capture());
        Review saved = captor.getValue();

        assertThat(menu.getReviewList()).contains(saved);
        assertThat(menu.getRate()).isEqualTo(4.0);
    }

    @Test
    @DisplayName("리뷰 등록 시 RateService 로 식당/메뉴 평점 반영이 위임된다")
    void registerReview_delegatesRateUpdate() {
        // given
        given(menuRepository.getReferenceById(MENU_ID)).willReturn(menu);
        ReviewData data = sampleReviewData(3.5, "보통", "");

        // when
        reviewService.registerReview(data);

        // then
        verify(rateService).updateRateByRestaurant(Restaurant.제2학생회관, menu, 3.5);
    }

    @Test
    @DisplayName("이미지 URL이 있는 리뷰는 imgLink가 그대로 저장된다")
    void registerReview_withImage_keepsImgLink() {
        // given
        given(menuRepository.getReferenceById(MENU_ID)).willReturn(menu);
        ReviewData data = sampleReviewData(5.0, "최고!", "/api/images/abc.png");

        // when
        reviewService.registerReview(data);

        // then
        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(captor.capture());
        assertThat(captor.getValue().getImgLink()).isEqualTo("/api/images/abc.png");
    }

    @Test
    @DisplayName("익명 리뷰는 닉네임이 \"익명\"으로 저장된다")
    void registerReview_anonymous_savesAsAnonymous() {
        // given - Controller 에서 익명 여부에 따라 nickName을 "익명"으로 채워 전달함
        given(menuRepository.getReferenceById(MENU_ID)).willReturn(menu);
        ReviewData data = ReviewData.builder()
                .restaurant("제2학생회관")
                .menuId(MENU_ID)
                .dept("STUDENT")
                .menuName("김치찌개")
                .rate(3.0)
                .email(EMAIL)
                .nickName("익명")
                .comment("그저 그래요")
                .imgUrl("")
                .build();

        // when
        reviewService.registerReview(data);

        // then
        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(captor.capture());
        assertThat(captor.getValue().getWriterNickname()).isEqualTo("익명");
    }

    @Test
    @DisplayName("식당명이 정규화된 형태(예: \"1\")로 들어와도 등록에 성공한다")
    void registerReview_acceptsParsedRestaurantName() {
        // given - Controller가 Restaurant.parseName("1") => "제1학생회관" 형태로 변환해 넘긴다
        Menu menu1 = Menu.builder()
                .price(5000)
                .date("2025-11-01")
                .dept(Dept.KOREAN)
                .menuType(MenuType.LUNCH)
                .restaurant(Restaurant.제1학생회관)
                .isOpen(true)
                .build();
        given(menuRepository.getReferenceById(MENU_ID)).willReturn(menu1);

        ReviewData data = ReviewData.builder()
                .restaurant(Restaurant.parseName("1"))
                .menuId(MENU_ID)
                .dept("KOREAN")
                .menuName("된장찌개")
                .rate(4.5)
                .email(EMAIL)
                .nickName(NICKNAME)
                .comment("굿")
                .imgUrl("")
                .build();

        // when
        reviewService.registerReview(data);

        // then
        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(captor.capture());
        assertThat(captor.getValue().getRestaurant()).isEqualTo(Restaurant.제1학생회관);
    }

    @Test
    @DisplayName("존재하지 않는 식당명이 들어오면 IllegalArgumentException 으로 거부되고, 저장이 일어나지 않는다")
    void registerReview_invalidRestaurant_rejected() {
        // given
        ReviewData data = ReviewData.builder()
                .restaurant("없는식당")
                .menuId(MENU_ID)
                .dept("STUDENT")
                .menuName("김치찌개")
                .rate(4.0)
                .email(EMAIL)
                .nickName(NICKNAME)
                .comment("comment")
                .imgUrl("")
                .build();

        // expect
        assertThatThrownBy(() -> reviewService.registerReview(data))
                .isInstanceOf(IllegalArgumentException.class);

        verify(reviewRepository, never()).save(any());
        verify(rateService, never()).updateRateByRestaurant(any(), any(), any());
    }

    private ReviewData sampleReviewData(double rate, String comment, String imgUrl) {
        return ReviewData.builder()
                .restaurant("제2학생회관")
                .menuId(MENU_ID)
                .dept("STUDENT")
                .menuName("김치찌개")
                .rate(rate)
                .email(EMAIL)
                .nickName(NICKNAME)
                .comment(comment)
                .imgUrl(imgUrl)
                .build();
    }
}
