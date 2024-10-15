package com.SeeAndYouGo.SeeAndYouGo.Connection;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.doReturn;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class ConnectionControllerTest {

    @Autowired
    private ConnectionController connectionController;

    @Autowired
    private ConnectionService connectionService;


//    @DisplayName("최근 혼잡도 불러오기")
//    @Test
//    void getRecentConnection() {
//        // given
//        // when
//        doReturn()
//    }
}
