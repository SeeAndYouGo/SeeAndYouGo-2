package com.SeeAndYouGo.SeeAndYouGo.Menu;

import com.SeeAndYouGo.SeeAndYouGo.Dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.Review.NCloudObjectStorage;
import com.SeeAndYouGo.SeeAndYouGo.Review.Review;
import com.SeeAndYouGo.SeeAndYouGo.Review.ReviewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
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
