package com.SeeAndYouGo.SeeAndYouGo.OAuth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OAuthController {

    @Autowired
    private final OAuthService oauthService;

    public OAuthController(OAuthService oauthService) {
        this.oauthService = oauthService;
    }

    @GetMapping("/api/oauth/kakao")
    @ResponseBody
    public ResponseEntity<String> kakaoLoginOauth(@RequestParam String code) {  // (1) 인가 코드를 받는다.
        String accessToken = oauthService.getKakaoAccessToken(code);            // (2) 카카오로부터 access token 을 받는다.
        String serviceToken = oauthService.kakaoLogin(accessToken);             // (3) access token 을 통해 유저정보를 확인 후
                                                                                //     로그인(회원가입) 진행한다.
                                                                                //     그리고 우리 서비스 전용 jwt token 을 리턴!
        return ResponseEntity.ok(serviceToken);
    }

    // 참고: kakao의 access token을 서비스에서 그대로 이용하다가 만약 해킹당해서 유저 정보 노출되면 고소당할 수 있다는 이야기가 있다.
}
