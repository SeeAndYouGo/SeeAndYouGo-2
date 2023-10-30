package com.SeeAndYouGo.SeeAndYouGo.Connection;

import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.RestaurantRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.SeeAndYouGo.SeeAndYouGo.Connection.Connection.createNewConnection;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ConnectionService {
    private final ConnectionRepository connectionRepository;
    private final RestaurantRepository restaurantRepository;

    public Connection getRecentConnected(String restaurantName){
        String changeRestaurantName = changeRestaurantName(restaurantName);
        Connection result = connectionRepository.findRecent(changeRestaurantName);

        return result;
    }

    @Transactional
    @Scheduled(fixedRate = 60000, initialDelay = 1000)
    public void saveAndCashConnection() throws Exception{

        String wifiInfo = fetchConnectionInfoToString();

        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(wifiInfo).getAsJsonObject();
        JsonObject jsonWithRestaurantInfo = CashJsonWithRestaurantInfo(jsonObject);

        if(jsonWithRestaurantInfo.size() == 0) return;

        JsonArray finalResult = new JsonArray();
        for (String location : jsonWithRestaurantInfo.keySet()) {
            JsonObject locationInfo = new JsonObject();
            locationInfo.addProperty("name", location);
            locationInfo.addProperty("connected", jsonWithRestaurantInfo.get(location).getAsInt());
            finalResult.add(locationInfo);
        }

        Long aLong = connectionRepository.countNumberOfData();

        String time = extractTimeInJson(jsonObject);
        if(aLong>0){
            String recentTime = connectionRepository.findRecentTime();
            if(recentTime.equals(time)) return;
        }

        String today = time.split(" ")[0];

        for (JsonElement jsonElement : finalResult) {
            JsonObject asJsonObject = jsonElement.getAsJsonObject();
            String rawName = asJsonObject.get("name").toString();
            String name = removeQuotes(rawName);
            // 오늘 날짜의 학생식당에 해당하는 DB 값이 있는지 확인하고 있다면 가져오고, 없다면 생성하자.
            Restaurant restaurant = getRestaurantIfExistElseCreate(name, today);

            // 만약 여기에 데이터가 없다면, restaurant를 새로 생성. 있다면, restaurant의 connection에 add하자.
            Integer connected = asJsonObject.get("connected").getAsInt();

            Connection connection = createNewConnection(connected, time, restaurant);
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

    private Restaurant getRestaurantIfExistElseCreate(String name, String today) {
        Long aLong = restaurantRepository.countNumberOfDataInDate(name, today);
        if(aLong > 0) {
            return restaurantRepository.findTodayRestaurant(name, today);
        }else{
            Restaurant restaurant = new Restaurant(name, today);
            restaurantRepository.save(restaurant);
            return restaurant;
        }
    }

    private String extractTimeInJson(JsonObject jsonObject) {
        JsonArray resultArray = jsonObject.getAsJsonArray("RESULT");
        JsonElement element = resultArray.get(0);
        JsonObject entry = element.getAsJsonObject();
        String rawTime = entry.get("CRT_DT").getAsString();
        String time = rawTime.substring(0, 4)+"-"+rawTime.substring(4, 6)+
                "-"+rawTime.substring(6, 8)+" "+rawTime.substring(8, 10)+
                ":"+rawTime.substring(10, 12)+":"+rawTime.substring(12);

        return time; // 시간형식은 2023-11-23 22:02:01 이다.
    }

    private JsonObject CashJsonWithRestaurantInfo(JsonObject jsonObject) {
        JsonArray resultArray = jsonObject.getAsJsonArray("RESULT");
        JsonObject locationData = new JsonObject();

        String time = "NULL";

        for (JsonElement element : resultArray) {
            JsonObject entry = element.getAsJsonObject();
            String location = entry.get("LOCATION").getAsString();


            location = changeRestaurantNameForCashing(location);
            if(location.equals("NULL")) continue;

            int client = entry.get("CLIENT").getAsInt();
            if(time.equals("NULL")){
                String rawTime = entry.get("CRT_DT").getAsString();
                time = rawTime.substring(0, 4)+"-"+rawTime.substring(4, 6)+"-"+rawTime.substring(6, 8)+" "+rawTime.substring(8, 10)+":"+rawTime.substring(10, 12)+":"+rawTime.substring(12);
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

    private String fetchConnectionInfoToString() throws Exception {
        String apiUrl = "https://api.cnu.ac.kr/svc/offcam/pub/WifiAllInfo?AUTH_KEY=D6E3BE404CC745B885E81D6BD5FE90CD6A59E572";

        // URL 생성
        URL url = new URL(apiUrl);
        // HttpURLConnection 설정
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // 응답 코드 확인
        int responseCode = connection.getResponseCode();
        System.out.println(responseCode);
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

    public String changeRestaurantNameForCashing(String name){
        if(name.contains("Je1")) return "1학생회관";
        else if(name.contains("제2학생회관")) return "2학생회관";
        else if(name.contains("Je3_Hak") || name.contains("3학생")) return "3학생회관";
        else if(name.contains("제4학생")) return "상록회관";
        else if(name.contains("생활과학대 1F")) return "생활과학대";
        else return "NULL";
    }


    public String changeRestaurantName(String name){
        if(name.contains("1")) return "1학생회관";
        else if(name.contains("2")) return "2학생회관";
        else if(name.contains("3")) return "3학생회관";
        else if(name.contains("4")) return "상록회관";
        else if(name.contains("5")) return "생활과학대";
        return "Null";
    }
}