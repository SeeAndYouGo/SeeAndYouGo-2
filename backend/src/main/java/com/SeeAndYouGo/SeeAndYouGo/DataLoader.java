package com.SeeAndYouGo.SeeAndYouGo;

import com.SeeAndYouGo.SeeAndYouGo.Connection.ConnectionService;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final IterService iterService;
    private final ConnectionService connectionService;
    private final RestaurantService restaurantService;

    @Override
    public void run(String... args) throws Exception {
        iterService.weeklyIterative();
        connectionService.saveAndCacheConnection();
        restaurantService.setRestaurant1MenuField();
    }
}