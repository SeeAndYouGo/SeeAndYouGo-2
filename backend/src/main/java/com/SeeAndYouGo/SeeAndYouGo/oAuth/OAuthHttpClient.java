package com.SeeAndYouGo.SeeAndYouGo.oAuth;

import com.SeeAndYouGo.SeeAndYouGo.global.exception.ApiException;
import com.SeeAndYouGo.SeeAndYouGo.global.exception.ErrorCode;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class OAuthHttpClient {

    public static String postForAccessToken(String urlString, String params) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()))) {
                bw.write(params);
                bw.flush();
            }

            return readResponse(connection);
        } catch (IOException e) {
            throw new RuntimeException("Failed to get access token", e);
        }
    }

    public static String getWithBearer(String urlString, String accessToken) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            return readResponse(connection);
        } catch (IOException e) {
            throw new RuntimeException("Failed to get user info", e);
        }
    }

    private static String readResponse(HttpURLConnection connection) throws IOException {
        int status = connection.getResponseCode();
        if (status >= 400) {
            throw toProviderError(status, readBody(connection.getErrorStream()));
        }
        return readBody(connection.getInputStream());
    }

    // 인가 코드 만료·재사용(invalid_grant)은 다시 로그인하면 해결되므로 400으로 안내하고,
    // 그 외(앱 키·redirect URI 설정 오류 등)는 다시 시도해도 실패하는 서버 문제이므로 응답 내용을 남겨 500으로 둔다.
    private static RuntimeException toProviderError(int status, String body) {
        if (isInvalidGrant(body)) {
            return new ApiException(ErrorCode.INVALID_INPUT_VALUE, "로그인에 실패했습니다. 다시 시도해주세요.");
        }
        return new IllegalStateException("OAuth provider responded " + status + ": " + body);
    }

    private static boolean isInvalidGrant(String body) {
        try {
            JsonElement error = parseJson(body).getAsJsonObject().get("error");
            return error != null && "invalid_grant".equals(error.getAsString());
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static String readBody(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        }
    }

    public static JsonElement parseJson(String json) {
        return JsonParser.parseString(json);
    }
}
