package com.SeeAndYouGo.SeeAndYouGo.menu;

import com.SeeAndYouGo.SeeAndYouGo.caching.annotation.EvictAllCache;
import com.SeeAndYouGo.SeeAndYouGo.dish.*;
import com.SeeAndYouGo.SeeAndYouGo.menu.dto.MenuPostDto;
import com.SeeAndYouGo.SeeAndYouGo.menu.dto.MenuVO;
import com.SeeAndYouGo.SeeAndYouGo.menu.mainCache.ClearMainDishCache;
import com.SeeAndYouGo.SeeAndYouGo.menu.mainCache.NewDishCacheService;
import com.SeeAndYouGo.SeeAndYouGo.menu.menuProvider.MenuProvider;
import com.SeeAndYouGo.SeeAndYouGo.menu.menuProvider.MenuProviderFactory;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Location;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import static com.SeeAndYouGo.SeeAndYouGo.IterService.getNearestMonday;
import static com.SeeAndYouGo.SeeAndYouGo.IterService.getSundayOfWeek;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MenuService {
    private static final Logger logger = LoggerFactory.getLogger(MenuService.class);

    private final DishRepository dishRepository;
    private final MenuRepository menuRepository;

    // 로컬에서 운영서버로 데이터를 넘겨주기 위함.
    private final MenuProviderFactory menuProviderFactory;
    private final NewDishCacheService newDishCacheService;

    public static final String DEFAULT_DISH_NAME = "메뉴 정보 없음";

    @Value("${API.DISH_KEY}")
    private String DISH_KEY;

    /**
     * date에 주어진 restaurant가 제공하는 식단 정보를 반환한다.
     * 1, 2, 3학은 식당에서 메뉴를 2개 제공하고
     * 4, 5학은 식당에서 메뉴를 1개 제공하므로 List로 반환한다.
     */
    public List<Menu> getOneDayRestaurantMenu(String restaurantName, String date) {
        String parseRestaurantName = Restaurant.parseName(restaurantName);
        Restaurant restaurant = Restaurant.valueOf(parseRestaurantName);
        List<Menu> menus = menuRepository.findByRestaurantAndDate(restaurant, date);  // 검색을 여기서 하는게 낫지 않나 싶기두 ~

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
        LocalDate endOfWeek = parsedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        List<Menu>[] weekMenuList = new List[7]; // mon to sun

        int idx = -1;
        for(LocalDate i = startOfWeek; i.compareTo(endOfWeek) <= 0; i = i.plusDays(1)){
            weekMenuList[++idx] = getOneDayRestaurantMenu(restaurantName, i.toString());
        }
        return weekMenuList;
    }

    @Transactional
    public void checkWeekMenu(LocalDate monday, LocalDate sunday) {
        // nearestMonday부터 일요일까지 2~5학의 메뉴 체크를 한다.
        for(LocalDate date = monday; !date.isAfter(sunday); date = date.plusDays(1)){
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
     */
    private void fillMenu(Restaurant restaurant, List<Menu> menus, String date) {

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
                            .isOpen(false)
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
            return dishRepository.findByName(DEFAULT_DISH_NAME)
                    .orElseThrow(() -> new RuntimeException("메뉴 정보 없음 dish가 없습니다."));
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

    public List<Menu> findAllMenuByMainDish(Menu menu) {
        List<Dish> mainDish = menu.getMainDish();
        List<Menu> results = new ArrayList<>();
        for (Dish dish : mainDish) {
            dish.getMenuDishes().forEach(menuDish -> results.add(menuDish.getMenu()));
        }

        return results;
    }

    public boolean checkSecretKey(String authKey) {
        return DISH_KEY.equals(authKey);
    }

    @Transactional
    public void saveWeeklyMenuAllRestaurant(LocalDate monday, LocalDate sunday) throws Exception {
        for (Restaurant restaurant : Restaurant.values()) {
            saveWeeklyMenu(restaurant, monday, sunday);
        }

        // api로 받아오지 못한 부분에는 '메뉴정보없음'을 표기한다.
        checkWeekMenu(monday, sunday);
    }
  
    @Transactional
    @ClearMainDishCache
    @EvictAllCache({"daily-menu", "weekly-menu"})
    public void saveWeeklyMenu(Restaurant restaurant, LocalDate monday, LocalDate sunday) throws Exception {
        MenuProvider menuProvider = menuProviderFactory.createMenuProvider(restaurant);
        menuProvider.updateMenuMap(restaurant, monday, sunday);

        List<MenuVO> weeklyMenu = menuProvider.getWeeklyMenu(restaurant);

        // 이제 받은 List<MenuVO>를 List<Menu>로 변환하고 저장한다.
        for (MenuVO menuVO : weeklyMenu) {
            List<DishVO> dishVOs = menuVO.getDishVOs();

            Menu menu = new Menu(menuVO);
            for (DishVO dishVO : dishVOs) {
                Dish dish;

                // 만약 해당 Dish가 있으면 그걸 repository에서 가져오고 없다면 생성한다.
                if(!dishRepository.existsByName(dishVO.getName())){
                    dish = Dish.builder()
                                    .name(dishVO.getName())
                                    .dishType(dishVO.getDishType())
                                    .build();

                    dishRepository.save(dish);
                }else{
                    dish = dishRepository.findByName(dishVO.getName()).get();
                }

                menu.addDish(dish);
            }

            menuRepository.save(menu);
        }
    }

    public void updateAllRestaurantMenuMap() throws Exception {
        LocalDate nearestMonday = getNearestMonday(LocalDate.now());
        LocalDate sunday = getSundayOfWeek(nearestMonday);

        for (Restaurant restaurant : Restaurant.values()) {
            MenuProvider menuProvider = menuProviderFactory.createMenuProvider(restaurant);
            menuProvider.updateMenuMap(restaurant, nearestMonday, sunday);
        }
    }

    // 여기서 전달되는 menu와 dish는 모두 이미 DB에 저장된 것들이다
    public List<MenuVO> getWeeklyMenuMap(Restaurant restaurant) throws Exception {
        MenuProvider menuProvider = menuProviderFactory.createMenuProvider(restaurant);

        return menuProvider.getWeeklyMenuMap(restaurant);
    }

    public List<String> getNewMainDishs(String place, List<Dish> mainDishs) {
        String parseRestaurantName = Restaurant.parseName(place);
        Restaurant restaurant = Restaurant.valueOf(parseRestaurantName);
        
        // 캐시에서 과거 메인메뉴들 조회
        Set<String> historicalMainDishes = newDishCacheService.getHistoricalMainDishes(restaurant);
        
        // 오늘 메인 요리 중에서 과거에 없던 것들만 필터링
        return mainDishs.stream()
                .map(Dish::getName)
                .filter(dishName -> !historicalMainDishes.contains(dishName))
                .collect(Collectors.toList());
    }
}