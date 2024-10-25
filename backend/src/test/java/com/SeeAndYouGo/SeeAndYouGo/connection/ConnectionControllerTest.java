package com.SeeAndYouGo.SeeAndYouGo.connection;

import com.SeeAndYouGo.SeeAndYouGo.TestSetUp;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class ConnectionControllerTest {

    @Autowired
    private ConnectionController connectionController;

    @Autowired
    private ConnectionRepository connectionRepository;

    private List<Connection> connections = new ArrayList<>();
    private final String oldConnectionDateTime = "2023-11-23 22:02:01";
    private final String newConnectionDateTime = "2023-11-23 23:02:01";

    @BeforeEach
    void init(){
        insertConnection();
    }

    private void insertConnection() {
        Random random = new Random();

        for (Restaurant restaurant : Restaurant.values()) {
            int connected = random.nextInt(30) + 30;
            Connection connection = TestSetUp.saveConnection(connectionRepository, connected, oldConnectionDateTime, restaurant);

            connections.add(connection);
        }

        for (Restaurant restaurant : Restaurant.values()) {
            int connected = random.nextInt(30) + 30;
            Connection connection = TestSetUp.saveConnection(connectionRepository, connected, newConnectionDateTime, restaurant);

            connections.add(connection);
        }
    }

    @DisplayName("최근 혼잡도 불러오기")
    @Test
    void getRecentConnection() throws Exception {
        // given(최신 Connection을 Connections에서 찾기)
        Restaurant restaurant = Restaurant.제2학생회관;
        Connection newConnection = getRecentConnectionInConnections(restaurant, newConnectionDateTime);

        // when
        ConnectionResponseDto getConnection = connectionController.congestionRequest(restaurant.toString());

        // then
        assertEquals(newConnection.getConnected(), getConnection.getConnected());
        assertEquals(newConnection.getTime(), getConnection.getDateTime());
        assertEquals(newConnection.getRestaurant().toString(), getConnection.getRestaurantName());
    }

    private Connection getRecentConnectionInConnections(Restaurant restaurant, String time) throws Exception {
        // 필드인 connections에서 최근 Connection 반환하기

        for (Connection connection : connections) {
            if (connection.getRestaurant().equals(restaurant) && connection.getTime().equals(time)) {
                return connection;
            }
        }

        throw new Exception("최신 connection을 찾을 수 없습니다.");
    }
}
