package com.SeeAndYouGo.SeeAndYouGo.user.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class NicknameUpdateResponseDto {
    private final boolean update;
    private final String lastUpdate;
}
