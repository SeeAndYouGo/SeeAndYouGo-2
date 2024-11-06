package com.SeeAndYouGo.SeeAndYouGo.menu.menuProvider;

import com.SeeAndYouGo.SeeAndYouGo.dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishRepository;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishType;
import com.SeeAndYouGo.SeeAndYouGo.menu.Dept;
import com.SeeAndYouGo.SeeAndYouGo.menu.Menu;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuRepository;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuType;
import com.SeeAndYouGo.SeeAndYouGo.menuDish.MenuDish;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class CrawlingMenuProvider implements MenuProvider{

    private final static String DORM_URL = "https://dorm.cnu.ac.kr/html/kr/sub03/sub03_0304.html";
    private final MenuRepository menuRepository;
    private final DishRepository dishRepository;

    @Override
    public List<Menu> getWeeklyMenu(Restaurant restaurant, LocalDate monday, LocalDate sunday) throws Exception {
        Connection connection = Jsoup.connect(DORM_URL);
        Document document = connection.get();

        Elements rows = document.select("#txt > table.default_view.diet_table > tbody > tr");

        LocalDate date = monday;
        for (int i = 0; i < rows.size(); i++, date = date.plusDays(1)) {
            Element row = rows.get(i);

            // 조식
            String firstColumn = row.select("td:nth-child(2)").text();
            if (!firstColumn.isEmpty()) {
                Map<String, List<String>> dishes = getDishes(firstColumn);
                for (String deptStr : dishes.keySet()) {
                    Dept dept = Dept.changeStringToDept(deptStr);

                    Menu menu = saveMenu(dept, date, restaurant, MenuType.BREAKFAST);

                    for (String dishToStr : dishes.get(deptStr)) {
                        if(!dishRepository.existsByName(dishToStr)){
                            Dish dish = Dish.builder()
                                    .name(dishToStr)
                                    .dishType(DishType.SIDE)
                                    .build();

                            dishRepository.save(dish);
                        }

                        Dish dish = dishRepository.findByName(dishToStr);

                        MenuDish menuDish = new MenuDish(menu, dish);
                        menu.addMenuDish(menuDish);
                        dish.addMenuDish(menuDish);
                    }
                }
            }

            // 중식
            String secondColumn = row.select("td:nth-child(3)").text();
            if (!secondColumn.isEmpty()) {
                Map<String, List<String>> dishes = getDishes(secondColumn);
                for (String deptStr : dishes.keySet()) {
                    Dept dept = Dept.changeStringToDept(deptStr);

                    Menu menu = saveMenu(dept, date, restaurant, MenuType.LUNCH);

                    for (String dishToStr : dishes.get(deptStr)) {
                        if(!dishRepository.existsByName(dishToStr)){
                            Dish dish = Dish.builder()
                                    .name(dishToStr)
                                    .dishType(DishType.SIDE)
                                    .build();

                            dishRepository.save(dish);
                        }

                        Dish dish = dishRepository.findByName(dishToStr);

                        MenuDish menuDish = new MenuDish(menu, dish);
                        menu.addMenuDish(menuDish);
                        dish.addMenuDish(menuDish);
                    }
                }
            }

            // 석식
            String lastColumn = row.select("td.left.last").text();
            if (!lastColumn.isEmpty()) {
                Map<String, List<String>> dishes = getDishes(secondColumn);
                for (String deptStr : dishes.keySet()) {
                    Dept dept = Dept.changeStringToDept(deptStr);

                    Menu menu = saveMenu(dept, date, restaurant, MenuType.DINNER);

                    for (String dishToStr : dishes.get(deptStr)) {
                        if(!dishRepository.existsByName(dishToStr)){
                            Dish dish = Dish.builder()
                                    .name(dishToStr)
                                    .dishType(DishType.SIDE)
                                    .build();

                            dishRepository.save(dish);
                        }

                        Dish dish = dishRepository.findByName(dishToStr);

                        MenuDish menuDish = new MenuDish(menu, dish);
                        menu.addMenuDish(menuDish);
                        dish.addMenuDish(menuDish);
                    }
                }
            }
        }

        return null;
    }

    private Menu saveMenu(Dept dept, LocalDate date, Restaurant restaurant, MenuType menuType) {
        Menu menu = Menu.builder()
                            .price(0)
                            .date(date.toString())
                            .dept(dept)
                            .menuType(menuType)
                            .restaurant(restaurant)
                            .build();

        return menuRepository.save(menu);
    }

    private Map<String, List<String>> getDishes(String text) {
        String[] lines = text.split(" ");
        Map<String, List<String>> menuMap = new HashMap<>(); // 메뉴 이름과 메뉴 항목 리스트를 매핑
        List<String> currentMenuList = null; // 현재 수집 중인 메뉴 리스트
        String currentMenuTitle = null; // 현재 메뉴 제목 (메인A, 메인C 등)

        for (String line : lines) {
            line = line.trim();

            // 새로운 메뉴 블록이 시작되면 메뉴 리스트 초기화
            if (line.contains("메인A") || line.contains("메인C")) {
                // 이전 메뉴 리스트 저장
                if (currentMenuTitle != null) {
                    menuMap.put(currentMenuTitle, new ArrayList<>(currentMenuList));
                }

                // 새 메뉴 리스트 초기화
                currentMenuTitle = line;
                currentMenuList = new ArrayList<>();
                continue;
            }

            // 영문 메뉴가 나오면 현재 메뉴 리스트 지우고 종료
            if (line.matches("^[a-zA-Z].*")) {
                if (currentMenuList != null) {
                    currentMenuList.clear(); // 현재 메뉴 리스트 초기화
                }
                currentMenuTitle = null;
                break;
            }

            // 한글 메뉴 항목만 추가하고 원산지 정보 제거
            if (currentMenuList != null && line.matches("^[가-힣]+.*")) {
                line = line.replaceAll("\\[.*?\\]", "").trim(); // 원산지 정보 제거
                currentMenuList.add(line);
            }
        }

        // 마지막 메뉴 리스트 저장
        if (currentMenuTitle != null && !currentMenuList.isEmpty()) {
            menuMap.put(currentMenuTitle, currentMenuList);
        }

        return menuMap;
    }

    private List<LocalDate> getDate(Document document) {
        Elements elements = document.select("#txt > div.diet_table_top > strong");
        String text = elements.text();

        // 정규식을 이용하여 날짜 부분을 추출합니다.
        Pattern datePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}"); // 날짜 형식에 맞는 패턴
        Matcher matcher = datePattern.matcher(text);

        List<LocalDate> dates = new ArrayList<>();
        while (matcher.find()) {
            String dateString = matcher.group();
            LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            dates.add(date);
        }

        if(dates.size() != 2){
            throw new IllegalArgumentException("날짜는 월요일, 일요일 총 2개만 나와야합니다.");
        }

        return dates;
    }
}
