package com.SeeAndYouGo.SeeAndYouGo.connection.connectionProvider;

import com.SeeAndYouGo.SeeAndYouGo.connection.Connection;
import com.SeeAndYouGo.SeeAndYouGo.connection.ConnectionRepository;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DormitoryConnectionProvider implements ConnectionProvider{

    private final ConnectionRepository connectionRepository;

    @Value("${URL.DORM_CONN_URL}")
    private String CONN_URL;

    @Override
    public List<Connection> getRecentConnection(Restaurant restaurant) {
        return List.of();
    }
}
