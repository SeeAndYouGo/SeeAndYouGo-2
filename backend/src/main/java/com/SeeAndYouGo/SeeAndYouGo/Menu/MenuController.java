package com.SeeAndYouGo.SeeAndYouGo.Menu;

import com.SeeAndYouGo.SeeAndYouGo.Dish.Dish;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
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
            menuResponse.setRestaurantName(dayRestaurantMenu.getRestaurant().getName());
            menuResponse.setPrice(dayRestaurantMenu.getPrice());
            menuResponse.setDept(dayRestaurantMenu.getDept().toString());
            menuResponse.setMenuType(dayRestaurantMenu.getMenuType().toString());
            menuResponse.setDate(dayRestaurantMenu.getDate().toString());
            List<String> dishesString = new ArrayList<>();
            for (Dish dish : dayRestaurantMenu.getDishList())
                dishesString.add(dish.toString());
            menuResponse.setDishList(dishesString);
            menuResponses.add(menuResponse);
        }
        return menuResponses;
    }

    @GetMapping("/{restaurant}/menu/week/{date}")
    @ResponseBody
    public ResponseEntity<List<MenuResponse>[]> restaurantMenuWeek(@PathVariable("restaurant") String place, @PathVariable("date") String date) {
        List<Menu>[] oneWeekRestaurantMenu = menuService.getOneWeekRestaurantMenu(place, date);
        List<MenuResponse>[] menuListArr = new List[5];

        int idx = 0;
        for (List<Menu> dayRestaurantMenu : oneWeekRestaurantMenu) {
            menuListArr[idx++] = parseOneDayRestaurantMenu(dayRestaurantMenu);
        }
        return ResponseEntity.ok(menuListArr);
    }

//    @GetMapping("/all/menu/week/{date}")
//    @ResponseBody
//    public ResponseEntity<List<MenuResponse>[]> allRestaurantMenuWeek(@PathVariable("date") String date) {
//
//        List<Menu>[] oneWeekRestaurantMenu = menuService.getOneWeekRestaurantMenu(place, date);
//        List<MenuResponse>[] menuListArr = new List[5];
//
//        int idx = 0;
//        for (List<Menu> dayRestaurantMenu : oneWeekRestaurantMenu) {
//            menuListArr[idx++] = parseOneDayRestaurantMenu(dayRestaurantMenu);
//        }
//        return ResponseEntity.ok(menuListArr);
//    }
}
