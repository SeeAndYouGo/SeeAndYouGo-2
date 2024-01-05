package com.SeeAndYouGo.SeeAndYouGo.OAuth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class OAuthController {

    @Autowired
    private final OAuthService oauthService;

    @GetMapping("/api/oauth/kakao")

    @ResponseBody
    public ResponseEntity<TokenDto> kakaoLoginOauth(@RequestParam String code) {  // (1) 인가 코드를 받는다.
        String accessToken = oauthService.getKakaoAccessToken(code);            // (2) 카카오로부터 access token 을 받는다.
        TokenDto token = new TokenDto(oauthService.kakaoLogin(accessToken));    // (3) access token 을 통해 유저정보를 확인 후
                                                                                //     로그인(회원가입) 진행한다.
                                                                                //     그리고 우리 서비스 전용 jwt token 을 리턴!
        return ResponseEntity.ok(token);
    }

    // 참고: kakao의 access token을 서비스에서 그대로 이용하다가 만약 해킹당해서 유저 정보 노출되면 고소당할 수 있다는 이야기가 있다.
}
