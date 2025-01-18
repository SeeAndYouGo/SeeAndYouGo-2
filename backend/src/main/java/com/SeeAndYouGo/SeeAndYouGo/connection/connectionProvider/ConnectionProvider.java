package com.SeeAndYouGo.SeeAndYouGo.connection.connectionProvider;

import com.SeeAndYouGo.SeeAndYouGo.connection.dto.ConnectionVO;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;

public interface ConnectionProvider {
    ConnectionVO getRecentConnection(Restaurant restaurant) throws Exception;
    ConnectionVO getRecentConnectionMap(Restaurant restaurant) throws Exception;
    void updateConnectionMap(Restaurant restaurant) throws Exception;
}
