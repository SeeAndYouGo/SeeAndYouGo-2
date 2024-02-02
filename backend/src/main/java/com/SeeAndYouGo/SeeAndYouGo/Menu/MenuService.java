package com.SeeAndYouGo.SeeAndYouGo.Menu;

import com.SeeAndYouGo.SeeAndYouGo.Dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.Dish.DishDto;
import com.SeeAndYouGo.SeeAndYouGo.Dish.DishRepository;
import com.SeeAndYouGo.SeeAndYouGo.Dish.DishType;
import com.SeeAndYouGo.SeeAndYouGo.MenuDish.MenuDish;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MenuService {

    private final DishRepository dishRepository;
    private final MenuRepository menuRepository;
    private final RestaurantRepository restaurantRepository;

    public List<Menu> getOneDayRestaurantMenu(String placeName, String date) {
        String parseRestaurantName = parseRestaurantName(placeName);
        String parsedDate = date;
        try{
            // 들어온 문자열이 DB 형식에 맞지 않을 때만 캐싱
            parsedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd")).toString();
        }catch(Exception e){}

        Restaurant restaurant = restaurantRepository.findByNameAndDate(parseRestaurantName, parsedDate).get(0);
        List<Menu> menus = extractNotLunch(restaurant.getMenuList());

        return sortMainDish(menus);
    }

    private List<Menu> sortMainDish(List<Menu> menus) {
        List<Menu> sortMenus = new ArrayList<>();

        for (Menu menu : menus) {
            List<Dish> dishList = new ArrayList<>();
            for (Dish dish : menu.getDishList()) {
                if(dishList.contains(dish))
                    continue;
                if(dish.getDishType().equals(DishType.MAIN))
                    dishList.add(0, dish);
                else
                    dishList.add(dish);
            }
            menu.setDishList(dishList);
            sortMenus.add(menu);
        }
        return sortMenus;
    }
    public List<Menu>[] getOneWeekRestaurantMenu(String placeName, String date) {
        // 날짜 문자열을 파싱
        LocalDate parsedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 해당 주(week)의 시작 날짜와 끝 날짜 계산
        LocalDate startOfWeek = parsedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = parsedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
        List<Menu>[] weekMenuList = new List[5]; // mon to fri

        int idx = -1;
        for(LocalDate i = startOfWeek; i.compareTo(endOfWeek) <= 0; i = i.plusDays(1)){
            weekMenuList[++idx] = getOneDayRestaurantMenu(placeName, i.toString());
        }
        return weekMenuList;
    }

    private static boolean containDinner(Menu menu) {
        return menu.getMenuType().equals(MenuType.LUNCH);
    }

    private List<Menu> extractNotLunch(List<Menu> weekMenuList) {
        List<Menu> weekLunchMenus = new ArrayList<>();
        for (Menu menu : weekMenuList) {
            if(containDinner(menu))
                weekLunchMenus.add(menu);
        }
        return weekLunchMenus;
    }

    public String parseRestaurantName(String name) {
        if (name.contains("1")) return "1학생회관";
        else if (name.contains("2")) return "2학생회관";
        else if (name.contains("3")) return "3학생회관";
        else if (name.contains("4")) return "상록회관";
        else if (name.contains("5") ||name.contains("생활과학대") ) return "생활과학대";
        return name;
    }

    public Menu createMenuIfNotExists(Integer price, String date, Dept dept, Restaurant restaurant, MenuType menuType){
        List<Menu> menus = menuRepository.findByDateAndDeptAndRestaurantAndMenuType(date, dept, restaurant, menuType);
        if(menus.size() == 0){
            return Menu.builder()
                    .price(price)
                    .date(date)
                    .dept(dept)
                    .menuType(menuType)
                    .restaurant(restaurant)
                    .build();
        }
        return menus.get(0);
    }

    @Transactional
    public List<Menu> createMenuWithDishes(List<DishDto> dishDtos) {
        if (dishDtos.size() == 0) {
            return null;
        }

        Map<String, Menu> responseMap = new HashMap<>();
        for (DishDto dishDto : dishDtos) {
            String key = dishDto.getRestaurant().getName() + dishDto.getDept().toString() + dishDto.getDishType().toString() + dishDto.getDate()+dishDto.getMenuType().toString();

            if (!responseMap.containsKey(key)) {
                Dept dept = dishDto.getDept();
                MenuType menuType = dishDto.getMenuType();
                int price = dishDto.getPrice();
                String date = dishDto.getDate();
                Restaurant restaurant = dishDto.getRestaurant();
                Menu menu = createMenuIfNotExists(price, date, dept, restaurant, menuType);
                responseMap.put(key, menu);
            }

            Menu menu = responseMap.get(key);
            Dish dish = dishRepository.findByName(dishDto.getName());
            List<Dish> dishList = menu.getDishList();
            if (!dishList.contains(dish)) {
                dishList.add(dish);
                menu.getMenuDishes().add(new MenuDish(menu, dish));
            }
        }
        List<Menu> menus = new ArrayList<>(responseMap.values());
        menuRepository.saveAll(menus);

        return menus;
    }
}