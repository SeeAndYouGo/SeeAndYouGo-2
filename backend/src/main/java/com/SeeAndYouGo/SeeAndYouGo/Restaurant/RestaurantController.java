package com.SeeAndYouGo.SeeAndYouGo.Restaurant;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping("/reviewRateAverage/{restaurant}")
    public ResponseEntity<RestaurantRateResponse> restaurantMenuDay(@PathVariable("restaurant") String place) {
        String date = LocalDate.now().toString();
        List<Restaurant> restaurants = restaurantService.findAllRestaurantByDate(place, date);

        RestaurantRateResponse restaurantResponse = new RestaurantRateResponse();
        restaurantResponse.setRestaurantName(restaurants.get(0).getName());

        double restaurantRate = 0;
        for (Restaurant restaurant : restaurants) {
            restaurantRate += restaurant.getRestaurantRate();
        }

        restaurantResponse.setRateAverage(restaurantRate/restaurants.size());
        return ResponseEntity.ok(restaurantResponse);
    }
}
