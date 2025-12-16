package com.SeeAndYouGo.SeeAndYouGo.rate;

import com.SeeAndYouGo.SeeAndYouGo.dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishRepository;
import com.SeeAndYouGo.SeeAndYouGo.menu.Dept;
import com.SeeAndYouGo.SeeAndYouGo.menu.Menu;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.dto.RestaurantDetailRateResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.dto.RestaurantRateMenuResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.dto.RestaurantTotalRateResponseDto;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RateService {

    private static final String RESTAURANT1_MENU_JSON_PATH = "src/main/java/com/SeeAndYouGo/SeeAndYouGo/restaurant/menuOfRestaurant1.json";

    private Map<String, List<String>> restaurant1MenuByCategory = new HashMap<>(); // 카테고리별로 메뉴의 이름이 들어있음.
    private Map<String, Integer> restaurant1MenuByPrice = new HashMap<>(); // 메뉴와 가격이 매칭되어있음.
    private final RateRepository rateRepository;
    private final DishRepository dishRepository;

    @Transactional
    public void saveRate(){
        List<Restaurant1MenuItem> menuItems = parseRestaurant1MenuJson();
        for (Restaurant1MenuItem item : menuItems) {
            if (!rateRepository.existsByDept(item.name())) {
                Rate rate = Rate.builder()
                        .restaurant(Restaurant.제1학생회관)
                        .dept(item.name())
                        .build();
                rateRepository.save(rate);
            }
        }
    }

    public void setRestaurant1MenuField() {
        List<Restaurant1MenuItem> menuItems = parseRestaurant1MenuJson();
        for (Restaurant1MenuItem item : menuItems) {
            restaurant1MenuByCategory.computeIfAbsent(item.dept().toString(), k -> new ArrayList<>())
                    .add(item.name());
            restaurant1MenuByPrice.put(item.name(), item.price());
        }
    }

    private List<Restaurant1MenuItem> parseRestaurant1MenuJson() {
        List<Restaurant1MenuItem> menuItems = new ArrayList<>();
        try {
            String jsonContent = new String(Files.readAllBytes(Paths.get(RESTAURANT1_MENU_JSON_PATH).toAbsolutePath()));
            JsonObject jsonData = JsonParser.parseString(jsonContent).getAsJsonObject();
            JsonArray menuNameArray = jsonData.getAsJsonArray("menuName");

            for (JsonElement menuJson : menuNameArray) {
                JsonObject menuObject = menuJson.getAsJsonObject();
                String name = menuObject.get("name").getAsString();
                Dept dept = Dept.valueOf(menuObject.get("dept").getAsString());
                Integer price = menuObject.get("price").getAsInt();
                menuItems.add(new Restaurant1MenuItem(name, dept, price));
            }
        } catch (IOException e) {
            log.error("Failed to parse Restaurant 1 menu JSON file", e);
            throw new RuntimeException("Failed to parse Restaurant 1 menu JSON", e);
        }
        return menuItems;
    }

    private static class Restaurant1MenuItem {
        private final String name;
        private final Dept dept;
        private final Integer price;

        Restaurant1MenuItem(String name, Dept dept, Integer price) {
            this.name = name;
            this.dept = dept;
            this.price = price;
        }

        String name() { return name; }
        Dept dept() { return dept; }
        Integer price() { return price; }
    }

    public RestaurantTotalRateResponseDto getTotalRestaurantRate(String restaurantName) {
        Restaurant restaurant = Restaurant.valueOf(restaurantName);
        List<Rate> ratesByRestaurant = rateRepository.findAllByRestaurant(restaurant);

        return new RestaurantTotalRateResponseDto(ratesByRestaurant);
//        현재는 1학 리뷰의 전체를 하므로 아래의 코드는 쓰지 않는다. 아래의 코드는 당일 1학에 대한 평점을 반환하는 코드이다.
//        Restaurant restaurant = restaurantRepository.findByNameAndDate(restaurantName, LocalDate.now().toString()).get(0);
//        return RestaurantTotalRateResponseDto.builder()
//                .totalAvgRate(restaurant.getRestaurantRate())
//                .build();
    }

    /**
     * 현재는 1학만 세부 평점을 제공하므로 parameter인 restaurantName은 필요가 없다.
     */
    public List<RestaurantDetailRateResponseDto> getDetailRestaurantRate(String restaurantName) {
        Restaurant restaurant = Restaurant.valueOf(restaurantName);

        if(!restaurant.equals(Restaurant.제1학생회관)){
            // 1학생활관이 아니라면 아직 기능을 제공 안함.
            throw new IllegalArgumentException("1학을 제외하고는 지원하지 않는 메서드입니다.");
        }

        // 1학의 개인 메뉴의 평점을 가져오는 코드를 작성하기
        List<Rate> ratesByRestaurant = rateRepository.findAllByRestaurant(restaurant);
        List<RestaurantDetailRateResponseDto> detailRate = new ArrayList<>();

        for (String deptToString : restaurant1MenuByCategory.keySet()) {
            List<String> dishNames = restaurant1MenuByCategory.get(deptToString);
            List<RestaurantRateMenuResponseDto> dishRate = new ArrayList<>();
            for (String dishName : dishNames) {
                Rate rate = rateRepository.findByDept(dishName).get(0);

                RestaurantRateMenuResponseDto rateDto = RestaurantRateMenuResponseDto.builder()
                        .menuName(dishName)
                        .price(restaurant1MenuByPrice.get(dishName))
                        .averageRate(rate.getRate())
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
     * 1학에서제공되는 평점을 뽑아온다.
     */
    private List<RestaurantDetailRateResponseDto> getDetailRestaurant1Rate() {
        List<RestaurantDetailRateResponseDto> detailRate = new ArrayList<>();
        for (String deptToString : restaurant1MenuByCategory.keySet()) {
            List<String> dishNames = restaurant1MenuByCategory.get(deptToString);
            List<RestaurantRateMenuResponseDto> dishRate = new ArrayList<>();
            for (String dishName : dishNames) {
                Dish dish = dishRepository.findByName(dishName)
                        .orElseThrow(() ->
                                new IllegalArgumentException(dishName+"의 Dish를 찾을 수 없습니다."));
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
     * 1학이라면 해당 메뉴를 찾아서 해당 메뉴에도 등록해줘야한다.
     */
    @Transactional
    public void updateRateByRestaurant(Restaurant restaurant, Menu menu, Double rate) {
        Dept dept = menu.getDept();
        Rate rateByRestaurant = rateRepository.findByRestaurantAndDept(restaurant, dept.toString());

        // 1학일 경우, 실제 dept에도 평점을 반영해야하고, 각 메뉴별 데이터에도 평점을 반영해줘야한다.
        // 따라서 개인 메뉴로 한번 더 찾아와서 업데이트해야한다.
        if(menu.getRestaurant().equals(Restaurant.제1학생회관)){
            Rate rateByMenu = rateRepository.findByRestaurantAndDept(restaurant, menu.getMenuName());
            rateByMenu.reflectRate(rate);
        }

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
            List<String> possibleDept = restaurant.getPossibleDept()
                                                    .stream()
                                                    .map(Dept::toString)
                                                    .collect(Collectors.toList());

            if(restaurant.equals(Restaurant.제1학생회관)){
                // 만약 1학생회관이라면 일반 메뉴들도 rate 테이블에 넣어야함.
                // 따라서 아래의 메서드에서 1학의 메뉴들을 추가적으로 넣어줘야함.
                possibleDept.addAll(restaurant1MenuByPrice.keySet());
            }

            for (String dept : possibleDept) {
                Rate rate = Rate.builder()
                        .restaurant(restaurant)
                        .dept(dept)
                        .build();

                rates.add(rate);
            }
        }

        rateRepository.saveAll(rates);
    }
}
