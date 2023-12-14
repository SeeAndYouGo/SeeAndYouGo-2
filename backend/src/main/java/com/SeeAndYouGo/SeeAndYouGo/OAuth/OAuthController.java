package com.SeeAndYouGo.SeeAndYouGo.OAuth;

import org.springframework.beans.factory.annotation.Autowired;
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
    public void kakaoLoginOauth(@RequestParam String code) {
        String accessToken = oauthService.getKakaoAccessToken(code);
    }
}
