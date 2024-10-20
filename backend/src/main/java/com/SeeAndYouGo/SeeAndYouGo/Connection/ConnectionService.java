package com.SeeAndYouGo.SeeAndYouGo.Connection;

import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ConnectionService {

    private final ConnectionRepository connectionRepository;

    @Value("${CONN_KEY}")
    private String CONN_KEY;

    @Value("${URL.CONN_URL}")
    private String CONN_URL;

    public Connection getRecentConnected(String restaurantName){
        String parseRestaurantName = Restaurant.parseName(restaurantName);
        Restaurant restaurant = Restaurant.valueOf(parseRestaurantName);
        Connection result = connectionRepository.findTopByRestaurantOrderByTimeDesc(restaurant);

        return result;
    }

    @Transactional
    public void saveAndCacheConnection() throws Exception{
        String wifiInfo = fetchConnectionInfoToString();
        if (wifiInfo.length() == 0) {
            return;
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
        if(connectionRepository.count() > 0){
            String recentTime = connectionRepository.findTopByOrderByTimeDesc().getTime();
            if(recentTime.equals(time)) return;
        }

        String today = time.split(" ")[0];

        for (JsonElement jsonElement : finalResult) {
            JsonObject asJsonObject = jsonElement.getAsJsonObject();
            String rawName = asJsonObject.get("name").toString();
            String restaurantName = Restaurant.parseName(removeQuotes(rawName));
            // 오늘 날짜의 학생식당에 해당하는 DB 값이 있는지 확인하고 있다면 가져오고, 없다면 생성하자.
            Restaurant restaurant = Restaurant.valueOf(restaurantName);

            // 만약 여기에 데이터가 없다면, restaurant를 새로 생성. 있다면, restaurant의 connection에 add하자.
            Integer connected = asJsonObject.get("connected").getAsInt();

            Connection connection = Connection.builder()
                                    .connected(connected)
                                    .time(time)
                                    .restaurant(restaurant)
                                    .build();

            connectionRepository.save(connection);
        }
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

    /**
     * 20240311131508 형식을 2024-03-11 13:15:08로 바꾸기
     */
    private static String resolveTimeFormat(String rawTime){
        return rawTime.substring(0, 4)+"-"+rawTime.substring(4, 6)+"-"+rawTime.substring(6, 8)+
                " "+rawTime.substring(8, 10)+":"+rawTime.substring(10, 12)+":"+rawTime.substring(12);
    }

    public String fetchConnectionInfoToString() throws Exception {

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

    public boolean checkSecretKey(String authKey) {
        return CONN_KEY.equals(authKey);
    }
}
