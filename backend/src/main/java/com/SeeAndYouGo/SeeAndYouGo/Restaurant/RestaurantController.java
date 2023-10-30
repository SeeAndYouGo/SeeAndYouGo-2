package com.SeeAndYouGo.SeeAndYouGo.Restaurant;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
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
