package com.SeeAndYouGo.SeeAndYouGo.menu.menuProvider;

import com.SeeAndYouGo.SeeAndYouGo.dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishRepository;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishType;
import com.SeeAndYouGo.SeeAndYouGo.menu.Dept;
import com.SeeAndYouGo.SeeAndYouGo.menu.Menu;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuRepository;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuType;
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

    private final DishRepository dishRepository;
    private final MenuRepository menuRepository;

    @Override
    public List<Menu> getWeeklyMenu(Restaurant restaurant, LocalDate monday, LocalDate sunday) throws Exception {
            // Read the JSON file
            String jsonContent = new String(Files.readAllBytes(Paths.get("src/main/java/com/SeeAndYouGo/SeeAndYouGo/Restaurant/menuOfRestaurant1.json").toAbsolutePath()));

            // Parse the JSON data
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonData = jsonParser.parse(jsonContent).getAsJsonObject();

            List<Menu> menus = new ArrayList<>();

            // Extract menuName
            JsonArray menuNameArray = jsonData.getAsJsonArray("menuName");
            for (JsonElement menuJson : menuNameArray) {
                String name = menuJson.getAsJsonObject().get("name").toString().replace("\"", "");
                Dept dept = Dept.valueOf(menuJson.getAsJsonObject().get("dept").toString().replace("\"", ""));
                Integer price = Integer.parseInt(menuJson.getAsJsonObject().get("price").toString());

                if (!dishRepository.existsByName(name)) {
                    dishRepository.save(Dish.builder()
                            .name(name)
                            .dishType(DishType.MAIN)
                            .build());
                }

                Dish dish = dishRepository.findByName(name);

                for(LocalDate date=monday; !date.isAfter(sunday); date = date.plusDays(1)) {
                    Menu menu = Menu.builder()
                            .price(price)
                            .date(date.toString())
                            .dept(dept)
                            .menuType(MenuType.LUNCH) // 1학은 고정적으로 LUNCH
                            .restaurant(restaurant)
                            .build();

                    menu.setDishList(List.of(dish));
                    menus.add(menu);
                }
            }
            menuRepository.saveAll(menus);

        return menus;
    }
}
