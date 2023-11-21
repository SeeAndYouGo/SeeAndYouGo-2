package com.SeeAndYouGo.SeeAndYouGo.Dish;

import com.SeeAndYouGo.SeeAndYouGo.Menu.*;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.RestaurantService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DishService {
    private final DishRepository dishRepository;
    private final MenuService menuService;
    private final RestaurantService restaurantService;
    private final Integer PAGE_START = 1;
    private final Integer PAGE_END = 3;

    @Transactional
    public void saveAndCacheWeekDish() throws Exception{
        String wifiInfo = fetchDishInfoToString();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate friday  = LocalDate.now().with(DayOfWeek.SUNDAY);

        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(wifiInfo).getAsJsonObject();

        JsonArray resultArray = jsonObject.getAsJsonArray("RESULT");

        List<Dish> dishes = new ArrayList<>();

        // "OutBlock" 배열 순회
        for (JsonElement element : resultArray) {
            JsonObject menuObject = element.getAsJsonObject();

            // 필드 값 추출
            String restaurantName = menuObject.get("CAFE_DIV_NM").getAsString();
            restaurantName = menuService.parseRestaurantName(restaurantName);

            String deptStr = menuObject.get("CAFE_DTL_DIV_NM").getAsString();
            Dept dept = Dept.changeStringToDept(deptStr);

            String menuTypeStr = menuObject.get("FOOM_DIV_NM").getAsString();
            MenuType menuType = MenuType.changeStringToMenuType(menuTypeStr);

            String menuName = menuObject.get("MENU_KORN_NM").getAsString();
            if(menuName.contains("매주 수요일은")) continue;

            int price = 0;
            String priceStr = menuObject.get("MENU_PRC").getAsString();
            if(priceStr.length()!=0)
                price = Integer.parseInt(priceStr);

            String dateStr = menuObject.get("FOOM_YMD").getAsString();
            LocalDate objDate = LocalDate.parse(dateStr, formatter);

            if(objDate.isAfter(monday) && objDate.isBefore(friday) || objDate.isEqual(monday) || objDate.isEqual(friday)) {
                // 식당, 날짜, DEPT, 메뉴타입을 기준으로 해당하는 식당을 찾는다.
                // Dish를 생성한다.
                Restaurant restaurant = restaurantService.getRestaurant(restaurantName, objDate.toString());
                Dish dish = new Dish(menuName, dept, objDate.toString(), DishType.SIDE, restaurant, menuType, price);
                dishes.add(dish);
            }else continue;
        }
        // 오늘 날짜의 Dish를 만들었으면, 이걸 기준으로 Menu를 만든다.
        List<Menu> menus = menuService.createMenuWithDishs(dishes);
        for (Dish dish : dishes) {
            for (Menu menu : menus) {
                if(menu.getDishList().contains(dish)){
                    dish.setMenu(menu);
                }
            }
        }
        dishRepository.saveAll(dishes);
    }

    @Transactional
    public void saveAndCacheTodayDish(LocalDate today) throws Exception{
        // 가장 먼저 해당 식당에 메뉴가 있는지 확인을 해봐야한다. 만약 있다면 갱신해야함.
        // Dish에 있다면 해당 Dish를 삭제하고, 다시 만들자.
        // 이 때, Restaurant은 만드는 것이 아닌 가져오는 것임.

        // 오늘 메뉴가 DB에 저장되어 있는지 확인
        if(restaurantService.checkRestaurantInDate(today.toString())){
            restaurantService.deleteRestaurants(today.toString());
        }


        String wifiInfo = fetchDishInfoToString();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(wifiInfo).getAsJsonObject();
        JsonArray resultArray = jsonObject.getAsJsonArray("RESULT");

        List<Dish> dishes = new ArrayList<>();

        // "OutBlock" 배열 순회
        for (JsonElement element : resultArray) {
            JsonObject menuObject = element.getAsJsonObject();

            // 필드 값 추출
            String restaurantName = menuObject.get("CAFE_DIV_NM").getAsString();
            restaurantName = menuService.parseRestaurantName(restaurantName);

            String deptStr = menuObject.get("CAFE_DTL_DIV_NM").getAsString();
            Dept dept = Dept.changeStringToDept(deptStr);

            String menuTypeStr = menuObject.get("FOOM_DIV_NM").getAsString();
            MenuType menuType = MenuType.changeStringToMenuType(menuTypeStr);

            String menuName = menuObject.get("MENU_KORN_NM").getAsString();
            if(menuName.contains("매주 수요일은")) continue;

            int price = 0;
            String priceStr = menuObject.get("MENU_PRC").getAsString();
            if(priceStr.length()!=0) price = Integer.parseInt(priceStr);

            String dateStr = menuObject.get("FOOM_YMD").getAsString();
            LocalDate objDate = LocalDate.parse(dateStr, formatter);

            if(objDate.isEqual(today)) {
                // 식당, 날짜, DEPT, 메뉴타입을 기준으로 해당하는 식당을 찾는다.
                // Dish를 생성한다.
                // 여기서는 무조건 레스토랑이 생성되지 않고 찾아와짐.
                Restaurant restaurant = restaurantService.getRestaurant(restaurantName, objDate.toString());
                Dish dish = new Dish(menuName, dept, objDate.toString(), DishType.SIDE, restaurant, menuType, price);
                dishes.add(dish);
            }else continue;
        }
        // 오늘 날짜의 Dish를 만들었으면, 이걸 기준으로 Menu를 만든다.
        List<Menu> menus = menuService.createMenuWithDishs(dishes);
        for (Dish dish : dishes) {
            for (Menu menu : menus) {
                if(menu.getDishList().contains(dish)){
                    dish.setMenu(menu);
                }
            }
        }
        dishRepository.saveAll(dishes);
    }



    private String fetchDishInfoToString() throws Exception {
        StringBuilder rawMenu = new StringBuilder();

        for(int page = PAGE_START; page <= PAGE_END; page++) {
            String apiUrl = "https://api.cnu.ac.kr/svc/offcam/pub/FoodInfo?page=" + page + "&AUTH_KEY=3CF4EB2CDE7F4593AD96C3EBEC8218AFCF9F15F1";

            // URL 생성
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // 응답 코드 확인
            int responseCode = connection.getResponseCode();
            String responseData = new String();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                StringBuilder response = new StringBuilder();

                while ((line = reader.readLine()) != null) response.append(line);

                reader.close();
                rawMenu.append(response);
            }
        }

        return rawMenu.toString();
    }


    @Transactional
    public void updateMainDish(List<MainDishResponse> mainDishResponses) {
        for (MainDishResponse mainDishResponse : mainDishResponses) {
            String mainDishName = mainDishResponse.getMainDishName();
            String date = mainDishResponse.getDate();
            Dept dept = Dept.valueOf(mainDishResponse.getDept());
            String restaurantName = mainDishResponse.getRestaurantName();

            Dish dish = dishRepository.findByDishIdentifier(restaurantName, mainDishName, dept, date);
            dish.setDishType(DishType.MAIN);
            dishRepository.save(dish);
        }
    }
}
