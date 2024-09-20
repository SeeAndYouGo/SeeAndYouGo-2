package com.SeeAndYouGo.SeeAndYouGo.statistics;

import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

//    @GetMapping("/{year}/{month}/{day}")
//    public boolean test(@PathVariable int year, @PathVariable int month, @PathVariable int day) throws IOException, JSONException {
//        return CalendarService.isHoliday(LocalDate.of(year, month, day));
//    }
}
