package com.SeeAndYouGo.SeeAndYouGo.statistics;

import com.SeeAndYouGo.SeeAndYouGo.menu.MenuType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ConnectionsStatisticsResponseDto {
    private String time;
    private Integer averageValue;
    private String type;

    public ConnectionsStatisticsResponseDto(Statistics statistics){
        this.time = statistics.getTime().toString();
        this.averageValue = (int) statistics.getAverageConnection();
        this.type = MenuType.resolveToMenuType(statistics.getTime()).toString();
    }
}
