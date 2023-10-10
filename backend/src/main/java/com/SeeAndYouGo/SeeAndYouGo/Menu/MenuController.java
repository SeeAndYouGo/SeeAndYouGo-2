package com.SeeAndYouGo.SeeAndYouGo.Menu;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;

    @GetMapping("/{restaurant}/menu/day/{date}")
    public ResponseEntity<List<MenuResponse>> restaurantMenuDay(
            @PathVariable("restaurant") String place, @PathVariable("date") String date) {
        List<Menu> oneDayRestaurantMenu = menuService.getOneDayRestaurantMenu(place, date);

        return ResponseEntity.ok(parseOneDayRestaurantMenu(oneDayRestaurantMenu));
    }

    private List<MenuResponse> parseOneDayRestaurantMenu(List<Menu> oneDayRestaurantMenu) {
        List<MenuResponse> menuResponses = new ArrayList<>();
        for (Menu dayRestaurantMenu : oneDayRestaurantMenu) {
            MenuResponse menuResponse = new MenuResponse();
            menuResponse.setDishList(dayRestaurantMenu.getDishList());
            menuResponse.setPrice(dayRestaurantMenu.getPrice());
            menuResponse.setDept(dayRestaurantMenu.getDept());

            menuResponses.add(menuResponse);
        }
        return menuResponses;
    }

    @GetMapping("/{restaurant}/menu/week/{date}")
    public ResponseEntity<List<MenuResponse>[]> restaurantMenuWeek(
            @PathVariable("restaurant") String place, @PathVariable("date") String date) {
        List<Menu>[] oneWeekRestaurantMenu = menuService.getOneWeekRestaurantMenu(place, date);

        List<MenuResponse>[] menuListArr = new List[5];

        int idx = 0;
        for (List<Menu> dayRestaurantMenu : oneWeekRestaurantMenu) {
            menuListArr[idx++] = parseOneDayRestaurantMenu(dayRestaurantMenu);
        }
        return ResponseEntity.ok(menuListArr);
    }
}
