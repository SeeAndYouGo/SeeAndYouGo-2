package com.SeeAndYouGo.SeeAndYouGo.OAuth;

import com.SeeAndYouGo.SeeAndYouGo.user.Social;
import com.SeeAndYouGo.SeeAndYouGo.user.User;
import com.SeeAndYouGo.SeeAndYouGo.user.dto.UserIdentityDto;
import com.SeeAndYouGo.SeeAndYouGo.OAuth.jwt.TokenProvider;
import com.SeeAndYouGo.SeeAndYouGo.user.UserRepository;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import java.io.*;
import java.net.*;
import java.util.List;

@Service
@ConfigurationProperties(value = "kakao")
@PropertySource("classpath:key.yml")
public class OAuthService {

    @Value("${REST_API_KEY}")
    private String KAKAO_REST_API_KEY;

    @Value("${REDIRECT_URI}")
    private String KAKAO_REDIRECT_URI;

    @Autowired
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;


    public OAuthService(UserRepository userRepository, TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
    }

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
            System.out.println("[kakao login token] response body: " + result);

            // JSON 파싱 후 access token 확인하기
            JsonElement jsonElement = JsonParser.parseString(result.toString());
            accessToken = jsonElement.getAsJsonObject().get("access_token").getAsString();
            System.out.println("[kakao login token] access_token: " + accessToken);

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
        // (1) accessToken을 통해 카카오의 유저정보를 가져온다.
        UserIdentityDto userIdentityDto = getUserKakaoInfo(accessToken);
        String message = "login";
        String email = userIdentityDto.getEmail();

        if (!userRepository.existsByEmail(email)) {
            signUp(userIdentityDto);
            message = "join";
        }

        // (2) jwt 토큰을 생성한다 by Email!
        String token = tokenProvider.createToken(userIdentityDto.getEmail());

        return TokenDto.builder()
                .token(token)
                .message(message)
                .build();
    }

    private void signUp(UserIdentityDto dto) {
        try {
            userRepository.save(User.builder()
                            .email(dto.getEmail())
                            .nickname(null)
                            .socialType(Social.KAKAO)
                            .build());
        } catch (Exception e) {
            // 유저 가입 실패
            e.printStackTrace();
        }
    }
}