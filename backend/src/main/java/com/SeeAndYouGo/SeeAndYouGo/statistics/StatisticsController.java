package com.SeeAndYouGo.SeeAndYouGo.statistics;

import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class StatisticsController {
    private final StatisticsService statisticsService;

    @GetMapping("/statistics")
    public List<ConnectionsStatisticsResponseDto> getConnectionStatistics(@RequestParam("restaurant") String restaurant){
        String restaurantName = Restaurant.parseName(restaurant);
        return statisticsService.getConnectionStatistics(restaurantName);
    }

    @GetMapping("/internal/statistics/{year}/{month}/{day}")
    public void recalculateStatistics(@PathVariable int year, @PathVariable int month, @PathVariable int day){
        statisticsService.updateConnectionStatistics(LocalDate.of(year, month, day));
    }
}
