package com.SeeAndYouGo.SeeAndYouGo.oAuth;

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
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
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
