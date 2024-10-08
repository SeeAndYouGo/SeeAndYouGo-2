package com.SeeAndYouGo.SeeAndYouGo.Rate;

import com.SeeAndYouGo.SeeAndYouGo.Dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.Dish.DishRepository;
import com.SeeAndYouGo.SeeAndYouGo.Menu.Dept;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.dto.RestaurantDetailRateResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.dto.RestaurantRateMenuResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.dto.RestaurantTotalRateResponseDto;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RateService {

    private Map<String, List<String>> restaurant1MenuByCategory = new HashMap<>(); // 카테고리별로 메뉴의 이름이 들어있음.
    private Map<String, Integer> restaurant1MenuByPrice = new HashMap<>(); // 메뉴와 가격이 매칭되어있음.
    private final RateRepository rateRepository;
    private final DishRepository dishRepository;

    public void setRestaurant1MenuField() {
        try {
            // Read the JSON file
            String jsonContent = new String(Files.readAllBytes(Paths.get("src/main/java/com/SeeAndYouGo/SeeAndYouGo/Restaurant/restaurantFormat.json").toAbsolutePath()));

            // Parse the JSON data
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonData = jsonParser.parse(jsonContent).getAsJsonObject();

            // Extract menuName
            JsonArray menuNameArray = jsonData.getAsJsonArray("menuName");
            for (JsonElement menuJson : menuNameArray) {
                String name = menuJson.getAsJsonObject().get("name").toString().replace("\"", "");
                Dept dept = Dept.valueOf(menuJson.getAsJsonObject().get("dept").toString().replace("\"", ""));
                Integer price = Integer.parseInt(menuJson.getAsJsonObject().get("price").toString());

                List<String> dishesByDept = restaurant1MenuByCategory.get(dept.toString());
                // 1학 메뉴가 초기화되지 않았다면 가장 처음 초기화해주는 작업.
                if (dishesByDept == null) {
                    dishesByDept = new ArrayList<>();
                }

                dishesByDept.add(name);
                restaurant1MenuByCategory.put(dept.toString(), dishesByDept);
                restaurant1MenuByPrice.put(name, price);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    @Cacheable(value="getTotalRestaurantRate", key="#restaurantName")
    public RestaurantTotalRateResponseDto getTotalRestaurantRate(String restaurantName) {
        Restaurant restaurant = Restaurant.valueOf(restaurantName);
        Rate rateByRestaurant = rateRepository.findByRestaurant(restaurant);

        return new RestaurantTotalRateResponseDto(rateByRestaurant);
//        현재는 1학 리뷰의 전체를 하므로 아래의 코드는 쓰지 않는다. 아래의 코드는 당일 1학에 대한 평점을 반환하는 코드이다.
//        Restaurant restaurant = restaurantRepository.findByNameAndDate(restaurantName, LocalDate.now().toString()).get(0);
//        return RestaurantTotalRateResponseDto.builder()
//                .totalAvgRate(restaurant.getRestaurantRate())
//                .build();
    }

    /**
     * 현재는 1학만 세부 평점을 제공하므로 parameter인 restaurantName은 필요가 없다.
     */
    @Cacheable(value="getDetailRestaurantRate", key="#restaurantName")
    public List<RestaurantDetailRateResponseDto> getDetailRestaurantRate(String restaurantName) {
        return getDetailRestaurant1Rate();
    }

    /**
     * 1학에서제공되는 평점을 뽑아온다.
     */
    private List<RestaurantDetailRateResponseDto> getDetailRestaurant1Rate() {
        List<RestaurantDetailRateResponseDto> detailRate = new ArrayList<>();
        for (String deptToString : restaurant1MenuByCategory.keySet()) {
            List<String> dishNames = restaurant1MenuByCategory.get(deptToString);
            List<RestaurantRateMenuResponseDto> dishRate = new ArrayList<>();
            for (String dishName : dishNames) {
                Dish dish = dishRepository.findByName(dishName);
                double rate = dish.getRateByDish();

                RestaurantRateMenuResponseDto rateDto = RestaurantRateMenuResponseDto.builder()
                        .menuName(dishName)
                        .price(restaurant1MenuByPrice.get(dishName))
                        .averageRate(rate)
                        .build();

                dishRate.add(rateDto);
            }

            RestaurantDetailRateResponseDto detailRateDto = RestaurantDetailRateResponseDto.builder()
                    .category(deptToString)
                    .avgRateByMenu(dishRate)
                    .build();

            detailRate.add(detailRateDto);
        }

        return detailRate;
    }

    /**
     * 입력받은 restaurant에 rate를 등록해준다.
     */
    @Transactional
    public void updateRateByRestaurant(Restaurant restaurant, Double rate) {
        Rate rateByRestaurant = rateRepository.findByRestaurant(restaurant);
        rateByRestaurant.reflectRate(rate);
    }

    public boolean exists() {
        return rateRepository.count() > 0;
    }

    @Transactional
    public void insertAllRestaurant() {
        Restaurant[] restaurants = Restaurant.values();

        List<Rate> rates = new ArrayList<>();
        for (Restaurant restaurant : restaurants) {
            Rate rate = Rate.builder()
                            .restaurant(restaurant)
                            .build();

            rates.add(rate);
        }

        rateRepository.saveAll(rates);
    }
}
