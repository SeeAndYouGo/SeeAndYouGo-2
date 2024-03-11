package com.SeeAndYouGo.SeeAndYouGo.Restaurant;

import com.SeeAndYouGo.SeeAndYouGo.Dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.Dish.DishRepository;
import com.SeeAndYouGo.SeeAndYouGo.Dish.DishType;
import com.SeeAndYouGo.SeeAndYouGo.Menu.*;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.dto.RestaurantDetailRateResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.dto.RestaurantRateMenuResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.dto.RestaurantTotalRateResponseDto;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final MenuRepository menuRepository;
    private final DishRepository dishRepository;
    private Map<String, List<String>> restaurant1MenuByCategory = new HashMap<>(); // 카테고리별로 메뉴의 이름이 들어있음.
    private Map<String, Integer> restaurant1MenuByPrice = new HashMap<>(); // 메뉴와 가격이 매칭되어있음.
    private static String[] restaurants = new String[]{"1학생회관", "2학생회관", "3학생회관", "상록회관", "생활과학대"};

    public static String[] getRestaurantNames(){
        return restaurants;
    }

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

    public Restaurant getRestaurant(String name, String date) {
        // 평일에 이 함수가 실행되면 restaurant는 찾아와질 것임.
        if(checkRestaurantInDate(name, date)){
            return restaurantRepository.findByNameAndDate(name, date);
        } else{
            Restaurant restaurant = new Restaurant(name, date);
            restaurantRepository.save(restaurant);
            return restaurant;
        }
    }

    public boolean checkRestaurantInDate(String name, String date) {
        Long aLong = restaurantRepository.countByNameAndDate(name, date);
        return aLong > 0 ? true : false;
    }

    public String parseRestaurantName(String name) {
        if (name.contains("1")) return "1학생회관";
        else if (name.contains("2")) return "2학생회관";
        else if (name.contains("3")) return "3학생회관";
        else if (name.contains("4")) return "상록회관";
        else if (name.contains("5") || name.contains("생활과학대") ) return "생활과학대";
        return name;
    }

    @Transactional
    public void createWeeklyRestaurant(LocalDate nearestMonday) {

        // 날짜를 인자로 받아와서, 해당 날짜의 월요일과 해당 날짜의 금요일까지의 식당 객체를 생성.
        for (LocalDate date = nearestMonday; date.getDayOfWeek() != DayOfWeek.SATURDAY; date = date.plusDays(1)) {
            createRestaurantsInDate(date);
        }
    }

    @Transactional
    public void createRestaurantsInDate(LocalDate date) {
        List<Restaurant>  restaurants = createRestaurantOnJson(date);
        restaurants.add(new Restaurant("2학생회관", date.toString()));
        restaurants.add(new Restaurant("3학생회관", date.toString()));
        restaurants.add(new Restaurant("상록회관", date.toString()));
        restaurants.add(new Restaurant("생활과학대", date.toString()));

        restaurantRepository.saveAll(restaurants);
    }

    @Transactional
    public List<Restaurant> createRestaurantOnJson(LocalDate date) {
        List<Restaurant> restaurants = new ArrayList<>();

        try {
            // Read the JSON file
            String jsonContent = new String(Files.readAllBytes(Paths.get("src/main/java/com/SeeAndYouGo/SeeAndYouGo/Restaurant/restaurantFormat.json").toAbsolutePath()));

            // Parse the JSON data
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonData = jsonParser.parse(jsonContent).getAsJsonObject();

            // Extract restaurantName
            String restaurantName = jsonData.get("restaurantName").toString().replace("\"", "");

            Restaurant restaurant = new Restaurant(restaurantName, date.toString());
            restaurants.add(restaurant);

            List<Menu> menus = new ArrayList<>();

            // Extract menuName
            JsonArray menuNameArray = jsonData.getAsJsonArray("menuName");
            for (JsonElement menuJson : menuNameArray) {
                String name = menuJson.getAsJsonObject().get("name").toString().replace("\"", "");
                Dept dept = Dept.valueOf(menuJson.getAsJsonObject().get("dept").toString().replace("\"", ""));
                Integer price = Integer.parseInt(menuJson.getAsJsonObject().get("price").toString());

                if (dishRepository.findByName(name) == null) {
                    dishRepository.save(Dish.builder()
                                        .name(name)
                                        .dishType(DishType.MAIN)
                                        .build());
                }

                Dish dish = dishRepository.findByName(name);
                Menu menu = Menu.builder()
                                .price(price)
                                .date(date.toString())
                                .dept(dept)
                                .menuType(MenuType.LUNCH)
                                .restaurant(restaurant)
                                .build();

                menu.setDishList(List.of(dish));
                menus.add(menu);
            }
            menuRepository.saveAll(menus);
        } catch (IOException e) {
            e.printStackTrace();
            restaurant1MenuByCategory = new HashMap<>();
        }

        return restaurants;
    }

    public boolean existWeekRestaurant(LocalDate nearestMonday) {
        return restaurantRepository.existsByDate(nearestMonday.toString());
    }

    public RestaurantTotalRateResponseDto getTotalRestaurantRate(Integer restaurantNumber) {
        String restaurantName = parseRestaurantName(String.valueOf(restaurantNumber));

        List<Restaurant> restaurants = restaurantRepository.findAllByName(restaurantName);
        double avgRate = calculateRestaurantAvgRate(restaurants);
        return RestaurantTotalRateResponseDto.builder()
                    .totalAvgRate(avgRate)
                    .build();

//        현재는 1학 리뷰의 전체를 하므로 아래의 코드는 쓰지 않는다. 아래의 코드는 당일 1학에 대한 평점을 반환하는 코드이다.
//        Restaurant restaurant = restaurantRepository.findByNameAndDate(restaurantName, LocalDate.now().toString()).get(0);
//        return RestaurantTotalRateResponseDto.builder()
//                .totalAvgRate(restaurant.getRestaurantRate())
//                .build();
    }

    private double calculateRestaurantAvgRate(List<Restaurant> restaurants) {
        double sum = 0.0;
        int count = 0;

        for (Restaurant restaurant : restaurants) {
            if(restaurant.getRestaurantRate() != 0.0){
                count++;
                sum += restaurant.getRestaurantRate();
            }
        }

        return count == 0 ? 0.0 : (sum/count);
    }

    public List<RestaurantDetailRateResponseDto> getDetailRestaurantRate(Integer restaurantNumber) {
        String restaurantName = parseRestaurantName(String.valueOf(restaurantNumber));

        // detail에서 보여지는 평점은 메인메뉴에 대한 평점이다.
        // 만약 메인메뉴가 없다면 평점은 우선 0.0점으로 가자.
        // 현재는 1학만 구현되어있는 상태이다.
        return getDetailRestaurant1Rate(restaurantName, LocalDate.now().toString());
    }

    /**
     * 1학에서제공되는 평점을 뽑아온다.
     */
    private List<RestaurantDetailRateResponseDto> getDetailRestaurant1Rate(String restaurantName, String date) {
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
}