package com.SeeAndYouGo.SeeAndYouGo.user;

import com.SeeAndYouGo.SeeAndYouGo.keyword.KeywordController;
import com.SeeAndYouGo.SeeAndYouGo.keyword.KeywordService;
import com.SeeAndYouGo.SeeAndYouGo.keyword.dto.KeywordRequestDto;
import com.SeeAndYouGo.SeeAndYouGo.like.LikeController;
import com.SeeAndYouGo.SeeAndYouGo.like.LikeService;
import com.SeeAndYouGo.SeeAndYouGo.user.dto.UserNicknameRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * 로그인이 필요한 API는 비로그인 요청(JwtFilter가 email "none"으로 넘김)에 401을 응답하고,
 * 서비스 로직을 실행하지 않는다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("로그인 필수 API - 비로그인 401")
class LoginRequiredApiTest {

    private static final String GUEST = "none";

    @Mock private UserReader userReader;
    @Mock private LikeService likeService;
    @Mock private KeywordService keywordService;
    @Mock private UserService userService;

    private LikeController likeController;
    private KeywordController keywordController;
    private UserController userController;

    @BeforeEach
    void setUp() {
        AuthorizationService authorizationService = new AuthorizationService(userReader);
        likeController = new LikeController(likeService, authorizationService);
        keywordController = new KeywordController(keywordService, authorizationService);
        userController = new UserController(userService, authorizationService);
    }

    @Test
    @DisplayName("공감 토글")
    void like() {
        assertUnauthorized(() -> likeController.postLikeCount(1L, GUEST));
        verifyNoInteractions(likeService);
    }

    @Test
    @DisplayName("키워드 조회·등록·삭제")
    void keyword() {
        assertUnauthorized(() -> keywordController.getKeywordsByUser(GUEST));
        assertUnauthorized(() -> keywordController.addKeyword(mock(KeywordRequestDto.class), GUEST));
        assertUnauthorized(() -> keywordController.deleteKeyword(mock(KeywordRequestDto.class), GUEST));
        verifyNoInteractions(keywordService);
    }

    @Test
    @DisplayName("내 정보 조회·닉네임 변경")
    void user() {
        assertUnauthorized(() -> userController.getNickname(GUEST));
        assertUnauthorized(() -> userController.changeNickname(mock(UserNicknameRequest.class), GUEST));
        verifyNoInteractions(userService);
    }

    private void assertUnauthorized(Runnable request) {
        assertThatThrownBy(request::run)
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatus())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
