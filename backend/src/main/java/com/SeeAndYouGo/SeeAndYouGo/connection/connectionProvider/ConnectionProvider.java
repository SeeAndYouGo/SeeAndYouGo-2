package com.SeeAndYouGo.SeeAndYouGo.connection.connectionProvider;

import com.SeeAndYouGo.SeeAndYouGo.connection.Connection;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;

import java.util.List;

public interface ConnectionProvider {
    List<Connection> getRecentConnection(Restaurant restaurant) throws Exception;
}
