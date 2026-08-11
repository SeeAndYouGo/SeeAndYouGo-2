package com.SeeAndYouGo.SeeAndYouGo.connection;

import com.SeeAndYouGo.SeeAndYouGo.connection.dto.PredictionResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.connection.dto.PredictionResultDto;
import com.SeeAndYouGo.SeeAndYouGo.connection.dto.PredictionServerRequest;
import com.SeeAndYouGo.SeeAndYouGo.connection.dto.PredictionServerResponse;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int OBSERVATION_WINDOW_MINUTES = 5;

    /** 예측 서버는 오프셋이 포함된 ISO-8601을 권장한다. 관측 시각은 모두 KST 기준이다. */
    private static final ZoneOffset KST = ZoneOffset.ofHours(9);

    /** 예측 서버가 허용하는 horizon 값. 그 외를 보내면 400이 떨어진다. */
    private static final Set<Integer> SUPPORTED_HORIZONS =
            Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(10, 20, 30, 60)));

    private static final String STATUS_OK = "OK";
    private static final String STATUS_INVALID_REQUEST = "INVALID_REQUEST";
    private static final String STATUS_UNSUPPORTED_RESTAURANT = "UNSUPPORTED_RESTAURANT";
    private static final String STATUS_PREDICTION_SERVER_DOWN = "PREDICTION_SERVER_DOWN";
    private static final String STATUS_PREDICTION_FAILED = "PREDICTION_FAILED";

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

    /**
     * 예측 서버는 10/20/30/60 외의 horizon에 400을, 빈 배열에 422를 반환한다.
     * 설정이 잘못돼도 예측 기능만 조용히 죽지 않도록 부팅 시점에 걸러내고 로그를 남긴다.
     */
    @PostConstruct
    void sanitizeHorizons() {
        List<Integer> configured = horizons == null ? Collections.emptyList() : horizons;
        List<Integer> valid = configured.stream()
                .filter(SUPPORTED_HORIZONS::contains)
                .collect(Collectors.toList());

        if (valid.size() != configured.size()) {
            log.error("지원하지 않는 horizon 설정을 제외합니다. 허용값={}, 설정값={}", SUPPORTED_HORIZONS, configured);
        }
        this.horizons = valid.isEmpty() ? new ArrayList<>(SUPPORTED_HORIZONS) : valid;
    }

    public PredictionResponseDto predict(String restaurantParam, String observedAt) {
        // 1. 식당 검증
        Restaurant restaurant;
        try {
            String parsedName = Restaurant.parseName(restaurantParam);
            restaurant = Restaurant.valueOf(parsedName);
        } catch (IllegalArgumentException e) {
            return errorResponse(STATUS_INVALID_REQUEST,
                    "알 수 없는 식당입니다: " + restaurantParam,
                    null, observedAt);
        }

        // 2. observed_at 파싱
        LocalDateTime requestedTime;
        try {
            requestedTime = LocalDateTime.parse(observedAt, FORMATTER);
        } catch (DateTimeParseException e) {
            return errorResponse(STATUS_INVALID_REQUEST,
                    "observed_at은 yyyy-MM-dd HH:mm:ss 형식이어야 합니다.",
                    restaurant.name(), observedAt);
        }

        // 3. DB에서 ±5분 윈도우 내 가장 가까운 관측값 찾기.
        //    없으면 current_count를 null로 보내 베이스라인 기반 예측을 받는다. 0으로 보내면 안 된다.
        Connection observed = findClosestObservation(restaurant, requestedTime).orElse(null);
        LocalDateTime observationTime = observed == null ? requestedTime : parseTime(observed.getTime(), requestedTime);
        Double currentCount = resolveCurrentCount(observed);

        if (currentCount == null) {
            log.info("관측 혼잡도 없이 예측 요청합니다(current_count=null). restaurant={}, observed_at={}",
                    restaurant, observedAt);
        }

        // 4. 예측 서버 헬스체크
        if (!isPredictionServerHealthy()) {
            return baseResponse(STATUS_PREDICTION_SERVER_DOWN, restaurant, observedAt, observed)
                    .message("예측 서버가 응답하지 않습니다.")
                    .build();
        }

        // 5. 외부 예측 서버 호출 후 응답 릴레이
        try {
            PredictionServerResponse prediction = callPredictionServer(restaurant, observationTime, currentCount);
            return toResponse(restaurant, observedAt, observed, prediction);
        } catch (HttpClientErrorException.NotFound e) {
            // 404는 이제 "예측 서버가 모르는 식당"인 경우에만 발생한다.
            log.warn("예측 서버가 모르는 식당입니다: restaurant={}", restaurant);
            return baseResponse(STATUS_UNSUPPORTED_RESTAURANT, restaurant, observedAt, observed)
                    .message("예측을 지원하지 않는 식당입니다.")
                    .build();
        } catch (HttpClientErrorException e) {
            // 400: 지원하지 않는 horizon 값, 422: 요청 형식 오류. 둘 다 우리 쪽 요청 문제다.
            log.error("예측 서버 요청이 거부되었습니다: restaurant={}, observed_at={}, status={}, body={}",
                    restaurant, observedAt, e.getStatusCode(), e.getResponseBodyAsString());
            return baseResponse(STATUS_PREDICTION_FAILED, restaurant, observedAt, observed)
                    .message("예측 서버가 요청을 거부했습니다: " + e.getStatusCode())
                    .build();
        } catch (RestClientException e) {
            log.error("예측 서버 호출 실패: restaurant={}, observed_at={}", restaurant, observedAt, e);
            return baseResponse(STATUS_PREDICTION_FAILED, restaurant, observedAt, observed)
                    .message("예측 서버 호출 실패: " + e.getMessage())
                    .build();
        }
    }

    @Async("asyncTaskExecutor")
    public void warmUpPrediction(Restaurant restaurant, Connection observed) {
        try {
            if (!isPredictionServerHealthy()) {
                log.warn("예측 캐시 워밍업 스킵 - 예측 서버 다운: restaurant={}", restaurant);
                return;
            }
            LocalDateTime observationTime = parseTime(observed.getTime(), null);
            if (observationTime == null) {
                log.warn("예측 캐시 워밍업 스킵 - 관측 시각 파싱 실패: restaurant={}, time={}",
                        restaurant, observed.getTime());
                return;
            }
            callPredictionServer(restaurant, observationTime, resolveCurrentCount(observed));
            log.info("예측 캐시 워밍업 성공: restaurant={}, observed_at={}", restaurant, observed.getTime());
        } catch (Exception e) {
            log.warn("예측 캐시 워밍업 실패: restaurant={}, observed_at={}, error={}",
                    restaurant, observed.getTime(), e.getMessage());
        }
    }

    /**
     * 현재 혼잡도를 모르면 null을 반환한다.
     * 0을 보내면 예측 서버가 "사람이 정말 0명"으로 해석해 예측값이 40%가량 낮아진다.
     * 비운영시간 센티널(-1)도 관측값이 아니므로 null로 취급한다.
     */
    private Double resolveCurrentCount(Connection observed) {
        if (observed == null || observed.getConnected() == null || observed.getConnected() < 0) {
            return null;
        }
        return observed.getConnected().doubleValue();
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

    private PredictionServerResponse callPredictionServer(Restaurant restaurant,
                                                          LocalDateTime observationTime,
                                                          Double currentCount) {
        PredictionServerRequest requestBody = PredictionServerRequest.builder()
                .restaurant(restaurant.name())
                .observedAt(observationTime.atOffset(KST).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .currentCount(currentCount)
                .horizons(horizons)
                .debug(false)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PredictionServerRequest> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<PredictionServerResponse> response = restTemplate.exchange(
                baseUrl + predictEndpoint,
                HttpMethod.POST,
                entity,
                PredictionServerResponse.class
        );

        PredictionServerResponse body = response.getBody();
        if (body == null) {
            throw new RestClientException("예측 서버 응답 바디가 비어 있습니다.");
        }

        logDataQuality(restaurant, body);
        return body;
    }

    /**
     * recent_slots_used가 계속 1이면 예측 서버 쪽에 데이터 문제가 남아 있다는 신호라 모니터링이 필요하다.
     */
    private void logDataQuality(Restaurant restaurant, PredictionServerResponse body) {
        PredictionServerResponse.DataQuality quality = body.getDataQuality();
        if (quality == null) {
            log.warn("예측 서버 응답에 data_quality가 없습니다: restaurant={}", restaurant);
            return;
        }

        log.info("예측 데이터 품질: restaurant={}, observed_at={}, current_count_source={}, recent_slots_used={}/{}",
                restaurant, body.getObservedAt(), quality.getCurrentCountSource(),
                quality.getRecentSlotsUsed(), quality.getRecentSlotsExpected());

        if (quality.getRecentSlotsUsed() != null && quality.getRecentSlotsUsed() <= 1) {
            log.warn("예측에 사용된 최근 슬롯이 {}개뿐입니다. 지속되면 예측 서버 데이터 문제입니다: restaurant={}, observed_at={}",
                    quality.getRecentSlotsUsed(), restaurant, body.getObservedAt());
        }
    }

    private PredictionResponseDto toResponse(Restaurant restaurant, String requestedAt,
                                             Connection observed, PredictionServerResponse prediction) {
        List<PredictionResultDto> results = prediction.getResults() == null
                ? Collections.emptyList()
                : prediction.getResults().stream()
                        .map(PredictionResultDto::from)
                        .collect(Collectors.toList());

        PredictionResponseDto.PredictionResponseDtoBuilder builder =
                baseResponse(STATUS_OK, restaurant, requestedAt, observed)
                        .observedAtSlot(prediction.getObservedAt())
                        .results(results);

        PredictionServerResponse.DataQuality quality = prediction.getDataQuality();
        if (quality != null) {
            builder.currentCountSource(quality.getCurrentCountSource())
                    .recentSlotsExpected(quality.getRecentSlotsExpected())
                    .recentSlotsUsed(quality.getRecentSlotsUsed());
        }

        return builder.build();
    }

    private PredictionResponseDto.PredictionResponseDtoBuilder baseResponse(String status, Restaurant restaurant,
                                                                            String requestedAt, Connection observed) {
        return PredictionResponseDto.builder()
                .status(status)
                .restaurantName(restaurant.name())
                .requestedAt(requestedAt)
                .observedAt(observed == null ? null : observed.getTime())
                .observedValue(observed == null ? null : observed.getConnected());
    }

    private LocalDateTime parseTime(String time, LocalDateTime fallback) {
        try {
            return LocalDateTime.parse(time, FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("관측 시각 파싱 실패: time={}", time);
            return fallback;
        }
    }

    private PredictionResponseDto errorResponse(String status, String message,
                                                String restaurantName, String requestedAt) {
        return PredictionResponseDto.builder()
                .status(status)
                .message(message)
                .restaurantName(restaurantName)
                .requestedAt(requestedAt)
                .build();
    }
}
