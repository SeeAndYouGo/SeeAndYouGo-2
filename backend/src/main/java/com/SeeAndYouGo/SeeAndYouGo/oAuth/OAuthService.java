package com.SeeAndYouGo.SeeAndYouGo.oAuth;

import com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt.TokenProvider;
import com.SeeAndYouGo.SeeAndYouGo.user.Social;
import com.SeeAndYouGo.SeeAndYouGo.user.User;
import com.SeeAndYouGo.SeeAndYouGo.user.UserRepository;
import com.SeeAndYouGo.SeeAndYouGo.user.dto.UserIdentityDto;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class OAuthService {

    @Value("${kakao.REST_API_KEY}")
    private String KAKAO_REST_API_KEY;

    @Value("${kakao.REDIRECT_URI}")
    private String KAKAO_REDIRECT_URI;

    @Autowired
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;

    public String getKakaoAccessToken(String code) {
        String accessToken;
        try {
            URL tokenRequestURL = new URL("https://kauth.kakao.com/oauth/token");
            HttpURLConnection connection = (HttpURLConnection) tokenRequestURL.openConnection();

            // POST요청을 위한 세팅
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // POST요청이 요구하는 파라미터 세팅 & 스트림에 쓰기
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            StringBuilder sb = new StringBuilder();
            sb.append("grant_type=authorization_code")
                    .append("&client_id=").append(KAKAO_REST_API_KEY) // REST_API_KEY
                    .append("&redirect_uri=").append(KAKAO_REDIRECT_URI) // 인가코드 받았던 그 URI
                    .append("&code=").append(code); // 파라미터의 인가코드
            bw.write(sb.toString());
            bw.flush();

            // 응답코드 200: JSON Response를 받아온다.
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            StringBuilder result = new StringBuilder();

            while ((line = br.readLine()) != null) {
                result.append(line);
            }

            // JSON 파싱 후 access token 확인하기
            JsonElement jsonElement = JsonParser.parseString(result.toString());
            accessToken = jsonElement.getAsJsonObject().get("access_token").getAsString();

            // 끝!
            br.close();
            bw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return accessToken;
    }

    public UserIdentityDto getUserKakaoInfo(String accessToken) {
        try {
            // GET: 카카오 사용자 정보 가져오기
            URL userInfoURL = new URL("https://kapi.kakao.com/v2/user/me");
            HttpURLConnection connection = (HttpURLConnection) userInfoURL.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            // 응답코드 200 이후
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder result = new StringBuilder();
            while ((line = br.readLine()) != null) {
                result.append(line);
            }

            // 받아온 유저정보에서 id와 email을 가져온다.
            JsonObject jsonObject = JsonParser.parseString(result.toString()).getAsJsonObject();
            String id = jsonObject.get("id").getAsString();
            String email = jsonObject.get("kakao_account").getAsJsonObject().get("email").getAsString();
            System.out.println(email);

            return UserIdentityDto.builder()
                    .id(id)
                    .email(email)
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public TokenDto kakaoLogin(String accessToken) {
        // Get user info from kakao
        UserIdentityDto userIdentityDto = getUserKakaoInfo(accessToken);
        String message = "login";
        String email = userIdentityDto.getEmail();

        if (!userRepository.existsByEmail(email)) {
            signUp(userIdentityDto);
            message = "join";
        }

        // Create jwt token (access & refresh)
        TokenDto tokenDto = tokenProvider.createToken(email);
        tokenDto.setMessage(message);

        return tokenDto;
    }

    public TokenDto reIssue(String refreshToken) {
        // 1. 인증된 사용자 정보 얻기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. 리프레시 검증 (1, 2차 검증)
        if (tokenProvider.isRefreshTokenExpired(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Expired Refresh Token");
        }
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        if (!user.getRefreshToken().equals(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Refresh Token");
        }

        // 액세스 재발급
        return tokenProvider.reIssueToken(authentication, refreshToken);
    }

    private void signUp(UserIdentityDto dto) {
        try {
            userRepository.save(User.builder()
                            .email(dto.getEmail())
                            .nickname(null)
                            .socialType(Social.KAKAO)
                            .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}