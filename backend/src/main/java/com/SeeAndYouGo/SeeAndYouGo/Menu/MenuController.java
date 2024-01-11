package com.SeeAndYouGo.SeeAndYouGo.Menu;

import com.SeeAndYouGo.SeeAndYouGo.Dish.Dish;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class MenuController {
    private final MenuService menuService;

    @GetMapping("/dailyMenu/{restaurant}")
    public ResponseEntity<List<MenuResponse>> restaurantMenuDay(@PathVariable("restaurant") String place) {
        String date = getTodayDate();
        List<Menu> oneDayRestaurantMenu = menuService.getOneDayRestaurantMenu(place, date);

        return ResponseEntity.ok(parseOneDayRestaurantMenu(oneDayRestaurantMenu));
    }

    private String getTodayDate() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        if(currentDate.getDayOfWeek().equals(DayOfWeek.SATURDAY)){
            currentDate = currentDate.plusDays(2);
        }else if(currentDate.getDayOfWeek().equals(DayOfWeek.SUNDAY)){
            currentDate = currentDate.plusDays(1);
        }
        return currentDate.format(formatter);
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

    @GetMapping("/weeklyMenu/{restaurant}")
    @ResponseBody
    public ResponseEntity<List<MenuResponse>> restaurantMenuWeek(@PathVariable("restaurant") String place) {
        String date = getTodayDate();
        List<Menu>[] oneWeekRestaurantMenu = menuService.getOneWeekRestaurantMenu(place, date);
        List<MenuResponse> menuListArr = new ArrayList<>();

        for (List<Menu> dayRestaurantMenu : oneWeekRestaurantMenu) {
            List<MenuResponse> menuResponses = parseOneDayRestaurantMenu(dayRestaurantMenu);
            for (MenuResponse menuResponse : menuResponses) {
                menuListArr.add(menuResponse);
            }
        }
        return ResponseEntity.ok(menuListArr);
    }

    @GetMapping("/weeklyMenu")
    @ResponseBody
    public ResponseEntity<List<MenuResponse>> allRestaurantMenuWeek() {
        String date = getTodayDate();
        List<MenuResponse> menuListArr = new ArrayList<>();
        List<Menu>[] oneWeekRestaurantMenu = new List[4];
        String place;

        for(int i=2; i<=5; i++) {
            place = "restaurant"+i;
            oneWeekRestaurantMenu = menuService.getOneWeekRestaurantMenu(place, date);

            for (List<Menu> dayRestaurantMenu : oneWeekRestaurantMenu) {
                List<MenuResponse> menuResponses = parseOneDayRestaurantMenu(dayRestaurantMenu);
                for (MenuResponse menuResponse : menuResponses) {
                    menuListArr.add(menuResponse);
                }
            }
        }
        return ResponseEntity.ok(menuListArr);
    }
}
