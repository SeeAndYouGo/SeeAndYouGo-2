package com.SeeAndYouGo.SeeAndYouGo.Menu;

import com.SeeAndYouGo.SeeAndYouGo.Dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.RestaurantRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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

    private final MenuRepository menuRepository;
    private final RestaurantRepository restaurantRepository;

    public List<Menu> getOneDayRestaurantMenu(String placeName, String date) {
        String parseRestaurantName = parseRestaurantName(placeName);
        String parsedDate = date;
        try{
            // 들어온 문자열이 DB 형식에 맞지 않을 때만 캐싱
            parsedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd")).toString();
        }catch(Exception e){}

        Restaurant restaurant = restaurantRepository.findTodayRestaurant(parseRestaurantName, parsedDate);
        System.out.println(restaurant.getMenuList());
        return restaurant.getMenuList();
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



    public String parseRestaurantName(String name) {
        if (name.contains("1")) return "1학생회관";
        else if (name.contains("2")) return "2학생회관";
        else if (name.contains("3")) return "3학생회관";
        else if (name.contains("4")) return "상록회관";
        else if (name.contains("5") ||name.contains("생활과학대") ) return "생활과학대";
        return name;
    }

    @Transactional
    public List<Menu> createMenuWithDishses(List<Dish> dishes) {
        Map<String, Menu> responseMap = new HashMap<>();

        for (Dish dish : dishes) {
            String key = dish.getRestaurant().getName() + dish.getDept().toString() + dish.getDishType().toString() + dish.getDate()+dish.getMenuType().toString();

            if (!responseMap.containsKey(key)) {
                Dept dept = dish.getDept();
                MenuType menuType = dish.getMenuType();
                int price = dish.getPrice();
                String date = dish.getDate();
                Restaurant restaurant = dish.getRestaurant();
                Menu menu = new Menu(price, date, dept, menuType, restaurant);
                responseMap.put(key, menu);
            }

            Menu menu = responseMap.get(key);
            menu.getDishList().add(dish);
        }

        List<Menu> menus = new ArrayList<>(responseMap.values());
        menuRepository.saveAll(menus);

        return menus;
    }
}
