package com.SeeAndYouGo.SeeAndYouGo.oAuth;

import com.SeeAndYouGo.SeeAndYouGo.global.response.ApiResponse;
import com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt.JwtFilter;
import com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class OAuthController {
    private static final String LOGIN_MESSAGE = "로그인되었습니다.";
    private static final String JOIN_MESSAGE = "회원가입을 축하합니다! 닉네임을 설정해주세요.";

    @Autowired
    private final OAuthService oauthService;

    @PostMapping("/api/oauth/kakao")
    public ApiResponse<TokenDto> kakaoLoginOauth(@RequestParam String code) {                // (1) 인가 코드를 받는다.
        String accessToken = oauthService.getKakaoAccessToken(code);            // (2) 카카오로부터 access token 을 받는다.
        return loginResponse(oauthService.kakaoLogin(accessToken));             // (3) access token 을 통해 유저정보를 확인 후 그리고 우리 서비스 전용 jwt token 을 리턴!
    }

    @PostMapping("/api/oauth/google")
    public ApiResponse<TokenDto> googleLoginOauth(@RequestParam String code) {               // (1) 인가 코드를 받는다.
        String accessToken = oauthService.getGoogleAccessToken(code);           // (2) 구글로부터 access token 을 받는다.
        return loginResponse(oauthService.googleLogin(accessToken));            // (3) access token 을 통해 유저정보를 확인 후 그리고 우리 서비스 전용 jwt token 을 리턴!
    }

    @PostMapping("/api/oauth/token")
    public TokenDto reissue(@RequestHeader(JwtFilter.REFRESH_HEADER) String refreshToken) {
        return oauthService.reIssue(refreshToken);
    }

    private ApiResponse<TokenDto> loginResponse(TokenDto token) {
        return ApiResponse.success(token, TokenDto.JOIN.equals(token.getMessage()) ? JOIN_MESSAGE : LOGIN_MESSAGE);
    }
}