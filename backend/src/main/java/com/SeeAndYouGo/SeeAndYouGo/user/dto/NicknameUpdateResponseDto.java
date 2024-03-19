package com.SeeAndYouGo.SeeAndYouGo.user.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class NicknameUpdateResponseDto {
    private final String error;
    private final String message;
    private final String lastUpdate;

    private NicknameUpdateResponseDto(String lastUpdate) {
        this.error = "Nickname update failed";
        this.message = "Nickname can be updated after 14 days";
        this.lastUpdate = lastUpdate;
    }

    public static NicknameUpdateResponseDto getInstance(String lastUpdate) {
        return new NicknameUpdateResponseDto(lastUpdate);
    }
}
