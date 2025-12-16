package com.SeeAndYouGo.SeeAndYouGo.global;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * HTTP 요청을 위한 유틸리티 클래스.
 * HttpURLConnection을 사용한 GET/POST 요청의 공통 로직을 제공한다.
 */
public class HttpRequestUtil {

    private static final int DEFAULT_TIMEOUT = 10000; // 10초

    private HttpRequestUtil() {
        // 유틸리티 클래스이므로 인스턴스화 방지
    }

    /**
     * GET 요청을 수행하고 응답 문자열을 반환한다.
     *
     * @param urlString 요청할 URL
     * @return 응답 문자열, 실패 시 빈 문자열
     * @throws IOException 네트워크 오류 발생 시
     */
    public static String get(String urlString) throws IOException {
        return executeRequest(urlString, "GET");
    }

    /**
     * POST 요청을 수행하고 응답 문자열을 반환한다.
     *
     * @param urlString 요청할 URL
     * @return 응답 문자열, 실패 시 빈 문자열
     * @throws IOException 네트워크 오류 발생 시
     */
    public static String post(String urlString) throws IOException {
        return executeRequest(urlString, "POST");
    }

    /**
     * HTTP 요청을 수행하는 공통 메서드.
     */
    private static String executeRequest(String urlString, String method) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod(method);
            connection.setConnectTimeout(DEFAULT_TIMEOUT);
            connection.setReadTimeout(DEFAULT_TIMEOUT);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return readResponse(connection.getInputStream());
            }
            return "";
        } finally {
            connection.disconnect();
        }
    }

    /**
     * InputStream에서 응답을 읽어 문자열로 반환한다.
     */
    private static String readResponse(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
}
