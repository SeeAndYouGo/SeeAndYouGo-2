package com.SeeAndYouGo.SeeAndYouGo.statistics;

import com.SeeAndYouGo.SeeAndYouGo.Connection.Connection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Statistics {
    @Id @GeneratedValue
    @Column(name = "statistics_id")
    private Long id;

    private String restaurantName;
    private LocalTime time;
    private LocalDate updateTime;
    private double averageConnection;
    private Integer accumulatedCount;

    @Builder
    public Statistics(String restaurantName, LocalTime time, LocalDate updateTime, double averageConnection, Integer accumulatedCount) {
        this.restaurantName = restaurantName;
        this.time = time;
        this.updateTime = updateTime;
        this.averageConnection = averageConnection;
        this.accumulatedCount = accumulatedCount;
    }

    public void updateAverageConnection(Connection connection) {
        double beforeSum = averageConnection * accumulatedCount;
        double afterSum = beforeSum + connection.getConnected();

        accumulatedCount++;
        this.averageConnection = afterSum/accumulatedCount;
    }
}
