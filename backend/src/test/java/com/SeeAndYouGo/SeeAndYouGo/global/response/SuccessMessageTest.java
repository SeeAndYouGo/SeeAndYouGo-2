package com.SeeAndYouGo.SeeAndYouGo.global.response;

import com.SeeAndYouGo.SeeAndYouGo.dish.DishController;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishService;
import com.SeeAndYouGo.SeeAndYouGo.dish.dto.DishRequestDto;
import com.SeeAndYouGo.SeeAndYouGo.keyword.KeywordController;
import com.SeeAndYouGo.SeeAndYouGo.keyword.KeywordService;
import com.SeeAndYouGo.SeeAndYouGo.keyword.dto.KeywordAddResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.keyword.dto.KeywordRequestDto;
import com.SeeAndYouGo.SeeAndYouGo.keyword.dto.KeywordResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.like.LikeService;
import com.SeeAndYouGo.SeeAndYouGo.review.ReviewController;
import com.SeeAndYouGo.SeeAndYouGo.review.ReviewService;
import com.SeeAndYouGo.SeeAndYouGo.review.dto.ReviewRequestDto;
import com.SeeAndYouGo.SeeAndYouGo.user.AuthorizationService;
import com.SeeAndYouGo.SeeAndYouGo.user.UserController;
import com.SeeAndYouGo.SeeAndYouGo.user.UserService;
import com.SeeAndYouGo.SeeAndYouGo.user.dto.UserNicknameRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * 성공 응답의 message가 처리 결과에 맞는 문구로 내려가는지 검증한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("성공 메시지")
class SuccessMessageTest {

    private static final String EMAIL = "user@test.com";

    @Mock private ReviewService reviewService;
    @Mock private UserService userService;
    @Mock private LikeService likeService;
    @Mock private DishService dishService;
    @Mock private KeywordService keywordService;
    @Mock private AuthorizationService authorizationService;
    @Mock private Executor executor;

    private ReviewController reviewController;
    private DishController dishController;
    private UserController userController;
    private KeywordController keywordController;

    @BeforeEach
    void setUp() {
        reviewController = new ReviewController(reviewService, userService, likeService, authorizationService, executor);
        dishController = new DishController(dishService, authorizationService);
        userController = new UserController(userService, authorizationService);
        keywordController = new KeywordController(keywordService, authorizationService);
        ReflectionTestUtils.setField(keywordController, "maxKeywordsPerUser", 10);
    }

    @Test
    @DisplayName("리뷰 작성·삭제")
    void review() {
        ReviewRequestDto dto = mock(ReviewRequestDto.class);
        given(dto.getRestaurant()).willReturn("2");
        given(reviewService.registerReview(any())).willReturn(512L);
        given(reviewService.deleteReview(EMAIL, 512L)).willReturn(true);

        ApiResponse<Long> posted = reviewController.postReview(dto, null, EMAIL);
        assertThat(posted.getMessage()).isEqualTo("리뷰가 등록되었습니다.");
        assertThat(posted.getData()).isEqualTo(512L);

        assertThat(reviewController.deleteReview(512L, EMAIL).getMessage()).isEqualTo("리뷰가 삭제되었습니다.");
    }

    @Test
    @DisplayName("닉네임 변경: 변경되면 전용 문구, 변경 주기 제한이면 기본 문구")
    void nickname() {
        UserNicknameRequest request = mock(UserNicknameRequest.class);

        given(userService.canUpdateNickname(EMAIL)).willReturn(true);
        assertThat(userController.changeNickname(request, EMAIL).getMessage()).isEqualTo("닉네임이 변경되었습니다.");

        given(userService.canUpdateNickname(EMAIL)).willReturn(false);
        assertThat(userController.changeNickname(request, EMAIL).getMessage()).isEqualTo(ApiResponse.SUCCESS_MESSAGE);
    }

    @Test
    @DisplayName("관리자 메뉴: 메뉴명 변경·변경 없음·삭제·대표 메뉴 지정")
    void adminDish() {
        DishRequestDto request = mock(DishRequestDto.class);
        given(request.getId()).willReturn(301L);
        given(request.getChangeName()).willReturn("수제 돈까스");

        given(dishService.updateDishName(301L, "수제 돈까스")).willReturn(true);
        assertThat(dishController.dishUpdateName(request, EMAIL).getMessage()).isEqualTo("메뉴명이 변경되었습니다.");

        given(dishService.updateDishName(301L, "수제 돈까스")).willReturn(false);
        assertThat(dishController.dishUpdateName(request, EMAIL).getMessage()).isEqualTo("변경된 내용이 없습니다.");

        assertThat(dishController.dishDelete(1L, EMAIL).getMessage()).isEqualTo("메뉴가 삭제되었습니다.");
        assertThat(dishController.updateMainDish(new ArrayList<>(), EMAIL).getMessage()).isEqualTo("대표 메뉴가 반영되었습니다.");
    }

    @Test
    @DisplayName("키워드: 등록·최대 개수 초과·삭제")
    void keyword() {
        KeywordRequestDto request = mock(KeywordRequestDto.class);
        List<String> keywords = Collections.singletonList("돈까스");

        given(keywordService.addKeyword(any(), anyString())).willReturn(new KeywordAddResponseDto(keywords, false));
        assertThat(keywordController.addKeyword(request, EMAIL).getMessage()).isEqualTo("키워드가 등록되었습니다.");

        given(keywordService.addKeyword(any(), anyString())).willReturn(new KeywordAddResponseDto(keywords, true));
        assertThat(keywordController.addKeyword(request, EMAIL).getMessage()).isEqualTo("키워드는 최대 10개까지 등록할 수 있습니다.");

        given(keywordService.deleteKeyword(any(), anyString())).willReturn(mock(KeywordResponseDto.class));
        assertThat(keywordController.deleteKeyword(request, EMAIL).getMessage()).isEqualTo("키워드가 삭제되었습니다.");
    }
}
