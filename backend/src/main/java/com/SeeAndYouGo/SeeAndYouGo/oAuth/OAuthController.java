package com.SeeAndYouGo.SeeAndYouGo.oAuth;

import com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt.JwtFilter;
import com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class OAuthController {

    @Autowired
    private final OAuthService oauthService;

    @GetMapping("/api/oauth/kakao")
    public TokenDto kakaoLoginOauth(@RequestParam String code) {                // (1) 인가 코드를 받는다.
        String accessToken = oauthService.getKakaoAccessToken(code);            // (2) 카카오로부터 access token 을 받는다.
        return oauthService.kakaoLogin(accessToken);                            // (3) access token 을 통해 유저정보를 확인 후 그리고 우리 서비스 전용 jwt token 을 리턴!
    }

    @GetMapping("/api/oauth/token/reissue")
    public TokenDto reissue(@RequestHeader(JwtFilter.REFRESH_HEADER) String refreshToken) {
        return oauthService.reIssue(refreshToken);
    }
}