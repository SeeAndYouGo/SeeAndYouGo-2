package com.SeeAndYouGo.SeeAndYouGo.Menu;

import com.SeeAndYouGo.SeeAndYouGo.Dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.Keyword.UserKeyword;
import com.SeeAndYouGo.SeeAndYouGo.Keyword.UserKeywordRepository;
import com.SeeAndYouGo.SeeAndYouGo.OAuth.jwt.TokenProvider;
import com.SeeAndYouGo.SeeAndYouGo.user.User;
import com.SeeAndYouGo.SeeAndYouGo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class MenuController {
    private final MenuService menuService;
    private final UserRepository userRepository;

    private final UserKeywordRepository userKeywordRepository;

    private final TokenProvider tokenProvider;

    @GetMapping(value = {"/daily-menu/{restaurant}/{user_id}", "/daily-menu/{restaurant}"})
    public ResponseEntity<List<MenuResponseByUserDto>> restaurantMenuDayByUser(@PathVariable("restaurant") String place,
                                                                               @PathVariable(value = "user_id", required = false) String tokenId) {
        String date = getTodayDate();
        List<Menu> oneDayRestaurantMenu = menuService.getOneDayRestaurantMenu(place, date);

        List<String> keyStrings = new ArrayList<>();
        if (tokenId != null) {
            String email = tokenProvider.decodeToEmail(tokenId);
            User user = userRepository.findByEmail(email).get(0);
            List<UserKeyword> keywords = userKeywordRepository.findByUser(user);
            keyStrings = keywords.stream().map(x -> x.getKeyword().getName()).collect(Collectors.toList());
        }

        return ResponseEntity.ok(parseOneDayRestaurantMenuByUser(oneDayRestaurantMenu, keyStrings));
    }

    public static String getTodayDate() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
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

    private List<MenuResponseByUserDto> parseOneDayRestaurantMenuByUser(List<Menu> oneDayRestaurantMenu, List<String> keywords) {
        List<MenuResponseByUserDto> dtos = new ArrayList<>();
        for (Menu dayRestaurantMenu : oneDayRestaurantMenu) {
            List<Dish> dishList = dayRestaurantMenu.getDishList();
            List<String> keyStrings = new ArrayList<>();
            for(Dish dish : dishList){
                for(String key : keywords){
                    if (dish.getName().contains(key)){
                        keyStrings.add(dish.getName());
                    }
                }
            }
            MenuResponseByUserDto dto = new MenuResponseByUserDto(dayRestaurantMenu, keyStrings);
            dtos.add(dto);
        }
        return dtos;
    }

    @GetMapping("/weekly-menu/{restaurant}")
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

    @GetMapping("/weekly-menu")
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
