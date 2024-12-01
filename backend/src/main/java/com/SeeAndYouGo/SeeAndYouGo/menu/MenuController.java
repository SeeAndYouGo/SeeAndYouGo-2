package com.SeeAndYouGo.SeeAndYouGo.menu;

import com.SeeAndYouGo.SeeAndYouGo.aop.log.TraceMethodLog;
import com.SeeAndYouGo.SeeAndYouGo.userKeyword.UserKeyword;
import com.SeeAndYouGo.SeeAndYouGo.userKeyword.UserKeywordRepository;
import com.SeeAndYouGo.SeeAndYouGo.menu.dto.MenuPostDto;
import com.SeeAndYouGo.SeeAndYouGo.menu.dto.MenuResponseByAdminDto;
import com.SeeAndYouGo.SeeAndYouGo.menu.dto.MenuResponseByUserDto;
import com.SeeAndYouGo.SeeAndYouGo.menu.dto.MenuResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt.TokenProvider;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import com.SeeAndYouGo.SeeAndYouGo.user.User;
import com.SeeAndYouGo.SeeAndYouGo.user.UserRepository;
import lombok.RequiredArgsConstructor;
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
    public List<MenuResponseByUserDto> restaurantMenuDayByUser(@PathVariable("restaurant") String place,
                                                                               @PathVariable(value = "user_id", required = false) String tokenId) {
        String date = getTodayDate();
        List<Menu> oneDayRestaurantMenu = menuService.getOneDayRestaurantMenu(place, date);  // 메인메뉴가 변하지 않았다면 캐싱해오고 있음

        List<String> keyStrings = new ArrayList<>();
        if (tokenId != null) {
            String email = tokenProvider.decodeToEmail(tokenId);
            User user = userRepository.findByEmail(email);
            List<UserKeyword> keywords = userKeywordRepository.findByUser(user);
            keyStrings = keywords.stream().map(x -> x.getKeyword().getName()).collect(Collectors.toList());
        }

        return parseOneDayRestaurantMenuByUser(oneDayRestaurantMenu, keyStrings);
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
        String place;

        for(int i=2; i<=5; i++) {
            place = "restaurant"+i;
            oneWeekRestaurantMenu = menuService.getOneWeekRestaurantMenu(place, date);

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
}