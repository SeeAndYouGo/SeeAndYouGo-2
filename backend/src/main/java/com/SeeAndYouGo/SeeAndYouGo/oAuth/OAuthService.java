package com.SeeAndYouGo.SeeAndYouGo.oAuth;

import com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt.TokenProvider;
import com.SeeAndYouGo.SeeAndYouGo.user.Social;
import com.SeeAndYouGo.SeeAndYouGo.user.User;
import com.SeeAndYouGo.SeeAndYouGo.user.UserRepository;
import com.SeeAndYouGo.SeeAndYouGo.user.dto.UserIdentityDto;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {

    @Value("${kakao.REST_API_KEY}")
    private String kakaoClientId;

    @Value("${kakao.REDIRECT_URI}")
    private String kakaoRedirectUri;

    @Value("${google.CLIENT_ID}")
    private String googleClientId;

    @Value("${google.CLIENT_SECRET}")
    private String googleClientSecret;

    @Value("${google.REDIRECT_URI}")
    private String googleRedirectUri;

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;

    // ===== KAKAO =====
    public String getKakaoAccessToken(String code) {
        String params = String.format(
                "grant_type=authorization_code&client_id=%s&redirect_uri=%s&code=%s",
                kakaoClientId, kakaoRedirectUri, code
        );
        String response = OAuthHttpClient.postForAccessToken("https://kauth.kakao.com/oauth/token", params);
        return OAuthHttpClient.parseJson(response).getAsJsonObject().get("access_token").getAsString();
    }

    public UserIdentityDto getUserKakaoInfo(String accessToken) {
        String response = OAuthHttpClient.getWithBearer("https://kapi.kakao.com/v2/user/me", accessToken);
        JsonObject json = OAuthHttpClient.parseJson(response).getAsJsonObject();

        String id = json.get("id").getAsString();
        String email = json.get("kakao_account").getAsJsonObject().get("email").getAsString();
        log.info("Kakao user email: {}", email);

        return UserIdentityDto.builder().id(id).email(email).build();
    }

    public TokenDto kakaoLogin(String accessToken) {
        return processLogin(getUserKakaoInfo(accessToken), Social.KAKAO);
    }

    // ===== GOOGLE =====
    public String getGoogleAccessToken(String code) {
        String params = String.format(
                "grant_type=authorization_code&client_id=%s&client_secret=%s&redirect_uri=%s&code=%s",
                googleClientId, googleClientSecret,
                URLEncoder.encode(googleRedirectUri, StandardCharsets.UTF_8), code
        );
        String response = OAuthHttpClient.postForAccessToken("https://oauth2.googleapis.com/token", params);
        return OAuthHttpClient.parseJson(response).getAsJsonObject().get("access_token").getAsString();
    }

    public UserIdentityDto getUserGoogleInfo(String accessToken) {
        String response = OAuthHttpClient.getWithBearer("https://www.googleapis.com/oauth2/v2/userinfo", accessToken);
        JsonObject json = OAuthHttpClient.parseJson(response).getAsJsonObject();

        String id = json.get("id").getAsString();
        String email = json.get("email").getAsString();
        log.info("Google user email: {}", email);

        return UserIdentityDto.builder().id(id).email(email).build();
    }

    public TokenDto googleLogin(String accessToken) {
        return processLogin(getUserGoogleInfo(accessToken), Social.GOOGLE);
    }

    // ===== COMMON =====
    private TokenDto processLogin(UserIdentityDto userInfo, Social socialType) {
        String email = userInfo.getEmail();
        String message = "login";

        if (!userRepository.existsByEmail(email)) {
            signUp(userInfo, socialType);
            message = "join";
        }

        TokenDto tokenDto = tokenProvider.createToken(email);
        tokenDto.setMessage(message);
        return tokenDto;
    }

    public TokenDto reIssue(String refreshToken) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (tokenProvider.isRefreshTokenExpired(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Expired Refresh Token");
        }

        User user = userRepository.findByEmail(authentication.getName());
        if (!user.getRefreshToken().equals(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Refresh Token");
        }

        return tokenProvider.reIssueToken(authentication, refreshToken);
    }

    private void signUp(UserIdentityDto dto, Social social) {
        userRepository.save(User.builder()
                .email(dto.getEmail())
                .nickname(null)
                .socialType(social)
                .build());
        log.info("New user signed up: {}", dto.getEmail());
    }
}