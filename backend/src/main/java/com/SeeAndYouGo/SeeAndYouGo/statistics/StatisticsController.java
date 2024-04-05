package com.SeeAndYouGo.SeeAndYouGo.statistics;

import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class StatisticsController {
    private final StatisticsService statisticsService;

    @RequestMapping("/{restaurantNumber}")
    public List<ConnectionsStatisticsResponseDto> getConnectionStatistics(@PathVariable String restaurantNumber){
        String restaurantName = Restaurant.parseName(restaurantNumber);
        return statisticsService.getConnectionStatistics(restaurantName);
    }
}
