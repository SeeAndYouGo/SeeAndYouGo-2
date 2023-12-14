package com.SeeAndYouGo.SeeAndYouGo.OAuth;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class OAuthService {

    @Value("${KAKAO_REST_API_KEY}")
    private String KAKAO_REST_API_KEY;

    @Value("${KAKAO_REDIRECT_URI}")
    private String KAKAO_REDIRECT_URI;

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

            // JSON의 access token 확인하기
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
}
