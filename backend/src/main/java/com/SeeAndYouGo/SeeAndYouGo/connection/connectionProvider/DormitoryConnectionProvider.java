package com.SeeAndYouGo.SeeAndYouGo.connection.connectionProvider;

import com.SeeAndYouGo.SeeAndYouGo.connection.dto.ConnectionVO;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DormitoryConnectionProvider implements ConnectionProvider{

    @Value("${URL.DORM_CONN_URL}")
    private String CONN_URL;

    private Map<Restaurant, ConnectionVO> connectionMap = new HashMap<>();

    @Override
    public ConnectionVO getRecentConnection(Restaurant restaurant) {
        ConnectionVO connection = connectionMap.get(restaurant);
        if (connection == null) {
            log.warn("res {} 접속자 정보 없음. 기본값 0으로 반환합니다.", restaurant);
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            return new ConnectionVO(0, now, restaurant);
        }
        log.info("res {} getRecentConnection result: {}", restaurant, connection.getConnected());
        return connection;
    }

    @Override
    public ConnectionVO getRecentConnectionMap(Restaurant restaurant) throws Exception {
        return getRecentConnection(restaurant);
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
            connection.setConnectTimeout(10000); // 연결 타임아웃 10초
            connection.setReadTimeout(10000);    // 읽기 타임아웃 10초

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
            } else {
                log.warn("기숙사 접속자 API 응답 실패 (HTTP {}): {}", responseCode, urlString);
            }
        } catch (Exception e) {
            log.warn("기숙사 접속자 정보 조회 실패: {}. 기존 캐시를 유지합니다.", e.getMessage());
        }
    }
}
