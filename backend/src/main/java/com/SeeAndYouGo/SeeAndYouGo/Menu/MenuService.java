package com.SeeAndYouGo.SeeAndYouGo.Menu;

import com.SeeAndYouGo.SeeAndYouGo.Dish.*;
import com.SeeAndYouGo.SeeAndYouGo.Menu.dto.MenuPostDto;
import com.SeeAndYouGo.SeeAndYouGo.MenuDish.MenuDish;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Location;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MenuService {
    private static final Logger logger = LoggerFactory.getLogger(MenuService.class);

    private final DishRepository dishRepository;
    private final MenuRepository menuRepository;
    public static final String DEFAULT_DISH_NAME = "메뉴 정보 없음";

    /**
     * date에 주어진 restaurant가 제공하는 식단 정보를 반환한다.
     * 1, 2, 3학은 식당에서 메뉴를 2개 제공하고
     * 4, 5학은 식당에서 메뉴를 1개 제공하므로 List로 반환한다.
     */
    @Cacheable(value="getDailyMenu", key="#restaurantName.concat('-').concat(#date)")
    public List<Menu> getOneDayRestaurantMenu(String restaurantName, String date) {
        String parseRestaurantName = Restaurant.parseName(restaurantName);
        Restaurant restaurant = Restaurant.valueOf(parseRestaurantName);
        List<Menu> menus = menuRepository.findByRestaurantAndDate(restaurant, date);

//        List<Menu> menus = extractNotLunch(restaurant.getMenuList());

        return sortMainDish(menus);
    }

    /**
     * 메인메뉴가 가장 상단에 위치하도록 변경해준다.
     * @param menus
     * @return
     */
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

    /**
     * 주간 식단정보를 제공하기 위해 주어진 restaurantName에 해당하는 금주 식단 정보를 return한다.
     */
    public List<Menu>[] getOneWeekRestaurantMenu(String restaurantName, String date) {
        // 날짜 문자열을 파싱
        LocalDate parsedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // 해당 주(week)의 시작 날짜와 끝 날짜 계산
        LocalDate startOfWeek = parsedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = parsedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
        List<Menu>[] weekMenuList = new List[5]; // mon to fri

        int idx = -1;
        for(LocalDate i = startOfWeek; i.compareTo(endOfWeek) <= 0; i = i.plusDays(1)){
            weekMenuList[++idx] = getOneDayRestaurantMenu(restaurantName, i.toString());
        }
        return weekMenuList;
    }

    private static boolean containLunch(Menu menu) {
        return menu.getMenuType().equals(MenuType.LUNCH);
    }

//    /**
//     * 현재 서비스에서는 점심 식단만 제공하므로, 점심에 해당하지 않는 것은 제외한다.
//     */
//    private List<Menu> extractNotLunch(List<Menu> weekMenuList) {
//        List<Menu> weekLunchMenus = new ArrayList<>();
//        for (Menu menu : weekMenuList) {
//            if(containLunch(menu))
//                weekLunchMenus.add(menu);
//        }
//        return weekLunchMenus;
//    }

    /**
     * 그 날의 Menu 엔티티가 없다면 만들어야한다.
     */
    public Menu createMenuIfNotExists(Integer price, String date, Dept dept, Restaurant restaurant, MenuType menuType){
        if(menuRepository.existsByDateAndDeptAndRestaurantAndMenuType(date, dept, restaurant, menuType)){
            return menuRepository.findByDateAndDeptAndRestaurantAndMenuType(date, dept, restaurant, menuType);
        }

        return Menu.builder()
                .price(price)
                .date(date)
                .dept(dept)
                .menuType(menuType)
                .restaurant(restaurant)
                .build();
    }

    @Transactional
    public List<Menu> createMenuWithDishes(List<DishDto> dishDtos) {
        if (dishDtos.size() == 0) {
            return Collections.emptyList();
        }

        Map<String, Menu> responseMap = new HashMap<>();
        for (DishDto dishDto : dishDtos) {
            String key = dishDto.getRestaurant().toString() + dishDto.getDept().toString() + dishDto.getDishType().toString() + dishDto.getDate()+dishDto.getMenuType().toString();

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

            menu.addDish(dish);
        }
        List<Menu> menus = new ArrayList<>(responseMap.values());
        menuRepository.saveAll(menus);

        return menus;
    }

    @Transactional
    public void checkWeekMenu(LocalDate date) {
        // nearestMonday부터 금요일까지 2~5학의 메뉴 체크를 한다.
        for(int i = 0; i < 5; i++, date = date.plusDays(1)){
            for (Restaurant restaurant : Restaurant.values()) {
                if(restaurant.equals(Restaurant.제1학생회관)) continue; // 1학생회관은 고정적인 메뉴를 제공하므로 메뉴 데이터의 손실이 없으므로 패스

                checkMenuByDate(restaurant, date.toString());
            }
        }
    }

    /**
     * date 일자의 식당 데이터가 잘 들어가있는지 확인한다.
     * 1학은 고정적으로 메뉴가 생성되므로, 검사하지 않는다.
     * 2학과 3학은 STUDENT와 STAFF의 메뉴가 있는지 확인한다.
     * 4학과 5학은 STUDENT 메뉴가 있는지 확인한다.
     * @param date 확인하고 싶은 날짜
     */
    @Transactional
    public void checkMenuByDate(Restaurant restaurant, String date){
        List<Menu> menus = menuRepository.findByRestaurantAndDate(restaurant, date);

        fillMenu(restaurant, menus, date);
    }

    /**
     * restaurant에 menus가 빠짐없이 들어가있는지 확인하고, 빠져있는 것이 있다면 '메뉴 정보 없음'으로 입력해준다.
     *
     * @param restaurant
     * @param menus
     * @param date
     */
    private void fillMenu(Restaurant restaurant, List<Menu> menus, String date) {
//        String restaurantName = restaurant.getName();

        for (MenuType menuType : MenuType.values()) {
            if(menuType.equals(MenuType.BREAKFAST)){
                // 아침은
                // 교직원 : 없음
                // 학생식당 : 2학생회관
                // 만 운영하므로 따로 다뤄준다.
                if(restaurant.equals(Restaurant.제2학생회관)){
                    checkMenuByDeptAndMenuType(restaurant, menus, date, Dept.STUDENT, MenuType.BREAKFAST);
                }
            }else if(menuType.equals(MenuType.LUNCH)){
                // 점심에는
                // 학생식당 : 2, 3, 상록, 생과대
                // 교직원식당 : 2, 3학생회관
                // 만 메뉴를 제공한다.
                if(restaurant.equals(Restaurant.제2학생회관) || restaurant.equals(Restaurant.제3학생회관)){
                    checkMenuByDeptAndMenuType(restaurant, menus, date, Dept.STUDENT, MenuType.LUNCH);
                    checkMenuByDeptAndMenuType(restaurant, menus, date, Dept.STAFF, MenuType.LUNCH);
                }else if(restaurant.equals(Restaurant.상록회관) || restaurant.equals(Restaurant.생활과학대)){
                    checkMenuByDeptAndMenuType(restaurant, menus, date, Dept.STUDENT, MenuType.LUNCH);
                }
            }else{
                // 저녁은
                // 학생식당 : 3학생회관
                // 교직원 : 없음
                // 만 메뉴를 제공한다.
                if(restaurant.equals(Restaurant.제3학생회관)){
                    checkMenuByDeptAndMenuType(restaurant, menus, date, Dept.STAFF, MenuType.DINNER);
                }

            }
        }
    }

    /**
     * 해당 학생식당의 메뉴를 가지고, DEPT가 있는지 판단. 없다면 만들어준다.
     */
    private void checkMenuByDeptAndMenuType(Restaurant restaurant, List<Menu> menus, String date, Dept dept, MenuType menuType) {
        boolean existDeptAndMenuType = false;
        for (Menu menu : menus) {
            if(menu.getDept().equals(dept) && menu.getMenuType().equals(menuType)){
                existDeptAndMenuType = true;
            }
        }

        if(!existDeptAndMenuType){
            Dish defaultDish = getDefaultDish();
            Menu menu = Menu.builder()
                            .price(0) // defaultDish이므로 0원짜리다.
                            .date(date)
                            .dept(dept)
                            .menuType(menuType)
                            .restaurant(restaurant)
                            .build();

            menu.addDish(defaultDish);
            menuRepository.save(menu);
        }
    }

    /**
     * 메뉴 정보 없음의 데이터가 있다면 찾아서 반환하고, 없다면 생성한다.
     * 원래 DishService에 있어야 맞는 것 같지만... 의존성 문제 떄문에 여기에 둔다.
     * @return
     */
    public Dish getDefaultDish() {
        if(dishRepository.existsByName(DEFAULT_DISH_NAME)){
            return dishRepository.findByName(DEFAULT_DISH_NAME);
        }

        Dish dish = Dish.builder()
                .name(DEFAULT_DISH_NAME)
                .dishType(DishType.SIDE)
                .build();

        dishRepository.save(dish);
        return dish;
    }

    public MenuPostDto postMenu(Restaurant restaurant, String date) {
        String restaurantName = restaurant.toString();
        List<Menu> menu = getOneDayRestaurantMenu(restaurantName, date);

        String message = parseMessageFormat(menu, restaurantName);

        // message가 없다 == 메뉴가 없다. <- 이 경우 message에 "없음" 전송
        if(message == null || message.equals("")){
            message = "없음";
        }

        Location location = restaurant.getLocation();

        MenuPostDto dto = MenuPostDto.builder()
                .latitude(location.getLatitude().toString())
                .longitude(location.getLongitude().toString())
                .title("오늘의 메뉴")
                .content(message)
                .build();

        logger.info("[API_JJONGAL] 데이터: " + dto.toString());

        return dto;
    }

    /**
     * 각 학생식당의 메뉴를 message 형식으로 변환하여 return한다.
     */
    private String parseMessageFormat(List<Menu> menus, String restaurantName){
        StringBuilder sb = new StringBuilder();

        for(int i=0; i<menus.size(); i++){
            Menu menu = menus.get(i);

            // 만약 메뉴정보가 없다면 올리지 않는 방향으로!
            if(menu.getDishList().get(0).getName().equals("메뉴 정보 없음"))
                continue;

            sb.append(menu.getDept().getKoreanDept()+"식당 : ");
            sb.append(menu.getDishList());

            if((i+1) != menus.size())
                // 마지막이 아닐 때만 개행문자를 추가함.
                sb.append("\n");
        }

        return sb.toString();
    }

    @Transactional
    public List<Restaurant> createRestaurant1MenuOnJson(LocalDate date) {
        List<Restaurant> restaurants = new ArrayList<>();

        try {
            // Read the JSON file
            String jsonContent = new String(Files.readAllBytes(Paths.get("src/main/java/com/SeeAndYouGo/SeeAndYouGo/Restaurant/restaurantFormat.json").toAbsolutePath()));

            // Parse the JSON data
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonData = jsonParser.parse(jsonContent).getAsJsonObject();

            // Extract restaurantName
            String restaurantName = jsonData.get("restaurantName").toString().replace("\"", "");
            Restaurant restaurant = Restaurant.valueOf(restaurantName);

            List<Menu> menus = new ArrayList<>();

            // Extract menuName
            JsonArray menuNameArray = jsonData.getAsJsonArray("menuName");
            for (JsonElement menuJson : menuNameArray) {
                String name = menuJson.getAsJsonObject().get("name").toString().replace("\"", "");
                Dept dept = Dept.valueOf(menuJson.getAsJsonObject().get("dept").toString().replace("\"", ""));
                Integer price = Integer.parseInt(menuJson.getAsJsonObject().get("price").toString());

                if (dishRepository.findByName(name) == null) {
                    dishRepository.save(Dish.builder()
                            .name(name)
                            .dishType(DishType.MAIN)
                            .build());
                }

                Dish dish = dishRepository.findByName(name);
                Menu menu = Menu.builder()
                        .price(price)
                        .date(date.toString())
                        .dept(dept)
                        .menuType(MenuType.LUNCH)
                        .restaurant(restaurant)
                        .build();

                menu.setDishList(List.of(dish));
                menus.add(menu);
            }
            menuRepository.saveAll(menus);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return restaurants;
    }

    @Transactional
    public void createRestaurant1Menu(LocalDate nearestMonday) {
        for (LocalDate date = nearestMonday; date.getDayOfWeek() != DayOfWeek.SATURDAY; date = date.plusDays(1)) {
            createRestaurant1MenuOnJson(date);
        }
    }

    public List<Menu> findAllMenuByMainDish(Menu menu) {
        List<Dish> mainDish = menu.getMainDish();
        List<Menu> results = new ArrayList<>();
        for (Dish dish : mainDish) {
            dish.getMenuDishes().forEach(menuDish -> results.add(menuDish.getMenu()));
        }

        return results;
    }
}
