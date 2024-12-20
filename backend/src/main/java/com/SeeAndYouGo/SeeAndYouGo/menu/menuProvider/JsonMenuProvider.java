package com.SeeAndYouGo.SeeAndYouGo.menu.menuProvider;

import com.SeeAndYouGo.SeeAndYouGo.dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishRepository;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishType;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishVO;
import com.SeeAndYouGo.SeeAndYouGo.menu.Dept;
import com.SeeAndYouGo.SeeAndYouGo.menu.Menu;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuRepository;
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
import java.util.List;

@Component
@RequiredArgsConstructor
public class JsonMenuProvider implements MenuProvider{

    @Override
    public List<MenuVO> getWeeklyMenu(Restaurant restaurant, LocalDate monday, LocalDate sunday) throws Exception {
            // Read the JSON file
            String jsonContent = new String(Files.readAllBytes(Paths.get("src/main/java/com/SeeAndYouGo/SeeAndYouGo/Restaurant/menuOfRestaurant1.json").toAbsolutePath()));

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

                for(LocalDate date=monday; !date.isAfter(sunday); date = date.plusDays(1)) {
                    MenuVO menuVO = getMenuVO(price, date, dept, MenuType.LUNCH, restaurant);

                    menuVO.addDishVO(dishVO);
                    menuVOs.add(menuVO);
                }
            }

        return menuVOs;
    }

    @Override
    public String getWeeklyMenuToString(LocalDate monday, LocalDate sunday) throws Exception {
        throw new IllegalArgumentException(this.getClass().toString() + "의 getWeeklyMenuToString은 호출이 금지되어 있습니다.");
    }

    private MenuVO getMenuVO(Integer price, LocalDate date, Dept dept, MenuType menuType, Restaurant restaurant) {
        return new MenuVO(price, date.toString(), dept, restaurant, menuType);
    }
}
