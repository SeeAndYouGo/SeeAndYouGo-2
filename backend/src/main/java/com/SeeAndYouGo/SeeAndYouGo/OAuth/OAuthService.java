package com.SeeAndYouGo.SeeAndYouGo.OAuth;

import com.SeeAndYouGo.SeeAndYouGo.OAuth.jwt.TokenProvider;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.List;

@Service
public class OAuthService {

    @Value("${KAKAO_REST_API_KEY}")
    private String KAKAO_REST_API_KEY;

    @Value("${KAKAO_REDIRECT_URI}")
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

    public HashMap<String, Object> getUserKakaoInfo(String accessToken) {
        HashMap<String, Object> userInfo = new HashMap<>();
        try {
            // GET: 카카오 사용자 정보 가져오기
            URL userInfoURL = new URL("https://kapi.kakao.com/v2/user/me");
            HttpURLConnection connection = (HttpURLConnection) userInfoURL.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            // 응답코드 200 : 가져온 유저 정보 확인 & userInfo 리턴값(HashMap) 생성
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder result = new StringBuilder();
            while ((line = br.readLine()) != null) {
                result.append(line);
            }

            JsonObject jsonObject = JsonParser.parseString(result.toString()).getAsJsonObject();
            String id = jsonObject.get("id").getAsString();
            JsonObject properties = jsonObject.get("properties").getAsJsonObject();
            JsonObject kakaoAccount = jsonObject.get("kakao_account").getAsJsonObject();
            String nickname = properties.get("nickname").getAsString();
            JsonElement emailElement = kakaoAccount.getAsJsonObject().get("email");
            if(emailElement != null) {
                String email = emailElement.getAsString();
                userInfo.put("email", email);
            }
            userInfo.put("nickname", nickname);
            userInfo.put("id", id);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return userInfo;
    }

    public String kakaoLogin(String accessToken) {
        // (1) accessToken을 통해 카카오의 유저정보를 가져온다. 특히 kakao identifier를 통하여 기존 유저 정보 존재여부를 따진다.
        HashMap<String, Object> userInfo = getUserKakaoInfo(accessToken);  // id, nickname, email

        String kakaoId = userInfo.get("id").toString();
        List<User> users = userRepository.findBySocialId(kakaoId);
        UserDto userDto = new UserDto(kakaoId, userInfo.get("email").toString(), userInfo.get("nickname").toString());
        if (users == null) {
            signUp(userDto);
        }

        // (2) jwt 토큰을 생성한다 by Email!
        // TODO: 예제의 createToken 파라미터를 String(즉 user Email)로 임의로 수정헀음. 작동 확인 해봐야 할듯.
        return tokenProvider.createToken(userDto.getEmail());
    }

    private void signUp(UserDto dto) {
        try {
            userRepository.save(new User(dto.getEmail(), dto.getNickname(), dto.getSocialId(), Social.KAKAO));
        } catch (Exception e) {
            // 유저 가입 실패
            e.printStackTrace();
        }
    }
}