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

    @GetMapping("/ratings")
    public RestaurantTotalRateResponseDto getTotalRestaurantRate(@RequestParam("restaurant") String restaurant){
        String restaurantName = Restaurant.parseName(restaurant);
        return rateService.getTotalRestaurantRate(restaurantName);
    }

    @GetMapping("/ratings/menus")
    public List<RestaurantDetailRateResponseDto> getDetailRestaurantRate(@RequestParam("restaurant") String restaurant){
        String restaurantName = Restaurant.parseName(restaurant);
        return rateService.getDetailRestaurantRate(restaurantName);
    }
}
