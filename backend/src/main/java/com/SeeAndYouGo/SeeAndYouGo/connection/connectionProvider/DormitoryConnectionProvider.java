package com.SeeAndYouGo.SeeAndYouGo.connection.connectionProvider;

import com.SeeAndYouGo.SeeAndYouGo.connection.dto.ConnectionVO;
import com.SeeAndYouGo.SeeAndYouGo.global.HttpRequestUtil;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.SeeAndYouGo.SeeAndYouGo.global.DateTimeFormatters.DATETIME;

@Component
@RequiredArgsConstructor
@Slf4j
public class DormitoryConnectionProvider implements ConnectionProvider{

    @Value("${external-api.dormitory.connection-url}")
    private String dormitoryConnectionUrl;

    private Map<Restaurant, ConnectionVO> connectionMap = new HashMap<>();

    @Override
    public ConnectionVO getRecentConnection(Restaurant restaurant) {
        log.info("res {} getRecentConnection result: {}", restaurant, connectionMap.get(restaurant).getConnected());
        return connectionMap.get(restaurant);
    }

    @Override
    public ConnectionVO getRecentConnectionMap(Restaurant restaurant) throws Exception {
        return getRecentConnection(restaurant);
    }

    @Override
    public void updateConnectionMap(Restaurant restaurant) throws Exception {
        try {
            String responseData = HttpRequestUtil.get(dormitoryConnectionUrl);
            if (responseData.isEmpty()) {
                return;
            }

            // 응답 처리: "|" 앞부분 추출
            String[] parts = responseData.split("\\|");
            int connected = 0;
            if (parts.length > 0) {
                connected = Integer.parseInt(parts[0]);
            }

            // 현재 시간 가져오기
            String formattedDateTime = LocalDateTime.now().format(DATETIME);
            ConnectionVO connectionVO = new ConnectionVO(connected, formattedDateTime, restaurant);
            connectionMap.put(restaurant, connectionVO);
        } catch (Exception e) {
            log.error("Failed to update connection for restaurant: {}", restaurant, e);
        }
    }
}
