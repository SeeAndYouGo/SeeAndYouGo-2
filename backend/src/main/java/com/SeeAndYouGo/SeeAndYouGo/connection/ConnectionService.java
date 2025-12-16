package com.SeeAndYouGo.SeeAndYouGo.connection;

import com.SeeAndYouGo.SeeAndYouGo.connection.connectionProvider.ConnectionProvider;
import com.SeeAndYouGo.SeeAndYouGo.connection.connectionProvider.ConnectionProviderFactory;
import com.SeeAndYouGo.SeeAndYouGo.connection.dto.ConnectionVO;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final ConnectionProviderFactory connectionProviderFactory;

    @Value("${API.CONN_KEY}")
    private String connKey;

    @Value("${external-api.connection.operating-hours.start}")
    private String operatingStartTime;

    @Value("${external-api.connection.operating-hours.end}")
    private String operatingEndTime;

    /**
     * restaurantName에 해당하는 학생식당의 connection 정보를 반환한다.
     * @param restaurantName connectoin 정보가 필요한 학생식당
     */
    public ConnectionVO getRecentConnection(String restaurantName) throws Exception {
        // 학생식당의 이름으로 Restaurant 엔티티를 가져온다.
        String parseRestaurantName = Restaurant.parseName(restaurantName);
        Restaurant restaurant = Restaurant.valueOf(parseRestaurantName);

        // 운영시간 체크: 비운영시간이면 -1 반환
        if (!isOperatingHours()) {
            log.info("비운영시간 혼잡도 조회 요청 - restaurant: {}, 현재시간: {}", restaurant, LocalTime.now());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String currentTime = LocalDateTime.now().format(formatter);
            return new ConnectionVO(-1, currentTime, restaurant);
        }

        ConnectionProvider connectionProvider = connectionProviderFactory.getConnectionProvider(restaurant);
        return connectionProvider.getRecentConnection(restaurant);
    }

    public boolean checkSecretKey(String authKey) {
        return connKey.equals(authKey);
    }

    @Transactional
    public void saveRecentConnection() throws Exception {
        // 운영시간이 아니면 저장하지 않음
        if (!isOperatingHours()) {
            log.info("비운영시간 혼잡도 저장 요청 무시 - 현재시간: {}", LocalTime.now());
            return;
        }

        for (Restaurant restaurant : Restaurant.values()) {
            // 부르기 전에 먼저 DB에 있는지 확인한다.
            if(connectionRepository.countByRestaurant(restaurant) > 0){
                String recentTime = connectionRepository.findTopByRestaurantOrderByTimeDesc(restaurant).getTime();
                // 이 시간과 현재 시간 차이를 비교해본다.
                if(haveRecentConnection(recentTime)){
                    // 최신 데이터가 있다면 저장하지 않아도 됨.
                    return;
                }
            }

            // 최신 데이터가 없다면 저장한다.
            ConnectionProvider connectionProvider = connectionProviderFactory.getConnectionProvider(restaurant);
            connectionProvider.updateConnectionMap(restaurant);
            ConnectionVO recentConnection = connectionProvider.getRecentConnection(restaurant);
            if (recentConnection == null) {
                // 간혹 받아오지 못한 경우인데 API 키 문제인지.. 원인은 확인 필요
                return;
            }
            Connection connection = Connection.builder()
                                                .connected(recentConnection.getConnected())
                                                .time(recentConnection.getTime())
                                                .restaurant(recentConnection.getRestaurant())
                                                .build();

            connectionRepository.save(connection);
        }
    }

    private boolean haveRecentConnection(String recentTime) {
        // 현재 시간
        LocalDateTime now = LocalDateTime.now();

        // recentTime을 LocalDateTime으로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime recentDateTime = LocalDateTime.parse(recentTime, formatter);

        // 두 시간의 차이를 계산
        Duration duration = Duration.between(recentDateTime, now);

        return duration.toMinutes() < 5;
    }

    public void updateAllRestaurantMenuMap() throws Exception {
        // 운영시간이 아니면 업데이트하지 않음
        if (!isOperatingHours()) {
            log.info("비운영시간 혼잡도 캐시 업데이트 요청 무시 - 현재시간: {}", LocalTime.now());
            return;
        }

        for (Restaurant restaurant : Restaurant.values()) {
            ConnectionProvider connectionProvider = connectionProviderFactory.getConnectionProvider(restaurant);
            connectionProvider.updateConnectionMap(restaurant);
        }
    }

    public ConnectionVO getRecentConnectionMap(Restaurant restaurant) throws Exception {
        ConnectionProvider connectionProvider = connectionProviderFactory.getConnectionProvider(restaurant);
        return connectionProvider.getRecentConnectionMap(restaurant);
    }

    /**
     * 현재 시간이 운영시간인지 체크한다.
     * 운영시간이 아니면 혼잡도 조회를 하지 않고 -1을 반환한다.
     * @return true if 운영시간, false if 비운영시간
     */
    private boolean isOperatingHours() {
        LocalTime now = LocalTime.now();
        LocalTime start = LocalTime.parse(operatingStartTime);
        LocalTime end = LocalTime.parse(operatingEndTime);

        // start(06:00) <= now < end(19:30) 이면 운영시간
        // 그 외(19:30 ~ 다음날 06:00)는 비운영시간
        return !now.isBefore(start) && now.isBefore(end);
    }
}
