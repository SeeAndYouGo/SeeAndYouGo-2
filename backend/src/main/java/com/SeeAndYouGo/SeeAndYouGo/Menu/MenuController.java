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
    public ResponseEntity<List<MenuResponseDto>> restaurantMenuDay(@PathVariable("restaurant") String place) {
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

    private List<MenuResponseDto> parseOneDayRestaurantMenu(List<Menu> oneDayRestaurantMenu) {
        List<MenuResponseDto> menuResponsDtos = new ArrayList<>();
        for (Menu dayRestaurantMenu : oneDayRestaurantMenu) {
            MenuResponseDto menuResponseDto = new MenuResponseDto(dayRestaurantMenu);
            menuResponsDtos.add(menuResponseDto);
        }
        return menuResponsDtos;
    }

    @GetMapping("/weeklyMenu/{restaurant}")
    public ResponseEntity<List<MenuResponseDto>> restaurantMenuWeek(@PathVariable("restaurant") String place) {
        String date = getTodayDate();
        List<Menu>[] oneWeekRestaurantMenu = menuService.getOneWeekRestaurantMenu(place, date);
        List<MenuResponseDto> menuListArr = new ArrayList<>();

        for (List<Menu> dayRestaurantMenu : oneWeekRestaurantMenu) {
            List<MenuResponseDto> menuResponsDtos = parseOneDayRestaurantMenu(dayRestaurantMenu);
            for (MenuResponseDto menuResponseDto : menuResponsDtos) {
                menuListArr.add(menuResponseDto);
            }
        }
        return ResponseEntity.ok(menuListArr);
    }

    @GetMapping("/weeklyMenu")
    public ResponseEntity<List<MenuResponseDto>> allRestaurantMenuWeek() {
        String date = getTodayDate();
        List<MenuResponseDto> menuListArr = new ArrayList<>();
        List<Menu>[] oneWeekRestaurantMenu;
        String place;

        for(int i=2; i<=5; i++) {
            place = "restaurant"+i;
            oneWeekRestaurantMenu = menuService.getOneWeekRestaurantMenu(place, date);

            for (List<Menu> dayRestaurantMenu : oneWeekRestaurantMenu) {
                List<MenuResponseDto> menuResponsDtos = parseOneDayRestaurantMenu(dayRestaurantMenu);
                for (MenuResponseDto menuResponseDto : menuResponsDtos) {
                    menuListArr.add(menuResponseDto);
                }
            }
        }
        return ResponseEntity.ok(menuListArr);
    }
}
