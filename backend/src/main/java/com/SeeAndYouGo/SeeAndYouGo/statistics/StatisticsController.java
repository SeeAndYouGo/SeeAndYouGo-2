package com.SeeAndYouGo.SeeAndYouGo.statistics;

import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class StatisticsController {
    private final StatisticsService statisticsService;

    @GetMapping("/{restaurantNumber}")
    public List<ConnectionsStatisticsResponseDto> getConnectionStatistics(@PathVariable String restaurantNumber){
        String restaurantName = Restaurant.parseName(restaurantNumber);
        return statisticsService.getConnectionStatistics(restaurantName);
    }

    @GetMapping("/{year}/{month}/{day}")
    public void test(@PathVariable int year, @PathVariable int month, @PathVariable int day){
        LocalDate date = LocalDate.of(year, month, day);
        log.info("Request to manually update connection statistics for date: {}", date);
        statisticsService.updateConnectionStatistics(date);
        log.info("Manual update of connection statistics for date: {} completed.", date);
    }
}
