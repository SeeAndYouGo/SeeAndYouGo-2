package com.SeeAndYouGo.SeeAndYouGo.connection.connectionProvider;

import com.SeeAndYouGo.SeeAndYouGo.connection.dto.ConnectionVO;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;

import java.util.List;

public interface ConnectionProvider {
    List<ConnectionVO> getRecentConnection(Restaurant restaurant) throws Exception;
    String getRecentConnectionToString(Restaurant restaurant) throws Exception;
}
