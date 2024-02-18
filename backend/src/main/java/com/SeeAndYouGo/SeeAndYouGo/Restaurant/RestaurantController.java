package com.SeeAndYouGo.SeeAndYouGo.Restaurant;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
        return restaurantService.getTotalRestaurantRate(restaurantNumber);
    }


}