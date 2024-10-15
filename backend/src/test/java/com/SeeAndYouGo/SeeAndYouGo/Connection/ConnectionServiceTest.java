package com.SeeAndYouGo.SeeAndYouGo.Connection;

import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class ConnectionServiceTest {
    @Autowired private ConnectionService connectionService;
    @Autowired private ConnectionRepository connectionRepository;

    @Test
    public void 최근_혼잡도_불러오기() throws Exception {
        // given
        Connection oldConnection = Connection.builder()
                .connected(32)
                .time("2023-11-23 10:05:01")
                .restaurant(Restaurant.제1학생회관)
                .build();

        Connection newConnection = Connection.builder()
                .connected(32)
                .time("2023-11-23 12:15:01")
                .restaurant(Restaurant.제1학생회관)
                .build();

        connectionRepository.save(oldConnection);
        connectionRepository.save(newConnection);

        // when
        connectionService.getRecentConnected("제1학생회관");

        // then

    }
}
