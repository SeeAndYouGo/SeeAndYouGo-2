package com.SeeAndYouGo.SeeAndYouGo.connection.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 외부 예측 서버로 보내는 요청 바디.
 *
 * currentCount는 nullable이다. 현재 혼잡도를 모르는 경우 반드시 null을 보내야 하며,
 * 0을 보내면 예측 서버가 "사람이 정말 0명"으로 해석해 예측값이 크게 낮아진다.
 * horizons는 빈 배열일 수 없고 10/20/30/60만 허용된다.
 */
@Getter
@Builder
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PredictionServerRequest {
    private final String restaurant;
    private final String observedAt;
    private final Double currentCount;
    private final List<Integer> horizons;
    private final boolean debug;
}
