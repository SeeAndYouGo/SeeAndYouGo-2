package com.SeeAndYouGo.SeeAndYouGo.restaurant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public class Location {
    private final BigDecimal longitude;
    private final BigDecimal latitude;

    public static Location of(double longitude, double latitude){
        return new Location(BigDecimal.valueOf(longitude), BigDecimal.valueOf(latitude));
    }
}
