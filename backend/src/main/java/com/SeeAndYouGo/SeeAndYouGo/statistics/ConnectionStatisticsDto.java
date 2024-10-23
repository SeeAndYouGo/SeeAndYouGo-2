package com.SeeAndYouGo.SeeAndYouGo.statistics;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ConnectionStatisticsDto {
    private double avgConnection;
    private String time;
    private String restaurantName;
    private Integer accumulatedCount;
}
