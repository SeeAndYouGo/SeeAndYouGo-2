package com.SeeAndYouGo.SeeAndYouGo.connection;

import com.SeeAndYouGo.SeeAndYouGo.connection.dto.PredictionResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.global.exception.ApiException;
import com.SeeAndYouGo.SeeAndYouGo.global.exception.ErrorCode;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int OBSERVATION_WINDOW_MINUTES = 5;

    private static final String STATUS_OK = "OK";

    private final ConnectionRepository connectionRepository;

    @Value("${prediction.base_url}")
    private String baseUrl;

    @Value("${prediction.health_endpoint}")
    private String healthEndpoint;

    @Value("${prediction.predict_endpoint}")
    private String predictEndpoint;

    @Value("${prediction.horizons}")
    private List<Integer> horizons;

    private final RestTemplate restTemplate = new RestTemplate();

    public PredictionResponseDto predict(String restaurantParam, String observedAt) {
        // 1. 식당 검증
        Restaurant restaurant;
        try {
            String parsedName = Restaurant.parseName(restaurantParam);
            restaurant = Restaurant.valueOf(parsedName);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("알 수 없는 식당입니다: " + restaurantParam);
        }

        // 2. observed_at 파싱
        LocalDateTime requestedTime;
        try {
            requestedTime = LocalDateTime.parse(observedAt, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("observed_at은 yyyy-MM-dd HH:mm:ss 형식이어야 합니다.");
        }

        // 3. DB에서 ±5분 윈도우 내 가장 가까운 관측값 찾기
        Optional<Connection> matched = findClosestObservation(restaurant, requestedTime);
        if (!matched.isPresent()) {
            throw new ApiException(
                    ErrorCode.PREDICTION_NO_OBSERVATION,
                    "해당 시간대(±" + OBSERVATION_WINDOW_MINUTES + "분) 관측 데이터가 없습니다."
            );
        }

        Connection observed = matched.get();

        // 4. 예측 서버 헬스체크
        if (!isPredictionServerHealthy()) {
            throw new ApiException(ErrorCode.PREDICTION_SERVER_DOWN);
        }

        // 5. 외부 예측 서버 호출 후 응답 릴레이
        try {
            Map<String, Object> predictions = callPredictionServer(restaurant, observed);
            return PredictionResponseDto.builder()
                    .status(STATUS_OK)
                    .restaurantName(restaurant.name())
                    .requestedAt(observedAt)
                    .observedAt(observed.getTime())
                    .observedValue(observed.getConnected())
                    .predictions(predictions)
                    .build();
        } catch (RestClientException e) {
            log.error("예측 서버 호출 실패: restaurant={}, observed_at={}", restaurant, observedAt, e);
            throw new ApiException(ErrorCode.PREDICTION_FAILED);
        }
    }

    @Async("asyncTaskExecutor")
    public void warmUpPrediction(Restaurant restaurant, Connection observed) {
        try {
            if (!isPredictionServerHealthy()) {
                log.warn("예측 캐시 워밍업 스킵 - 예측 서버 다운: restaurant={}", restaurant);
                return;
            }
            callPredictionServer(restaurant, observed);
            log.info("예측 캐시 워밍업 성공: restaurant={}, observed_at={}", restaurant, observed.getTime());
        } catch (Exception e) {
            log.warn("예측 캐시 워밍업 실패: restaurant={}, observed_at={}, error={}",
                    restaurant, observed.getTime(), e.getMessage());
        }
    }

    private Optional<Connection> findClosestObservation(Restaurant restaurant, LocalDateTime requestedTime) {
        String start = requestedTime.minusMinutes(OBSERVATION_WINDOW_MINUTES).format(FORMATTER);
        String end = requestedTime.plusMinutes(OBSERVATION_WINDOW_MINUTES).format(FORMATTER);

        List<Connection> candidates = connectionRepository
                .findByRestaurantAndTimeBetween(restaurant, start, end);

        return candidates.stream()
                .min(Comparator.comparingLong(c -> Math.abs(
                        Duration.between(LocalDateTime.parse(c.getTime(), FORMATTER), requestedTime).getSeconds()
                )));
    }

    private boolean isPredictionServerHealthy() {
        try {
            ResponseEntity<Void> response = restTemplate.getForEntity(
                    baseUrl + healthEndpoint, Void.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException e) {
            log.warn("예측 서버 헬스체크 실패: {}", e.getMessage());
            return false;
        }
    }

    private Map<String, Object> callPredictionServer(Restaurant restaurant, Connection observed) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("restaurant", restaurant.name());
        requestBody.put("observed_at", observed.getTime());
        requestBody.put("current_count", observed.getConnected());
        requestBody.put("horizons", horizons);
        requestBody.put("debug", false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + predictEndpoint,
                HttpMethod.POST,
                entity,
                Map.class
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> body = response.getBody();
        return body == null ? new HashMap<>() : body;
    }

}
