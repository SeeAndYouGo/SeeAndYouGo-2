package com.SeeAndYouGo.SeeAndYouGo.statistics;

import com.SeeAndYouGo.SeeAndYouGo.connection.Connection;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Statistics {
    @Id @GeneratedValue
    @Column(name = "statistics_id")
    private Long id;

    @Enumerated(value = EnumType.STRING)
    private Restaurant restaurant;
    private LocalTime time;
    private LocalDate updateTime;
    private double averageConnection;
    private Integer accumulatedCount;

    @Builder
    public Statistics(Restaurant restaurant, LocalTime time, LocalDate updateTime, double averageConnection, Integer accumulatedCount) {
        this.restaurant = restaurant;
        this.time = time;
        this.updateTime = updateTime;
        this.averageConnection = averageConnection;
        this.accumulatedCount = accumulatedCount;
    }

    public void updateAverageConnection(Connection connection, LocalDate date) {
        double beforeSum = averageConnection * accumulatedCount;
        double afterSum = beforeSum + connection.getConnected();

        accumulatedCount++;
        this.averageConnection = afterSum/accumulatedCount;

        this.updateTime = date;
    }
}
