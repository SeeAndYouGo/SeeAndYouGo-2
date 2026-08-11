package com.SeeAndYouGo.SeeAndYouGo.connection.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 외부 예측 서버의 응답 바디.
 * 필드 추가는 하위 호환으로 계속 발생하므로 모르는 필드는 무시한다.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PredictionServerResponse {

    public static final String RESULT_STATUS_OK = "ok";

    /** 서버가 5분 격자로 반올림해 실제 사용한 시각 (11:32 → 11:30) */
    private String observedAt;

    /** 우리가 보낸 원본 시각 */
    private String observedAtInput;

    private String restaurant;

    private DataQuality dataQuality;

    private List<Result> results;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataQuality {
        /** 예: "observed" - 예측 서버가 current_count를 어디서 가져왔는지 */
        private String currentCountSource;
        private Integer recentSlotsExpected;
        /** 이 값이 지속적으로 1이면 예측 서버 쪽 데이터 문제 신호이므로 모니터링 대상이다. */
        private Integer recentSlotsUsed;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private Integer horizonMin;
        private String targetTimestamp;

        /** status가 "ok"가 아니면 null이다. 0으로 폴백하면 안 된다. */
        private Double prediction;

        /** "ok" | "unavailable" */
        private String status;

        /**
         * status가 "unavailable"일 때만 존재한다.
         * no_baseline_for_target_slot / no_baseline_for_current_slot / no_coefficients_for_target_meal_period
         */
        private String unavailableReason;

        public boolean isOk() {
            return RESULT_STATUS_OK.equals(status);
        }
    }
}
