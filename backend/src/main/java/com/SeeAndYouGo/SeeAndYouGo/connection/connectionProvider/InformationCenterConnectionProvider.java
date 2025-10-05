package com.SeeAndYouGo.SeeAndYouGo.connection.connectionProvider;

import com.SeeAndYouGo.SeeAndYouGo.connection.Connection;
import com.SeeAndYouGo.SeeAndYouGo.connection.dto.ConnectionVO;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.HashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class InformationCenterConnectionProvider implements ConnectionProvider{

    @Value("${API.CONN_KEY}")
    private String AUTH_KEY;

    @Value("${CONN.GET.URL}")
    private String URL;

    @Value("${CONN.GET.END_POINT}")
    private String END_POINT;

    @Value("${CONN.SAVE.URL}")
    private String SAVE_URL;

    @Value("${CONN.SAVE.END_POINT}")
    private String SAVE_END_POINT;

    private Map<Restaurant, ConnectionVO> connectionMap = new HashMap<>();

    public String getRecentConnectionToString() throws Exception {

        String apiUrl = SAVE_URL+SAVE_END_POINT + "?AUTH_KEY=" + AUTH_KEY;

        // URL 생성
        URL url = new URL(apiUrl);
        // HttpURLConnection 설정
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");

        // 응답 코드 확인
        int responseCode = connection.getResponseCode();
        String json = new String();

        // 응답 내용 읽기
        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuilder response = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            json = response.toString();
        }

        return json;
    }

    @Override
    public ConnectionVO getRecentConnection(Restaurant restaurant) {
        // 여기서 prod는 자기자신의 controller에게
        // local은 서버의 controller에게 요청을 보내자.
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        URI uri = getUri(restaurant);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<ConnectionVO> response = restTemplate.exchange(
                uri,
                HttpMethod.POST,
                entity,
                ConnectionVO.class
        );
        log.info("res {} getRecentConnection result: {}", restaurant, response.getBody());

        return response.getBody();
    }

    @Override
    public ConnectionVO getRecentConnectionMap(Restaurant restaurant) throws Exception {
        return connectionMap.get(restaurant);
    }

    private URI getUri(Restaurant restaurant) {
        return UriComponentsBuilder.fromUriString(URL)
                .path(END_POINT)
                .queryParam("AUTH_KEY", AUTH_KEY)
                .queryParam("restaurant", restaurant.getNumber())
                .encode()
                .build()
                .toUri();
    }

    @Override
    public void updateConnectionMap(Restaurant restaurant){
        try {
            String wifiInfo = getRecentConnectionToString();

            if (wifiInfo.isEmpty()) {
                throw new RuntimeException("API 결과로 받아온 Connection이 없습니다.");
            }

            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(wifiInfo).getAsJsonObject();
            JsonObject jsonWithRestaurantInfo = CacheJsonWithRestaurantInfo(jsonObject);

            if(jsonWithRestaurantInfo.size() == 0) return;

            JsonArray finalResult = new JsonArray();
            for (String location : jsonWithRestaurantInfo.keySet()) {
                JsonObject locationInfo = new JsonObject();
                locationInfo.addProperty("name", location);
                locationInfo.addProperty("connected", jsonWithRestaurantInfo.get(location).getAsInt());
                finalResult.add(locationInfo);
            }

            String time = extractTimeInJson(jsonObject);

            // 현재 3, 5학생회관이 connection문제로 날라오지 않는다.
            // 0으로 만들어서 보내주자.
            //if(restaurant.equals(Restaurant.생활과학대) || restaurant.equals(Restaurant.제3학생회관)){
            //    ConnectionVO connectionVO = new ConnectionVO(0, time, restaurant);
            //    connectionMap.put(restaurant, connectionVO);
            //    return;
            //}

            for (JsonElement jsonElement : finalResult) {
                JsonObject asJsonObject = jsonElement.getAsJsonObject();
                String rawName = asJsonObject.get("name").toString();
                String restaurantName = Restaurant.parseName(removeQuotes(rawName));

                if (!restaurant.toString().equals(restaurantName)) {
                    // 내가 원하는 restaurant가 아니면 패쓰
                    continue;
                }

                Integer connected = asJsonObject.get("connected").getAsInt();

                ConnectionVO connectionVO = new ConnectionVO(connected, time, restaurant);

                connectionMap.put(restaurant, connectionVO);
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }

    private JsonObject CacheJsonWithRestaurantInfo(JsonObject jsonObject) {
        JsonArray resultArray = jsonObject.getAsJsonArray("RESULT");
        JsonObject locationData = new JsonObject();

        String time = "NULL";

        for (JsonElement element : resultArray) {
            JsonObject entry = element.getAsJsonObject();
            String location = entry.get("LOCATION").getAsString();


            location = Connection.parseRestaurantNameForCache(location);
            if(location.equals("NULL")) continue;

            int client = entry.get("CLIENT").getAsInt();
            if(time.equals("NULL")){
                String rawTime = entry.get("CRT_DT").getAsString();
                time = resolveTimeFormat(rawTime);
            }

            if (locationData.has(location)) {
                int currentClient = locationData.get(location).getAsInt();
                locationData.addProperty(location, currentClient + client);
            } else {
                locationData.addProperty(location, client);
            }
        }

        return locationData;
    }

    public static String removeQuotes(String input) {
        // 만약 입력 문자열에 큰따옴표가 없다면 원래 문자열 반환
        if (!input.contains("\"")) {
            return input;
        }
        // 큰따옴표를 제거한 문자열 생성
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c != '"') {
                result.append(c);
            }
        }
        return result.toString();
    }

    private String extractTimeInJson(JsonObject jsonObject) {
        JsonArray resultArray = jsonObject.getAsJsonArray("RESULT");
        JsonElement element = resultArray.get(0);
        JsonObject entry = element.getAsJsonObject();
        String rawTime = entry.get("CRT_DT").getAsString();
        String time = resolveTimeFormat(rawTime);

        return time; // 시간형식은 2023-11-23 22:02:01 이다.
    }

    /**
     * 20240311131508 형식을 2024-03-11 13:15:08로 바꾸기
     */
    private static String resolveTimeFormat(String rawTime){
        return rawTime.substring(0, 4)+"-"+rawTime.substring(4, 6)+"-"+rawTime.substring(6, 8)+
                " "+rawTime.substring(8, 10)+":"+rawTime.substring(10, 12)+":"+rawTime.substring(12);
    }
}
