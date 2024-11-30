package com.SeeAndYouGo.SeeAndYouGo.connection;

import com.SeeAndYouGo.SeeAndYouGo.connection.connectionProvider.ConnectionProvider;
import com.SeeAndYouGo.SeeAndYouGo.connection.connectionProvider.ConnectionProviderFactory;
import com.SeeAndYouGo.SeeAndYouGo.connection.connectionProvider.InformationCenterConnectionProvider;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Connection getRecentConnectionToString(String restaurantName){
        // 학생식당의 이름으로 Restaurant 엔티티를 가져온다.
        String parseRestaurantName = Restaurant.parseName(restaurantName);
        Restaurant restaurant = Restaurant.valueOf(parseRestaurantName);

        // 해당하는 학생식당의 가장 최신의 Connection 정보를 가져온다.
        return connectionRepository.findTopByRestaurantOrderByTimeDesc(restaurant);
    }

    public boolean checkSecretKey(String authKey) {
        return CONN_KEY.equals(authKey);
    }

    public String getRecentConnectionToString() throws Exception {
        InformationCenterConnectionProvider connectionProvider = (InformationCenterConnectionProvider) connectionProviderFactory.getConnectionProvider(Restaurant.제2학생회관);
        return connectionProvider.getRecentConnectionToString();
    }

    public void saveRecentConnection() throws Exception {
        for (Restaurant restaurant : Restaurant.values()) {
            ConnectionProvider connectionProvider = connectionProviderFactory.getConnectionProvider(restaurant);
            connectionProvider.getRecentConnection(restaurant);
        }
    }
}
