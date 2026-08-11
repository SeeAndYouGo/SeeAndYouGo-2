package com.SeeAndYouGo.SeeAndYouGo.connection;

import com.SeeAndYouGo.SeeAndYouGo.connection.dto.PredictionResultDto;
import com.SeeAndYouGo.SeeAndYouGo.connection.dto.PredictionServerRequest;
import com.SeeAndYouGo.SeeAndYouGo.connection.dto.PredictionServerResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 테스트 목표: 예측 서버와 주고받는 JSON 계약을 고정한다.
 *  - 요청은 snake_case이고 current_count는 모르면 null로 나간다(0으로 폴백하면 안 됨).
 *  - 응답의 prediction은 null일 수 있고, status/unavailable_reason이 함께 온다.
 *  - horizon마다 판정이 다르므로 일부만 unavailable인 응답도 파싱된다.
 */
@DisplayName("혼잡도 예측 서버 JSON 계약 - PredictionServerRequest/Response")
class PredictionDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("요청은 snake_case로 직렬화되고, 혼잡도를 모르면 current_count가 null로 나간다")
    void serializeRequestWithNullCurrentCount() throws Exception {
        PredictionServerRequest request = PredictionServerRequest.builder()
                .restaurant("제1학생회관")
                .observedAt("2026-05-11T11:32:41+09:00")
                .currentCount(null)
                .horizons(Arrays.asList(10, 20, 30, 60))
                .debug(false)
                .build();

        String json = objectMapper.writeValueAsString(request);

        assertThat(objectMapper.readTree(json).get("current_count").isNull()).isTrue();
        assertThat(json).contains("\"observed_at\":\"2026-05-11T11:32:41+09:00\"");
        assertThat(json).contains("\"horizons\":[10,20,30,60]");
        assertThat(json).doesNotContain("currentCount");
    }

    @Test
    @DisplayName("혼잡도를 알면 current_count가 숫자로 나간다")
    void serializeRequestWithObservedCurrentCount() throws Exception {
        PredictionServerRequest request = PredictionServerRequest.builder()
                .restaurant("제1학생회관")
                .observedAt("2026-05-11T11:32:41+09:00")
                .currentCount(30.0)
                .horizons(Arrays.asList(10, 60))
                .debug(false)
                .build();

        assertThat(objectMapper.readTree(objectMapper.writeValueAsString(request))
                .get("current_count").asDouble()).isEqualTo(30.0);
    }

    @Test
    @DisplayName("horizon별로 ok와 unavailable이 섞인 응답을 파싱한다")
    void deserializeMixedResults() throws Exception {
        String body = "{"
                + "\"restaurant\":\"제1학생회관\","
                + "\"observed_at\":\"2026-05-11T11:30:00\","
                + "\"observed_at_input\":\"2026-05-11T11:32:41\","
                + "\"data_quality\":{\"current_count_source\":\"observed\","
                + "\"recent_slots_expected\":5,\"recent_slots_used\":5},"
                + "\"results\":["
                + "{\"horizon_min\":10,\"target_timestamp\":\"2026-05-11T11:40:00\","
                + "\"prediction\":84.2,\"status\":\"ok\"},"
                + "{\"horizon_min\":60,\"target_timestamp\":\"2026-05-11T12:30:00\","
                + "\"prediction\":null,\"status\":\"unavailable\","
                + "\"unavailable_reason\":\"no_baseline_for_target_slot\"}"
                + "]}";

        PredictionServerResponse response = objectMapper.readValue(body, PredictionServerResponse.class);

        assertThat(response.getObservedAt()).isEqualTo("2026-05-11T11:30:00");
        assertThat(response.getObservedAtInput()).isEqualTo("2026-05-11T11:32:41");
        assertThat(response.getDataQuality().getCurrentCountSource()).isEqualTo("observed");
        assertThat(response.getDataQuality().getRecentSlotsUsed()).isEqualTo(5);

        PredictionServerResponse.Result ok = response.getResults().get(0);
        assertThat(ok.isOk()).isTrue();
        assertThat(ok.getPrediction()).isEqualTo(84.2);

        PredictionServerResponse.Result unavailable = response.getResults().get(1);
        assertThat(unavailable.isOk()).isFalse();
        assertThat(unavailable.getPrediction()).isNull();
        assertThat(unavailable.getUnavailableReason()).isEqualTo("no_baseline_for_target_slot");
    }

    @Test
    @DisplayName("모르는 필드가 추가돼도 파싱에 실패하지 않는다")
    void ignoreUnknownFields() throws Exception {
        String body = "{\"restaurant\":\"제1학생회관\",\"brand_new_field\":123,"
                + "\"results\":[{\"horizon_min\":10,\"prediction\":1.0,\"status\":\"ok\",\"extra\":true}]}";

        PredictionServerResponse response = objectMapper.readValue(body, PredictionServerResponse.class);

        assertThat(response.getResults()).hasSize(1);
    }

    @Test
    @DisplayName("unavailable 결과는 예측값 없이 사유만 담아 전달한다")
    void mapUnavailableResultToDto() {
        PredictionServerResponse.Result result = new PredictionServerResponse.Result();
        result.setHorizonMin(60);
        result.setStatus("unavailable");
        result.setUnavailableReason("no_coefficients_for_target_meal_period");
        result.setPrediction(12.3); // 서버가 값을 함께 보내더라도 무시해야 한다.

        PredictionResultDto dto = PredictionResultDto.from(result);

        assertThat(dto.getPrediction()).isNull();
        assertThat(dto.getStatus()).isEqualTo("unavailable");
        assertThat(dto.getUnavailableReason()).isEqualTo("no_coefficients_for_target_meal_period");
    }
}
