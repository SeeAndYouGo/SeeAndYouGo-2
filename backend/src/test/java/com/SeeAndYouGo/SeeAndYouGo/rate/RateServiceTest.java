package com.SeeAndYouGo.SeeAndYouGo.rate;

import com.SeeAndYouGo.SeeAndYouGo.dish.DishRepository;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.dto.RestaurantDetailRateResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.dto.RestaurantRateMenuResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateServiceTest {

    @Mock
    private RateRepository rateRepository;

    @Mock
    private DishRepository dishRepository;

    private RateService rateService;

    @BeforeEach
    void setUp() {
        rateService = new RateService(rateRepository, dishRepository);
    }

    @Test
    void setRestaurant1MenuFieldDoesNotAccumulateMenusWhenCalledRepeatedly() {
        Restaurant restaurant1 = findRestaurant1();
        when(rateRepository.findByRestaurantAndDept(eq(restaurant1), anyString()))
                .thenAnswer(invocation -> {
                    Restaurant restaurant = invocation.getArgument(0);
                    String dept = invocation.getArgument(1);
                    return Rate.builder()
                            .restaurant(restaurant)
                            .dept(dept)
                            .build();
                });

        rateService.setRestaurant1MenuField();
        rateService.setRestaurant1MenuField();

        List<RestaurantDetailRateResponseDto> detailRates = rateService.getDetailRestaurantRate(restaurant1.name());

        assertThat(detailRates).isNotEmpty();
        detailRates.forEach(detailRate -> {
            List<String> menuNames = detailRate.getAvgRateByMenu().stream()
                    .map(RestaurantRateMenuResponseDto::getMenuName)
                    .collect(Collectors.toList());

            assertThat(menuNames)
                    .as("category %s", detailRate.getCategory())
                    .doesNotHaveDuplicates();
        });
    }

    private Restaurant findRestaurant1() {
        return Arrays.stream(Restaurant.values())
                .filter(restaurant -> restaurant.getNumber() == 1)
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }
}
