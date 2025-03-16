package com.SeeAndYouGo.SeeAndYouGo.menu;

import com.SeeAndYouGo.SeeAndYouGo.aop.log.TraceMethodLog;
import com.SeeAndYouGo.SeeAndYouGo.menu.dto.*;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import com.SeeAndYouGo.SeeAndYouGo.user.User;
import com.SeeAndYouGo.SeeAndYouGo.user.UserRepository;
import com.SeeAndYouGo.SeeAndYouGo.userKeyword.UserKeyword;
import com.SeeAndYouGo.SeeAndYouGo.userKeyword.UserKeywordRepository;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.SeeAndYouGo.SeeAndYouGo.IterService.getNearestMonday;
import static com.SeeAndYouGo.SeeAndYouGo.IterService.getSundayOfWeek;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class MenuController {
    private final MenuService menuService;
    private final UserRepository userRepository;

    private final UserKeywordRepository userKeywordRepository;

    @GetMapping("/daily-menu/{restaurant}")
    public List<MenuResponseByUserDto> restaurantMenuDayByUser(@PathVariable("restaurant") String place,
                                                               @Parameter(hidden = true) @AuthenticationPrincipal String email) {
        String date = getTodayDate();
        List<Menu> oneDayRestaurantMenu = menuService.getOneDayRestaurantMenu(place, date);  // 메인메뉴가 변하지 않았다면 캐싱해오고 있음

        List<String> keyStrings = new ArrayList<>();
        if (email.equals("none")) {
            User user = userRepository.findByEmail(email);
            List<UserKeyword> keywords = userKeywordRepository.findByUser(user);
            keyStrings = keywords.stream().map(x -> x.getKeyword().getName()).collect(Collectors.toList());
        }

        return parseOneDayRestaurantMenuByUser(oneDayRestaurantMenu, keyStrings);
    }

    public static String getTodayDate() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return currentDate.format(formatter);
    }

    private List<MenuResponseDto> parseOneDayRestaurantMenu(List<Menu> oneDayRestaurantMenu) {
        List<MenuResponseDto> menuResponseDtos = new ArrayList<>();
        for (Menu dayRestaurantMenu : oneDayRestaurantMenu) {
            MenuResponseDto menuResponseDto = new MenuResponseDto(dayRestaurantMenu);
            menuResponseDtos.add(menuResponseDto);
        }
        return menuResponseDtos;
    }

    private List<MenuResponseByAdminDto> parseOneDayRestaurantMenuForAdmin(List<Menu> oneDayRestaurantMenu) {
        List<MenuResponseByAdminDto> menuResponseDtos = new ArrayList<>();
        for (Menu dayRestaurantMenu : oneDayRestaurantMenu) {
            MenuResponseByAdminDto menuResponseDto = new MenuResponseByAdminDto(dayRestaurantMenu);
            menuResponseDtos.add(menuResponseDto);
        }
        return menuResponseDtos;
    }

    /**
     * 유저의 keywords에 해당되는 menu가 있다면 해당 menu를 추가.
     */
    private List<MenuResponseByUserDto> parseOneDayRestaurantMenuByUser(List<Menu> oneDayRestaurantMenu, List<String> keywords) {
        List<MenuResponseByUserDto> menuResponseDtos = new ArrayList<>();
        for (Menu menu : oneDayRestaurantMenu) {

            MenuResponseByUserDto dto = MenuResponseByUserDto.builder()
                    .menuId(menu.getId())
                    .menuType(menu.getMenuType().toString())
                    .price(menu.getPrice())
                    .dept(menu.getDept().toString())
                    .sideDishList(menu.getSideDishToString())
                    .mainDishList(menu.getMainDishToString())
                    .restaurantName(menu.getRestaurant().toString())
                    .date(menu.getDate())
                    .keywordList(keywords)
                    .build();
            menuResponseDtos.add(dto);
        }
        return menuResponseDtos;
    }

    /**
     * dishes와 keywords 중에 겹치는 것만 return한다.
     */
    private List<String> findOverlappingThing(List<String> dishes, List<String> keywords) {
        return dishes.stream()
                .filter(
                        dish -> keywords.stream()
                                .anyMatch(dish::contains)
                ).collect(Collectors.toList());
    }

    @GetMapping("/weekly-menu/{restaurant}")
    public List<MenuResponseDto> restaurantMenuWeek(@PathVariable("restaurant") String place) {
        String date = getTodayDate();
        List<Menu>[] oneWeekRestaurantMenu = menuService.getOneWeekRestaurantMenu(place, date);
        List<MenuResponseDto> menuListArr = new ArrayList<>();

        for (List<Menu> dayRestaurantMenu : oneWeekRestaurantMenu) {
            List<MenuResponseDto> menuResponseDtos = parseOneDayRestaurantMenu(dayRestaurantMenu);
            menuListArr.addAll(menuResponseDtos);
        }
        return menuListArr;
    }

    @GetMapping("/weekly-menu")
    public List<MenuResponseByAdminDto> allRestaurantMenuWeekForAdmin() {
        String date = getTodayDate();
        List<MenuResponseByAdminDto> menuListArr = new ArrayList<>();
        List<Menu>[] oneWeekRestaurantMenu;

        for (Restaurant restaurant : Restaurant.values()) {
            oneWeekRestaurantMenu = menuService.getOneWeekRestaurantMenu(restaurant.toString(), date);

            for (List<Menu> dayRestaurantMenu : oneWeekRestaurantMenu) {
                List<MenuResponseByAdminDto> menuResponsDtos = parseOneDayRestaurantMenuForAdmin(dayRestaurantMenu);
                menuListArr.addAll(menuResponsDtos);
            }
        }

        return menuListArr;
    }

    @TraceMethodLog
    @GetMapping("/test/{restaurantName}/{date}")
    public MenuPostDto test(@PathVariable String restaurantName, @PathVariable String date){
        String parseRestaurantName = Restaurant.parseName(restaurantName);
        Restaurant restaurant = Restaurant.valueOf(parseRestaurantName);
        return menuService.postMenu(restaurant, date);
    }

    @PostMapping("/menu/local")
    public List<MenuVO> bridgeDish(@RequestParam String AUTH_KEY,
                                   @RequestParam(name = "restaurant") String restaurantToString,
                                   HttpServletResponse response) throws Exception {

        boolean isRightSecretKey = menuService.checkSecretKey(AUTH_KEY);

        if(!isRightSecretKey){
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            throw new IllegalArgumentException("Invalid AUTH_KEY: Unauthorized access");
        }

        String restaurantName = Restaurant.parseName(restaurantToString);
        Restaurant restaurant = Restaurant.valueOf(restaurantName);

        return menuService.getWeeklyMenuMap(restaurant);
    }

    @GetMapping("/week")
    public void week() throws Exception {
        LocalDate nearestMonday = getNearestMonday(LocalDate.now());
        LocalDate sunday = getSundayOfWeek(nearestMonday);

        menuService.saveWeeklyMenuAllRestaurant(nearestMonday, sunday);
    }
}