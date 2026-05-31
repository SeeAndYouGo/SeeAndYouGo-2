package com.SeeAndYouGo.SeeAndYouGo.connection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

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
    private String observedAt;
    private Integer observedValue;
    private Map<String, Object> predictions;
}
