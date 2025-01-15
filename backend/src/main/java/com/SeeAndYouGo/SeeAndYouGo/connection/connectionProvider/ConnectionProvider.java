package com.SeeAndYouGo.SeeAndYouGo.connection.connectionProvider;

import com.SeeAndYouGo.SeeAndYouGo.connection.dto.ConnectionVO;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;

import java.util.List;

public interface ConnectionProvider {
    ConnectionVO getRecentConnection(Restaurant restaurant) throws Exception;
    void updateConnectionMap(Restaurant restaurant) throws Exception;
}
