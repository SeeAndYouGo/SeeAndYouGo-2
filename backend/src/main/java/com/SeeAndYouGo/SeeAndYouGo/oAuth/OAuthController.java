package com.SeeAndYouGo.SeeAndYouGo.oAuth;

import com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt.JwtFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class OAuthController {

    private final OAuthService oauthService;

    @GetMapping("/api/oauth/kakao")
    public TokenDto kakaoLoginOauth(@RequestParam String code) {
        log.info("Received Kakao login request with authorization code.");
        // (1) 인가 코드를 받는다.
        String accessToken = oauthService.getKakaoAccessToken(code);
        // (2) 카카오로부터 access token 을 받는다.
        TokenDto tokenDto = oauthService.kakaoLogin(accessToken);
        // (3) access token 을 통해 유저정보를 확인 후 그리고 우리 서비스 전용 jwt token 을 리턴!
        log.info("Kakao login successful. Issuing service tokens.");
        return tokenDto;
    }

    @GetMapping("/api/oauth/token/reissue")
    public TokenDto reissue(@RequestHeader(JwtFilter.REFRESH_HEADER) String refreshToken) {
        log.info("Received request to reissue token.");
        TokenDto tokenDto = oauthService.reIssue(refreshToken);
        log.info("Token reissue successful.");
        return tokenDto;
    }
}
