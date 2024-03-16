package com.SeeAndYouGo.SeeAndYouGo.Menu;

import com.SeeAndYouGo.SeeAndYouGo.Dish.*;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.RestaurantRepository;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MenuService {

    private final DishRepository dishRepository;
    private final MenuRepository menuRepository;
    private final RestaurantRepository restaurantRepository;
    public static final String DEFAULT_DISH_NAME = "메뉴 정보 없음";

    /**
     * date에 주어진 restaurant가 제공하는 식단 정보를 반환한다.
     * 1, 2, 3학은 식당에서 메뉴를 2개 제공하고
     * 4, 5학은 식당에서 메뉴를 1개 제공하므로 List로 반환한다.
     */
    public List<Menu> getOneDayRestaurantMenu(String restaurantName, String date) {
        String parseRestaurantName = Restaurant.parseName(restaurantName);

        Restaurant restaurant = restaurantRepository.findByNameAndDate(parseRestaurantName, date);
        List<Menu> menus = extractNotLunch(restaurant.getMenuList());

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

    /**
     * 현재 서비스에서는 점심 식단만 제공하므로, 점심에 해당하지 않는 것은 제외한다.
     */
    private List<Menu> extractNotLunch(List<Menu> weekMenuList) {
        List<Menu> weekLunchMenus = new ArrayList<>();
        for (Menu menu : weekMenuList) {
            if(containLunch(menu))
                weekLunchMenus.add(menu);
        }
        return weekLunchMenus;
    }

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
            for (String restaurantName : RestaurantService.getRestaurantNames()) {
                if(restaurantName.contains("1")) continue; // 1학생회관은 고정적인 메뉴를 제공하므로 메뉴 데이터의 손실이 없으므로 패스

                checkMenuByDate(restaurantName, date.toString());
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
    public void checkMenuByDate(String restaurantName, String date){
        Restaurant restaurant = restaurantRepository.findByNameAndDate(restaurantName, date);
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
        String restaurantName = restaurant.getName();

        checkMenuByDept(restaurant, menus, date, Dept.STUDENT);

        if(restaurantName.contains("2") || restaurantName.contains("3")){
            // 2학과 3학은 STAFF가 추가로 존재해야한다.
            checkMenuByDept(restaurant, menus, date, Dept.STAFF);
        }
    }

    /**
     * 해당 학생식당의 메뉴를 가지고, DEPT가 있는지 판단. 없다면 만들어준다.
     */
    private void checkMenuByDept(Restaurant restaurant, List<Menu> menus, String date, Dept dept) {
        boolean existDept = false;
        for (Menu menu : menus) {
            if(menu.getDept().equals(dept)){
                existDept = true;
            }
        }

        if(!existDept){
            Dish defaultDish = getDefaultDish();
            Menu menu = Menu.builder()
                            .price(0)
                            .date(date)
                            .dept(dept)
                            .menuType(MenuType.LUNCH)
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
}