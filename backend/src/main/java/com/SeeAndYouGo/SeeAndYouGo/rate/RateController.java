package com.SeeAndYouGo.SeeAndYouGo.rate;

import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.dto.RestaurantDetailRateResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.dto.RestaurantTotalRateResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class RateController {

    private final RateService rateService;

    @GetMapping("/restaurant/{restaurantNumber}/rate/main")
    public RestaurantTotalRateResponseDto getTotalRestaurantRate(@PathVariable("restaurantNumber") Integer restaurantNumber){
        String restaurantName = Restaurant.parseName(String.valueOf(restaurantNumber));
        return rateService.getTotalRestaurantRate(restaurantName);
    }

    @GetMapping("/restaurant/{restaurantNumber}/rate/detail")
    public List<RestaurantDetailRateResponseDto> getDetailRestaurantRate(@PathVariable("restaurantNumber") Integer restaurantNumber){
        String restaurantName = Restaurant.parseName(String.valueOf(restaurantNumber));
        return rateService.getDetailRestaurantRate(restaurantName);
    }
}
