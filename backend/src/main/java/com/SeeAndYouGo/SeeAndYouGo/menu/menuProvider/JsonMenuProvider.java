package com.SeeAndYouGo.SeeAndYouGo.menu.menuProvider;

import com.SeeAndYouGo.SeeAndYouGo.dish.DishType;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishVO;
import com.SeeAndYouGo.SeeAndYouGo.menu.Dept;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuType;
import com.SeeAndYouGo.SeeAndYouGo.menu.dto.MenuVO;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JsonMenuProvider implements MenuProvider{

    private Map<Restaurant, List<MenuVO>> menuMap = new HashMap<>();

    @Override
    public List<MenuVO> getWeeklyMenu(Restaurant restaurant) throws Exception {
        return menuMap.get(restaurant);
    }

    @Override
    public List<MenuVO> getWeeklyMenuMap(Restaurant restaurant) throws Exception {
        return menuMap.get(restaurant);
    }

    @Override
    public void updateDailyMenu(Restaurant restaurant, LocalDate date) throws IOException {
        // Read the JSON file
        String jsonContent = new String(Files.readAllBytes(Paths.get("src/main/java/com/SeeAndYouGo/SeeAndYouGo/restaurant/menuOfRestaurant1.json").toAbsolutePath()));

        // Parse the JSON data
        JsonParser jsonParser = new JsonParser();
        JsonArray deptArray = jsonParser.parse(jsonContent).getAsJsonArray();

        List<MenuVO> dailyMenu = new ArrayList<>();

        // Extract menus from each dept
        for (JsonElement deptElement : deptArray) {
            JsonObject deptObj = deptElement.getAsJsonObject();
            Dept dept = Dept.valueOf(deptObj.get("deptEn").getAsString());
            JsonArray menusArray = deptObj.getAsJsonArray("menus");

            for (JsonElement menuJson : menusArray) {
                String name = menuJson.getAsJsonObject().get("name").getAsString();
                Integer price = menuJson.getAsJsonObject().get("price").getAsInt();

                DishVO dishVO = new DishVO(name, DishType.MAIN);

                // Only create menu for the given date
                MenuVO menuVO = getMenuVO(price, date, dept, MenuType.LUNCH, restaurant);
                menuVO.addDishVO(dishVO);
                dailyMenu.add(menuVO);
            }
        }

        // Update the menuMap for the specific day
        List<MenuVO> weeklyMenu = menuMap.get(restaurant);
        if (weeklyMenu != null) {
            weeklyMenu.removeIf(menuVO -> menuVO.getDate().equals(date.toString()));
            weeklyMenu.addAll(dailyMenu);
            menuMap.put(restaurant, weeklyMenu);
        }
    }

    @Override
    public void updateMenuMap(Restaurant restaurant, LocalDate monday, LocalDate sunday) throws IOException {
        // Read the JSON file
        String jsonContent = new String(Files.readAllBytes(Paths.get("src/main/java/com/SeeAndYouGo/SeeAndYouGo/restaurant/menuOfRestaurant1.json").toAbsolutePath()));

        // Parse the JSON data
        JsonParser jsonParser = new JsonParser();
        JsonArray deptArray = jsonParser.parse(jsonContent).getAsJsonArray();

        List<MenuVO> menuVOs = new ArrayList<>();

        // Extract menus from each dept
        for (JsonElement deptElement : deptArray) {
            JsonObject deptObj = deptElement.getAsJsonObject();
            Dept dept = Dept.valueOf(deptObj.get("deptEn").getAsString());
            JsonArray menusArray = deptObj.getAsJsonArray("menus");

            for (JsonElement menuJson : menusArray) {
                String name = menuJson.getAsJsonObject().get("name").getAsString();
                Integer price = menuJson.getAsJsonObject().get("price").getAsInt();

                DishVO dishVO = new DishVO(name, DishType.MAIN);

                // 1학은 주말에 운영하지 않으므로 메뉴가 들어가면 안된다.
                LocalDate friday = sunday.minusDays(2);

                for (LocalDate date = monday; !date.isAfter(friday); date = date.plusDays(1)) {
                    MenuVO menuVO = getMenuVO(price, date, dept, MenuType.LUNCH, restaurant);

                    menuVO.addDishVO(dishVO);
                    menuVOs.add(menuVO);
                }
            }
        }

        menuMap.put(restaurant, menuVOs);
    }

    private MenuVO getMenuVO(Integer price, LocalDate date, Dept dept, MenuType menuType, Restaurant restaurant) {
        return new MenuVO(price, date.toString(), dept, restaurant, menuType);
    }
}
