package com.SeeAndYouGo.SeeAndYouGo.connection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionResponseDto {
    private String status;
    private String message;
    private String restaurantName;
    private String requestedAt;

    /** 예측 요청에 사용한 관측 시각. 관측값이 없으면 null */
    private String observedAt;

    /** 예측 요청에 사용한 관측 혼잡도. 모르면 null(0으로 폴백하지 않는다) */
    private Integer observedValue;

    /** 예측 서버가 5분 격자로 반올림해 실제 사용한 시각 */
    private String observedAtSlot;

    /** 예측 서버가 current_count를 어디서 가져왔는지 (예: "observed") */
    private String currentCountSource;

    private Integer recentSlotsExpected;
    private Integer recentSlotsUsed;

    /** horizon별 예측 결과. 일부만 unavailable일 수 있다. */
    private List<PredictionResultDto> results;
}
