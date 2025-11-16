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
    public void updateMenuMap(Restaurant restaurant, LocalDate monday, LocalDate sunday) throws IOException {
        // Read the JSON file
        String jsonContent = new String(Files.readAllBytes(Paths.get("src/main/java/com/SeeAndYouGo/SeeAndYouGo/restaurant/menuOfRestaurant1.json").toAbsolutePath()));

        // Parse the JSON data
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonData = jsonParser.parse(jsonContent).getAsJsonObject();

        List<MenuVO> menuVOs = new ArrayList<>();

        // Extract menuName
        JsonArray menuNameArray = jsonData.getAsJsonArray("menuName");
        for (JsonElement menuJson : menuNameArray) {
            String name = menuJson.getAsJsonObject().get("name").toString().replace("\"", "");
            Dept dept = Dept.valueOf(menuJson.getAsJsonObject().get("dept").toString().replace("\"", ""));
            Integer price = Integer.parseInt(menuJson.getAsJsonObject().get("price").toString());

            DishVO dishVO = new DishVO(name, DishType.MAIN);

            // 1학은 주말에 운영하지 않으므로 메뉴가 들어가면 안된다.
            LocalDate friday = sunday.minusDays(2);

            for(LocalDate date=monday; !date.isAfter(friday); date = date.plusDays(1)) {
                MenuVO menuVO = getMenuVO(price, date, dept, MenuType.LUNCH, restaurant);

                menuVO.addDishVO(dishVO);
                menuVOs.add(menuVO);
            }
        }

        menuMap.put(restaurant, menuVOs);
    }

    private MenuVO getMenuVO(Integer price, LocalDate date, Dept dept, MenuType menuType, Restaurant restaurant) {
        return new MenuVO(price, date.toString(), dept, restaurant, menuType);
    }
}
