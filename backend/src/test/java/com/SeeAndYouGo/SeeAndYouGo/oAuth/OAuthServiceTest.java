package com.SeeAndYouGo.SeeAndYouGo.oAuth;

import com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt.TokenProvider;
import com.SeeAndYouGo.SeeAndYouGo.user.Social;
import com.SeeAndYouGo.SeeAndYouGo.user.User;
import com.SeeAndYouGo.SeeAndYouGo.user.UserReader;
import com.SeeAndYouGo.SeeAndYouGo.user.UserRepository;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 테스트 목표: 소셜 로그인(카카오/구글) 전체 흐름 검증.
 *
 *  [HTTP 호출 경로]
 *  - getKakaoAccessToken / getGoogleAccessToken: 발급된 응답에서 access_token 추출
 *  - getUserKakaoInfo / getUserGoogleInfo: 사용자 식별 정보 추출 (id/email)
 *
 *  [processLogin 분기]
 *  - 신규 유저: signUp 호출 + tokenDto.message = "join"
 *  - 기존 유저: signUp 미호출 + tokenDto.message = "login"
 *
 *  [reIssue 분기]
 *  - 리프레시 토큰 만료: 401
 *  - DB 의 refresh 와 입력값 불일치: 401
 *  - 정상: tokenProvider.reIssueToken 위임 결과 반환
 *
 *  OAuthHttpClient 의 static 메서드는 mockito-inline 의 mockStatic 으로 격리한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("소셜 로그인 - OAuthService")
class OAuthServiceTest {

    @Mock private UserReader userReader;
    @Mock private UserRepository userRepository;
    @Mock private TokenProvider tokenProvider;

    @InjectMocks private OAuthService oAuthService;

    private MockedStatic<OAuthHttpClient> httpClientMock;

    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USER_URL = "https://kapi.kakao.com/v2/user/me";
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_USER_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(oAuthService, "kakaoClientId", "kakao-client-id");
        ReflectionTestUtils.setField(oAuthService, "kakaoRedirectUri", "https://example.com/kakao");
        ReflectionTestUtils.setField(oAuthService, "googleClientId", "google-client-id");
        ReflectionTestUtils.setField(oAuthService, "googleClientSecret", "google-client-secret");
        ReflectionTestUtils.setField(oAuthService, "googleRedirectUri", "https://example.com/google");

        httpClientMock = Mockito.mockStatic(OAuthHttpClient.class);
        // parseJson 은 실제 구현을 사용 (단순 위임 메서드)
        httpClientMock.when(() -> OAuthHttpClient.parseJson(anyString()))
                .thenAnswer(invocation -> JsonParser.parseString(invocation.getArgument(0)));
    }

    @AfterEach
    void tearDown() {
        httpClientMock.close();
        SecurityContextHolder.clearContext();
    }

    // ===== KAKAO HTTP =====

    @Test
    @DisplayName("getKakaoAccessToken: 인가코드를 보내고 응답에서 access_token 을 파싱한다")
    void getKakaoAccessToken_parsesResponse() {
        // given
        String body = "{\"access_token\":\"kakao-access-xyz\",\"token_type\":\"bearer\"}";
        ArgumentCaptor<String> paramsCaptor = ArgumentCaptor.forClass(String.class);
        httpClientMock.when(() -> OAuthHttpClient.postForAccessToken(eq(KAKAO_TOKEN_URL), anyString()))
                .thenReturn(body);

        // when
        String accessToken = oAuthService.getKakaoAccessToken("auth-code-123");

        // then
        assertThat(accessToken).isEqualTo("kakao-access-xyz");

        httpClientMock.verify(() -> OAuthHttpClient.postForAccessToken(eq(KAKAO_TOKEN_URL), paramsCaptor.capture()));
        String params = paramsCaptor.getValue();
        assertThat(params).contains("grant_type=authorization_code");
        assertThat(params).contains("client_id=kakao-client-id");
        assertThat(params).contains("redirect_uri=https://example.com/kakao");
        assertThat(params).contains("code=auth-code-123");
    }

    @Test
    @DisplayName("getUserKakaoInfo: Bearer 토큰으로 유저정보를 받아 id/email 을 추출한다")
    void getUserKakaoInfo_extractsIdAndEmail() {
        // given
        String body = "{\"id\":12345,\"kakao_account\":{\"email\":\"user@kakao.com\"}}";
        httpClientMock.when(() -> OAuthHttpClient.getWithBearer(KAKAO_USER_URL, "kakao-access-xyz"))
                .thenReturn(body);

        // when
        var dto = oAuthService.getUserKakaoInfo("kakao-access-xyz");

        // then
        assertThat(dto.getId()).isEqualTo("12345");
        assertThat(dto.getEmail()).isEqualTo("user@kakao.com");
    }

    // ===== GOOGLE HTTP =====

    @Test
    @DisplayName("getGoogleAccessToken: 인가코드를 보내고 응답에서 access_token 을 파싱한다 (redirect_uri 가 URL-encoded)")
    void getGoogleAccessToken_parsesResponse() {
        // given
        String body = "{\"access_token\":\"google-access-abc\",\"expires_in\":3599}";
        ArgumentCaptor<String> paramsCaptor = ArgumentCaptor.forClass(String.class);
        httpClientMock.when(() -> OAuthHttpClient.postForAccessToken(eq(GOOGLE_TOKEN_URL), anyString()))
                .thenReturn(body);

        // when
        String token = oAuthService.getGoogleAccessToken("g-code-abc");

        // then
        assertThat(token).isEqualTo("google-access-abc");
        httpClientMock.verify(() -> OAuthHttpClient.postForAccessToken(eq(GOOGLE_TOKEN_URL), paramsCaptor.capture()));
        String params = paramsCaptor.getValue();
        assertThat(params).contains("client_id=google-client-id");
        assertThat(params).contains("client_secret=google-client-secret");
        assertThat(params).contains("code=g-code-abc");
        // URL 인코딩 검증
        assertThat(params).contains("redirect_uri=https%3A%2F%2Fexample.com%2Fgoogle");
    }

    @Test
    @DisplayName("getUserGoogleInfo: Bearer 토큰으로 유저정보를 받아 id/email 을 추출한다")
    void getUserGoogleInfo_extractsIdAndEmail() {
        // given
        String body = "{\"id\":\"g-id-001\",\"email\":\"user@google.com\",\"verified_email\":true}";
        httpClientMock.when(() -> OAuthHttpClient.getWithBearer(GOOGLE_USER_URL, "google-access-abc"))
                .thenReturn(body);

        // when
        var dto = oAuthService.getUserGoogleInfo("google-access-abc");

        // then
        assertThat(dto.getId()).isEqualTo("g-id-001");
        assertThat(dto.getEmail()).isEqualTo("user@google.com");
    }

    // ===== KAKAO LOGIN 분기 =====

    @Test
    @DisplayName("kakaoLogin (신규 유저): User 를 저장하고 message=\"join\" 인 TokenDto 를 반환한다")
    void kakaoLogin_newUser_signsUpAndReturnsJoin() {
        // given - kakao 사용자 정보 응답
        String body = "{\"id\":777,\"kakao_account\":{\"email\":\"new@kakao.com\"}}";
        httpClientMock.when(() -> OAuthHttpClient.getWithBearer(KAKAO_USER_URL, "k-access"))
                .thenReturn(body);
        // 신규 유저
        given(userReader.existsByEmail("new@kakao.com")).willReturn(false);
        TokenDto issued = TokenDto.builder().token("acc").refreshToken("ref").build();
        given(tokenProvider.createToken("new@kakao.com")).willReturn(issued);

        // when
        TokenDto result = oAuthService.kakaoLogin("k-access");

        // then
        assertThat(result).isSameAs(issued);
        assertThat(result.getMessage()).isEqualTo("join");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getEmail()).isEqualTo("new@kakao.com");
        assertThat(saved.getSocialType()).isEqualTo(Social.KAKAO);
        assertThat(saved.getNickname()).isNull();
    }

    @Test
    @DisplayName("kakaoLogin (기존 유저): signUp 을 호출하지 않고 message=\"login\" 인 TokenDto 를 반환한다")
    void kakaoLogin_existingUser_returnsLogin() {
        // given
        String body = "{\"id\":777,\"kakao_account\":{\"email\":\"old@kakao.com\"}}";
        httpClientMock.when(() -> OAuthHttpClient.getWithBearer(KAKAO_USER_URL, "k-access"))
                .thenReturn(body);
        given(userReader.existsByEmail("old@kakao.com")).willReturn(true);
        TokenDto issued = TokenDto.builder().token("acc").refreshToken("ref").build();
        given(tokenProvider.createToken("old@kakao.com")).willReturn(issued);

        // when
        TokenDto result = oAuthService.kakaoLogin("k-access");

        // then
        assertThat(result.getMessage()).isEqualTo("login");
        verify(userRepository, never()).save(any());
    }

    // ===== GOOGLE LOGIN 분기 =====

    @Test
    @DisplayName("googleLogin (신규 유저): User 를 GOOGLE 소셜로 저장하고 message=\"join\" 을 반환한다")
    void googleLogin_newUser_signsUpAndReturnsJoin() {
        // given
        String body = "{\"id\":\"g-001\",\"email\":\"new@google.com\"}";
        httpClientMock.when(() -> OAuthHttpClient.getWithBearer(GOOGLE_USER_URL, "g-access"))
                .thenReturn(body);
        given(userReader.existsByEmail("new@google.com")).willReturn(false);
        TokenDto issued = TokenDto.builder().token("acc").refreshToken("ref").build();
        given(tokenProvider.createToken("new@google.com")).willReturn(issued);

        // when
        TokenDto result = oAuthService.googleLogin("g-access");

        // then
        assertThat(result.getMessage()).isEqualTo("join");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("new@google.com");
        assertThat(userCaptor.getValue().getSocialType()).isEqualTo(Social.GOOGLE);
    }

    @Test
    @DisplayName("googleLogin (기존 유저): signUp 을 호출하지 않고 message=\"login\" 을 반환한다")
    void googleLogin_existingUser_returnsLogin() {
        String body = "{\"id\":\"g-001\",\"email\":\"old@google.com\"}";
        httpClientMock.when(() -> OAuthHttpClient.getWithBearer(GOOGLE_USER_URL, "g-access"))
                .thenReturn(body);
        given(userReader.existsByEmail("old@google.com")).willReturn(true);
        TokenDto issued = TokenDto.builder().token("acc").refreshToken("ref").build();
        given(tokenProvider.createToken("old@google.com")).willReturn(issued);

        TokenDto result = oAuthService.googleLogin("g-access");

        assertThat(result.getMessage()).isEqualTo("login");
        verify(userRepository, never()).save(any());
    }

    // ===== reIssue 분기 =====

    @Test
    @DisplayName("reIssue (리프레시 만료): 401 ResponseStatusException")
    void reIssue_expiredRefresh() {
        // given
        setAuthentication("user@kakao.com");
        given(tokenProvider.isRefreshTokenExpired("expired-refresh")).willReturn(true);

        // expect
        assertThatThrownBy(() -> oAuthService.reIssue("expired-refresh"))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.UNAUTHORIZED);

        // userReader/tokenProvider.reIssueToken 은 호출되지 않음
        verify(userReader, never()).getByEmail(anyString());
        verify(tokenProvider, never()).reIssueToken(any(), anyString());
    }

    @Test
    @DisplayName("reIssue (DB 의 refresh 와 입력값 불일치): 401 ResponseStatusException")
    void reIssue_refreshMismatch() {
        // given
        setAuthentication("user@kakao.com");
        given(tokenProvider.isRefreshTokenExpired("client-refresh")).willReturn(false);

        User user = User.builder().email("user@kakao.com").socialType(Social.KAKAO).build();
        user.updateRefreshToken("different-refresh-in-db");
        given(userReader.getByEmail("user@kakao.com")).willReturn(user);

        // expect
        assertThatThrownBy(() -> oAuthService.reIssue("client-refresh"))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.UNAUTHORIZED);

        verify(tokenProvider, never()).reIssueToken(any(), anyString());
    }

    @Test
    @DisplayName("reIssue (정상): tokenProvider.reIssueToken 위임 결과를 그대로 반환한다")
    void reIssue_success() {
        // given
        UsernamePasswordAuthenticationToken auth = setAuthentication("user@kakao.com");
        given(tokenProvider.isRefreshTokenExpired("valid-refresh")).willReturn(false);

        User user = User.builder().email("user@kakao.com").socialType(Social.KAKAO).build();
        user.updateRefreshToken("valid-refresh");
        given(userReader.getByEmail("user@kakao.com")).willReturn(user);

        TokenDto reissued = new TokenDto("new-acc", "new-ref", null, "reissue");
        given(tokenProvider.reIssueToken(auth, "valid-refresh")).willReturn(reissued);

        // when
        TokenDto result = oAuthService.reIssue("valid-refresh");

        // then
        assertThat(result).isSameAs(reissued);
    }

    // ===== helpers =====

    private UsernamePasswordAuthenticationToken setAuthentication(String email) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                email, null, Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        return auth;
    }
}
