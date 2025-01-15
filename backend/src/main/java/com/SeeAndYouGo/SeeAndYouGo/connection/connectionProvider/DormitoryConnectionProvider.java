package com.SeeAndYouGo.SeeAndYouGo.connection.connectionProvider;

import com.SeeAndYouGo.SeeAndYouGo.connection.Connection;
import com.SeeAndYouGo.SeeAndYouGo.connection.ConnectionRepository;
import com.SeeAndYouGo.SeeAndYouGo.connection.dto.ConnectionVO;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DormitoryConnectionProvider implements ConnectionProvider{

    @Value("${URL.DORM_CONN_URL}")
    private String CONN_URL;

    private Map<Restaurant, ConnectionVO> connectionMap;

    @Override
    public ConnectionVO getRecentConnection(Restaurant restaurant) {
        return connectionMap.get(restaurant);
    }

    @Override
    public void updateConnectionMap(Restaurant restaurant) throws Exception {
        try{
            // 요청할 URL
            String urlString = "https://dorm.cnu.ac.kr/intranet/public/ajax_cafe_inwon.php?mode=inwon";
            URL url = new URL(urlString);

            // HttpURLConnection 설정
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // 응답 코드 확인
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 응답 성공
                // 응답 데이터 읽기
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // 응답 처리: "|" 앞부분 추출
                String responseData = response.toString();
                String[] parts = responseData.split("\\|");

                int connected = 0;
                if (parts.length > 0) {
                    connected = Integer.parseInt(parts[0]);
                }

                // 현재 시간 가져오기
                LocalDateTime now = LocalDateTime.now();

                // 포맷터 정의
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                // LocalDateTime을 포맷팅
                String formattedDateTime = now.format(formatter);

                connection.disconnect();

                ConnectionVO connectionVO = new ConnectionVO(connected, formattedDateTime, restaurant);

                connectionMap.put(restaurant, connectionVO);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
