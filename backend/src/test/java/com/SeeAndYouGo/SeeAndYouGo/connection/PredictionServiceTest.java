package com.SeeAndYouGo.SeeAndYouGo.connection;

import com.SeeAndYouGo.SeeAndYouGo.connection.dto.PredictionResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.global.exception.ApiException;
import com.SeeAndYouGo.SeeAndYouGo.global.exception.ErrorCode;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

/**
 * 예측 서버 역할을 하는 로컬 HTTP 서버로 PredictionService 의 실패 응답 매핑을 검증한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PredictionService - 예측 서버 응답 처리")
class PredictionServiceTest {

    private static final String OBSERVED_AT = "2026-10-09 12:00:00";

    @Mock private ConnectionRepository connectionRepository;

    private PredictionService predictionService;
    private HttpServer server;

    @BeforeEach
    void setUp() {
        predictionService = new PredictionService(connectionRepository);
        ReflectionTestUtils.setField(predictionService, "healthEndpoint", "/health");
        ReflectionTestUtils.setField(predictionService, "predictEndpoint", "/predict");
        ReflectionTestUtils.setField(predictionService, "horizons", Arrays.asList(10, 20, 30, 60));
        given(connectionRepository.findByRestaurantAndTimeBetween(any(), anyString(), anyString()))
                .willReturn(Collections.emptyList());
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    @DisplayName("관측값이 없어도 에러 없이 예측 결과를 반환한다")
    void predictsWithoutObservation() throws IOException {
        serve(200, 200, "{\"observed_at\":\"2026-10-09T12:00:00+09:00\",\"results\":[]}");

        PredictionResponseDto response = predictionService.predict("1", OBSERVED_AT);

        assertThat(response.getStatus()).isEqualTo("OK");
    }

    @Test
    @DisplayName("헬스체크 실패는 PREDICTION_002")
    void healthCheckFailure() throws IOException {
        serve(503, 200, "{}");

        assertErrorCode(ErrorCode.PREDICTION_SERVER_DOWN);
    }

    @Test
    @DisplayName("예측 서버가 모르는 식당(404)은 PREDICTION_003")
    void unsupportedRestaurant() throws IOException {
        serve(200, 404, "{\"detail\":\"unknown restaurant\"}");

        assertErrorCode(ErrorCode.PREDICTION_FAILED);
    }

    @Test
    @DisplayName("예측 서버가 요청을 거부(422)하면 PREDICTION_003")
    void rejectedRequest() throws IOException {
        serve(200, 422, "{\"detail\":\"invalid\"}");

        assertErrorCode(ErrorCode.PREDICTION_FAILED);
    }

    private void assertErrorCode(ErrorCode expected) {
        ApiException e = catchThrowableOfType(() -> predictionService.predict("1", OBSERVED_AT), ApiException.class);
        assertThat(e).isNotNull();
        assertThat(e.getErrorCode()).isEqualTo(expected);
    }

    private void serve(int healthStatus, int predictStatus, String predictBody) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/health", exchange -> {
            exchange.sendResponseHeaders(healthStatus, -1);
            exchange.close();
        });
        server.createContext("/predict", exchange -> {
            byte[] bytes = predictBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(predictStatus, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        server.start();
        ReflectionTestUtils.setField(predictionService, "baseUrl",
                "http://127.0.0.1:" + server.getAddress().getPort());
    }
}
