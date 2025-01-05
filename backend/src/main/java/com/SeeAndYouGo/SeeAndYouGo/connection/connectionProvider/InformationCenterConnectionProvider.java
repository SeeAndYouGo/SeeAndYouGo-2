package com.SeeAndYouGo.SeeAndYouGo.connection.connectionProvider;

import com.SeeAndYouGo.SeeAndYouGo.connection.Connection;
import com.SeeAndYouGo.SeeAndYouGo.connection.ConnectionRepository;
import com.SeeAndYouGo.SeeAndYouGo.connection.dto.ConnectionVO;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InformationCenterConnectionProvider implements ConnectionProvider{

    @Value("${CONN_KEY}")
    private String CONN_KEY;

    @Value("${URL.CONN_URL}")
    private String CONN_URL;

    public String getRecentConnectionToString(Restaurant restaurant) throws Exception {

        String apiUrl = CONN_URL + "?AUTH_KEY=" + CONN_KEY;

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
    public List<ConnectionVO> getRecentConnection(Restaurant restaurant) throws Exception{
        String wifiInfo = getRecentConnectionToString(restaurant);
        List<ConnectionVO> result = new ArrayList<>();
        
        if (wifiInfo.isEmpty()) {
            return result;
        }
        
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(wifiInfo).getAsJsonObject();
        JsonObject jsonWithRestaurantInfo = CacheJsonWithRestaurantInfo(jsonObject);

        if(jsonWithRestaurantInfo.size() == 0) return null;

        JsonArray finalResult = new JsonArray();
        for (String location : jsonWithRestaurantInfo.keySet()) {
            JsonObject locationInfo = new JsonObject();
            locationInfo.addProperty("name", location);
            locationInfo.addProperty("connected", jsonWithRestaurantInfo.get(location).getAsInt());
            finalResult.add(locationInfo);
        }

        String time = extractTimeInJson(jsonObject);

        for (JsonElement jsonElement : finalResult) {
            JsonObject asJsonObject = jsonElement.getAsJsonObject();
            String rawName = asJsonObject.get("name").toString();
            String restaurantName = Restaurant.parseName(removeQuotes(rawName));

            if(!restaurant.toString().equals(restaurantName)){
                // 내가 원하는 restaurant가 아니면 패쓰
                continue;
            }

            Integer connected = asJsonObject.get("connected").getAsInt();

            ConnectionVO connectionVO = new ConnectionVO(connected, time, restaurant);

            result.add(connectionVO);
        }

        return result;
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
