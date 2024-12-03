package com.SeeAndYouGo.SeeAndYouGo.connection;

import com.SeeAndYouGo.SeeAndYouGo.connection.connectionProvider.ConnectionProvider;
import com.SeeAndYouGo.SeeAndYouGo.connection.connectionProvider.ConnectionProviderFactory;
import com.SeeAndYouGo.SeeAndYouGo.connection.dto.ConnectionVO;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final ConnectionProviderFactory connectionProviderFactory;

    @Value("${CONN_KEY}")
    private String CONN_KEY;

    /**
     * restaurantName에 해당하는 학생식당의 connection 정보를 반환한다.
     * @param restaurantName connectoin 정보가 필요한 학생식당
     */
    public Connection getRecentConnection(String restaurantName){
        // 학생식당의 이름으로 Restaurant 엔티티를 가져온다.
        String parseRestaurantName = Restaurant.parseName(restaurantName);
        Restaurant restaurant = Restaurant.valueOf(parseRestaurantName);

        // 해당하는 학생식당의 가장 최신의 Connection 정보를 가져온다.
        return connectionRepository.findTopByRestaurantOrderByTimeDesc(restaurant);
    }

    public boolean checkSecretKey(String authKey) {
        return CONN_KEY.equals(authKey);
    }

    public String getRecentConnectionToString(Restaurant restaurant) throws Exception {
        ConnectionProvider connectionProvider = connectionProviderFactory.getConnectionProvider(restaurant);
        return connectionProvider.getRecentConnectionToString(restaurant);
    }

    @Transactional
    public void saveRecentConnection() throws Exception {
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
            List<ConnectionVO> recentConnection = connectionProvider.getRecentConnection(restaurant);

            // 받아온 Connection을 저장한다.
            for (ConnectionVO connectionVO : recentConnection) {
                Connection connection = Connection.builder()
                                                    .connected(connectionVO.getConnected())
                                                    .time(connectionVO.getTime())
                                                    .restaurant(connectionVO.getRestaurant())
                                                    .build();

                connectionRepository.save(connection);
            }
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

        return duration.toMinutes() <= 5;
    }
}
