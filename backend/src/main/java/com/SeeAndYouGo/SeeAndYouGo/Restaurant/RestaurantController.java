package com.SeeAndYouGo.SeeAndYouGo.Restaurant;

import com.SeeAndYouGo.SeeAndYouGo.Restaurant.dto.RestaurantDetailRateResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.dto.RestaurantTotalRateResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class RestaurantController {

    private final RestaurantService restaurantService;

//    @GetMapping("/reviewRateAverage/{restaurant}")
//    public ResponseEntity<RestaurantRateResponseDto> restaurantMenuDay(@PathVariable("restaurant") String place) {
//        String date = LocalDate.now().toString();
//        List<Restaurant> restaurants = restaurantService.findAllRestaurantByDate(place, date);
//
//        RestaurantRateResponseDto restaurantRateResponseDto = new RestaurantRateResponseDto(restaurants);
//        return ResponseEntity.ok(restaurantRateResponseDto);
//    }

    @GetMapping("/restaurant/{restaurantNumber}/rate/main")
    public RestaurantTotalRateResponseDto getTotalRestaurantRate(@PathVariable("restaurantNumber") Integer restaurantNumber){
        String restaurantName = Restaurant.parseName(String.valueOf(restaurantNumber));
        return restaurantService.getTotalRestaurantRate(restaurantName);
    }

    @GetMapping("/restaurant/{restaurantNumber}/rate/detail")
    public List<RestaurantDetailRateResponseDto> getDetailRestaurantRate(@PathVariable("restaurantNumber") Integer restaurantNumber){
        String restaurantName = Restaurant.parseName(String.valueOf(restaurantNumber));
        return restaurantService.getDetailRestaurantRate(restaurantName);
    }
}