package com.SeeAndYouGo.SeeAndYouGo.Restaurant;

import com.SeeAndYouGo.SeeAndYouGo.Dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.Dish.DishRepository;
import com.SeeAndYouGo.SeeAndYouGo.Dish.DishType;
import com.SeeAndYouGo.SeeAndYouGo.Menu.Dept;
import com.SeeAndYouGo.SeeAndYouGo.Menu.Menu;
import com.SeeAndYouGo.SeeAndYouGo.Menu.MenuRepository;
import com.SeeAndYouGo.SeeAndYouGo.Menu.MenuType;
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
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final MenuRepository menuRepository;
    private final DishRepository dishRepository;


    public Restaurant getRestaurant(String name, String date) {
        // 평일에 이 함수가 실행되면 restaurant는 찾아와질 것임.
//        return restaurantRepository.findTodayRestaurant(name, date);
        if(checkRestaurantInDate(name, date)){
            return restaurantRepository.findTodayRestaurant(name, date);
        }else{
            Restaurant restaurant = new Restaurant(name, date);
            restaurantRepository.save(restaurant);
            return restaurant;
        }
    }

    public boolean checkRestaurantInDate(String name, String date) {
        Long aLong = restaurantRepository.countNumberOfDataInDate(name, date);
        return aLong > 0 ? true : false;
    }

    public boolean checkRestaurantInDate(String date) {
        Long aLong = restaurantRepository.countNumberOfDataInDate(date);
        return aLong > 0 ? true : false;
    }

    public void deleteRestaurants(String date) {
        restaurantRepository.deleteRestaurantsMatchedDate(date);
    }

    public List<Restaurant> findAllRestaurantByDate(String place, String date) {
        return restaurantRepository.findTodayAllRestaurant(parseRestaurantName(place), date);
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
    private void createRestaurantsInDate(LocalDate date) {
        List<Restaurant>  restaurants = createRestaurantOnJson(date);
        restaurants.add(new Restaurant("2학생회관", date.toString()));
        restaurants.add(new Restaurant("3학생회관", date.toString()));
        restaurants.add(new Restaurant("상록회관", date.toString()));
        restaurants.add(new Restaurant("생활과학대", date.toString()));

        restaurantRepository.saveAll(restaurants);
    }

    @Transactional
    private List<Restaurant> createRestaurantOnJson(LocalDate date) {
        List<Restaurant> restaurants = new ArrayList<>();
        try {
            // Read the JSON file
            String jsonContent = new String(Files.readAllBytes(Paths.get("/Users/singyeongjun/Desktop/SeeAndYouGo/backend/src/main/java/com/SeeAndYouGo/SeeAndYouGo/Restaurant/restaurantFormat.json")));

            // Parse the JSON data
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonData = jsonParser.parse(jsonContent).getAsJsonObject();

            // Extract restaurantName
            String restaurantName = jsonData.get("restaurantName").toString().replace("\"", "");

            Restaurant restaurant = new Restaurant(restaurantName, date.toString());
            restaurants.add(restaurant);

            List<Dish> dishes = new ArrayList<>();
            List<Menu> menus = new ArrayList<>();

            // Extract menuName
            JsonArray menuNameArray = jsonData.getAsJsonArray("menuName");
            for (JsonElement menuJson : menuNameArray) {
                String name = menuJson.getAsJsonObject().get("name").toString().replace("\"", "");
                Integer price = Integer.parseInt(menuJson.getAsJsonObject().get("price").toString());

                Dish dish = new Dish(name, Dept.STUDENT, date.toString(), DishType.MAIN, restaurant, MenuType.LUNCH, price);
                Menu menu = new Menu(price, date.toString(), Dept.STUDENT, MenuType.LUNCH, restaurant);
                dish.setMenu(menu);
                menu.setDishList(List.of(dish));

                dishes.add(dish);
                menus.add(menu);
            }
            dishRepository.saveAll(dishes);
            menuRepository.saveAll(menus);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return restaurants;
    }
}
