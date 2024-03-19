package com.SeeAndYouGo.SeeAndYouGo.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
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
