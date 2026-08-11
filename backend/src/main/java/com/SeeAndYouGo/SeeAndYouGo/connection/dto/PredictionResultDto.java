package com.SeeAndYouGo.SeeAndYouGo.connection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * horizon 하나에 대한 예측 결과.
 * horizon마다 개별 판정이므로 10/20분은 ok인데 60분만 unavailable인 응답이 정상적으로 나온다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionResultDto {
    private Integer horizonMin;
    private String targetTimestamp;

    /** status가 "ok"가 아니면 null. 클라이언트는 0으로 폴백하지 말고 "예측 불가"로 표시해야 한다. */
    private Double prediction;

    /** "ok" | "unavailable" */
    private String status;

    /** status가 "unavailable"일 때만 존재 */
    private String unavailableReason;

    public static PredictionResultDto from(PredictionServerResponse.Result result) {
        return PredictionResultDto.builder()
                .horizonMin(result.getHorizonMin())
                .targetTimestamp(result.getTargetTimestamp())
                .prediction(result.isOk() ? result.getPrediction() : null)
                .status(result.getStatus())
                .unavailableReason(result.getUnavailableReason())
                .build();
    }
}
