package com.SeeAndYouGo.SeeAndYouGo.menu.menuProvider;

import com.SeeAndYouGo.SeeAndYouGo.dish.DishType;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishVO;
import com.SeeAndYouGo.SeeAndYouGo.menu.Dept;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuType;
import com.SeeAndYouGo.SeeAndYouGo.menu.dto.MenuVO;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.SeeAndYouGo.SeeAndYouGo.global.DateTimeFormatters.DATE;
import static com.SeeAndYouGo.SeeAndYouGo.global.MenuConstants.DEFAULT_DISH_NAME;

@Component
@RequiredArgsConstructor
public class CrawlingMenuProvider implements MenuProvider{
    private static final Logger logger = LoggerFactory.getLogger(CrawlingMenuProvider.class);
    private final static String DORM_URL = "https://dorm.cnu.ac.kr/html/kr/sub04/sub04_040301.html";

    private Map<Restaurant, List<MenuVO>> menuMap = new HashMap<>();

    @Override
    public List<MenuVO> getWeeklyMenu(Restaurant restaurant) throws Exception {
        return menuMap.get(restaurant);
    }

    @Override
    public List<MenuVO> getWeeklyMenuMap(Restaurant restaurant) throws Exception {
        return getWeeklyMenu(restaurant);
    }

    @Override
    public void updateDailyMenu(Restaurant restaurant, LocalDate date) throws Exception {
        List<MenuVO> dailyMenu = new ArrayList<>();

        // 1. 주간 URL 가져오기
        Connection mainConnection = Jsoup.connect(DORM_URL);
        Document mainDocument = mainConnection.get();
        Elements dayLinks = mainDocument.select(".custom-week li a");

        for (Element dayLink : dayLinks) {
            String dayUrl = DORM_URL + dayLink.attr("href");
            String dateStr = dayLink.attr("href").split("=")[1].split("#")[0];
            LocalDate targetDate = LocalDate.parse(dateStr);

            // updateDailyMenu는 특정 날짜의 데이터만 업데이트 해야함.
            if(!targetDate.equals(date)) continue;

            Connection dayConnection = Jsoup.connect(dayUrl);
            Document dayDocument = dayConnection.get();

            // 2. 아침, 점심, 저녁 메뉴 가져오기
            Elements mealTds = dayDocument.select(".diet_table td");
            for (Element mealTd : mealTds) {
                String mealTypeStr = mealTd.attr("data-cell-header");
                MenuType menuType;
                if (mealTypeStr.equals("아침")) {
                    menuType = MenuType.BREAKFAST;
                } else if (mealTypeStr.equals("점심")) {
                    menuType = MenuType.LUNCH;
                } else {
                    menuType = MenuType.DINNER;
                }

                String menuContent = mealTd.toString();
                if (!menuContent.isEmpty()) {
                    Map<String, List<String>> dishes = getDishes(menuContent);
                    for (String deptStr : dishes.keySet()) {
                        Dept dept = Dept.changeStringToDept(deptStr);
                        MenuVO menuVO = CreateMenuVO(dept, date, restaurant, menuType);
                        for (String dishToStr : dishes.get(deptStr)) {
                            DishVO dishVO = new DishVO(dishToStr, DishType.SIDE);
                            menuVO.addDishVO(dishVO);
                        }
                        dailyMenu.add(menuVO);
                    }
                }
            }
        }

        // Fill absent menu for the day
        if (dailyMenu.stream().noneMatch(menu -> menu.getMenuType() == MenuType.BREAKFAST)) {
            addDefaultMenu(dailyMenu, date, Dept.DORM_A, restaurant, MenuType.BREAKFAST);
        }
        if (dailyMenu.stream().noneMatch(menu -> menu.getMenuType() == MenuType.LUNCH)) {
            addDefaultMenu(dailyMenu, date, Dept.DORM_A, restaurant, MenuType.LUNCH);
        }
        if (dailyMenu.stream().noneMatch(menu -> menu.getMenuType() == MenuType.DINNER)) {
            addDefaultMenu(dailyMenu, date, Dept.DORM_A, restaurant, MenuType.DINNER);
        }

        List<MenuVO> weeklyMenu = menuMap.get(restaurant);
        if (weeklyMenu != null) {
            weeklyMenu.removeIf(menuVO -> menuVO.getDate().equals(date.toString()));
            weeklyMenu.addAll(dailyMenu);
            menuMap.put(restaurant, weeklyMenu);
        }
    }

    public void updateMenuMap(Restaurant restaurant, LocalDate monday, LocalDate sunday) throws IOException {
        List<MenuVO> menuVOs = new ArrayList<>();

        // 1. 주간 URL 가져오기
        Connection mainConnection = Jsoup.connect(DORM_URL);
        Document mainDocument = mainConnection.get();
        Elements dayLinks = mainDocument.select(".custom-week li a");

        for (Element dayLink : dayLinks) {
            String dayUrl = DORM_URL + dayLink.attr("href");
            String dateStr = dayLink.attr("href").split("=")[1].split("#")[0];
            LocalDate date = LocalDate.parse(dateStr);

            Connection dayConnection = Jsoup.connect(dayUrl);
            Document dayDocument = dayConnection.get();

            // 2. 아침, 점심, 저녁 메뉴 가져오기
            Elements mealTds = dayDocument.select(".diet_table td");
            for (Element mealTd : mealTds) {
                String mealTypeStr = mealTd.attr("data-cell-header");
                MenuType menuType;
                if (mealTypeStr.equals("아침")) {
                    menuType = MenuType.BREAKFAST;
                } else if (mealTypeStr.equals("점심")) {
                    menuType = MenuType.LUNCH;
                } else {
                    menuType = MenuType.DINNER;
                }

                String menuContent = mealTd.toString();
                if (!menuContent.isEmpty()) {
                    Map<String, List<String>> dishes = getDishes(menuContent);
                    for (String deptStr : dishes.keySet()) {
                        Dept dept = Dept.changeStringToDept(deptStr);
                        MenuVO menuVO = CreateMenuVO(dept, date, restaurant, menuType);
                        for (String dishToStr : dishes.get(deptStr)) {
                            DishVO dishVO = new DishVO(dishToStr, DishType.SIDE);
                            menuVO.addDishVO(dishVO);
                        }
                        menuVOs.add(menuVO);
                    }
                }
            }
        }

        fillAbsentMenu(restaurant, menuVOs, monday, sunday);

        menuMap.put(restaurant, menuVOs);
    }

    /**
     * 학생생활관에서 제공하지 않는 경우에는 메뉴정보없음을 채워준다.
     *
     * @param restaurant
     * @param menuVOs
     * @param monday
     * @param sunday
     */
    private void fillAbsentMenu(Restaurant restaurant, List<MenuVO> menuVOs, LocalDate monday, LocalDate sunday) {
        if(!restaurant.equals(Restaurant.학생생활관)) {
            throw new IllegalArgumentException("현재 크롤링은 학생생활관만 제공됩니다.");
        }

        List<MenuVO> breakfast = menuVOs.stream().filter(menuVO -> menuVO.getMenuType() == MenuType.BREAKFAST).collect(Collectors.toList());
        List<MenuVO> lunch = menuVOs.stream().filter(menuVO -> menuVO.getMenuType() == MenuType.LUNCH).collect(Collectors.toList());
        List<MenuVO> dinner = menuVOs.stream().filter(menuVO -> menuVO.getMenuType() == MenuType.DINNER).collect(Collectors.toList());

        for(LocalDate date = monday; !date.isAfter(sunday); date = date.plusDays(1)) {
            if(!hasDate(breakfast, date)){
                addDefaultMenu(menuVOs, date, Dept.DORM_A, restaurant, MenuType.BREAKFAST);
            }

            if(!hasDate(lunch, date)){
                addDefaultMenu(menuVOs, date, Dept.DORM_A, restaurant, MenuType.LUNCH);
            }

            if(!hasDate(dinner, date)){
                addDefaultMenu(menuVOs, date, Dept.DORM_A, restaurant, MenuType.DINNER);
            }
        }
    }

    private void addDefaultMenu(List<MenuVO> menus, LocalDate date, Dept dept, Restaurant restaurant, MenuType menuType) {
        MenuVO defaultMenu = new MenuVO(0, date.toString(), dept, restaurant, menuType);
        DishVO defaultDish = new DishVO(DEFAULT_DISH_NAME, DishType.SIDE);

        defaultMenu.addDishVO(defaultDish);
        menus.add(defaultMenu);
    }

    private boolean hasDate(List<MenuVO> menus, LocalDate date) {
        List<MenuVO> collect = menus.stream().filter(menuVO -> menuVO.getDate().equals(date.toString())).collect(Collectors.toList());

        return !collect.isEmpty();
    }

    private MenuVO CreateMenuVO(Dept dept, LocalDate date, Restaurant restaurant, MenuType menuType) {
        return new MenuVO(0, date.toString(), dept, restaurant, menuType);
    }

    private Map<String, List<String>> getDishes(String text) {
        String[] lines = text.replace("<td class=\"left\">", "")
                .replace("<td class=\"left last\">", "")
                .replace("</td>", "")
                .replace("<br><br>", "<br>")
                .split("<br>");

        Map<String, List<String>> menuMap = new HashMap<>();
        List<String> currentMenuList = null;
        String currentMenuTitle = null;

        boolean inBracketBlock = false; // [가 시작되고 ] 닫힐 때까지 무시

        for (String line : lines) {
            line = line.trim();

            // 블록 무시 시작
            if (line.contains("[") && !line.contains("]")) {
                // [는 있으나 ]는 없는 경우: 메뉴에서 원산지 분리
                int idx = line.indexOf("[");
                if (idx > 0) {
                    line = line.substring(0, idx).trim();
                    currentMenuList.add(Entities.unescape(line));
                }
                inBracketBlock = true; // 이후 줄은 무시되도록 유지
            }
            // 블록 무시 종료
            if (inBracketBlock) {
                if (line.contains("]")) {
                    inBracketBlock = false;
                }
                continue;
            }

            // 다음 메뉴가 나온다면 이전 메뉴를 저장함.
            if (line.contains("메인") || line.contains("Main") || line.contains("MAIN")) {
                if (currentMenuTitle != null) {
                    putMenu(menuMap, currentMenuTitle, currentMenuList);
                }
                currentMenuTitle = line.replaceAll("\\s*[\\(（][^\\)）]*[\\)）]", "").trim();
                currentMenuList = new ArrayList<>();
                continue;
            }

            // 영문이 나오면 메뉴 저장 종료
            if (line.matches("^[a-zA-Z].*")) {
                if (currentMenuList != null) {
                    currentMenuList.clear();
                }
                currentMenuTitle = null;
                break;
            }

            // 원산지, 알레르기 정보 등의 메뉴 이름을 가공.
            if (currentMenuList != null && line.matches(".*[가-힣]+.*")) {
                line = cleanMenuName(line);

                if(line.isEmpty() || line.startsWith("\\")) continue;
                currentMenuList.add(Entities.unescape(line));
            }
        }

        if (currentMenuTitle != null && !currentMenuList.isEmpty()) {
            putMenu(menuMap, currentMenuTitle, currentMenuList);
        }

        return menuMap;
    }

    private void putMenu(Map<String, List<String>> menuMap, String title, List<String> menuList) {
        String newTitle = title;
        if (newTitle.contains("메인A") && menuMap.containsKey(newTitle)) {
            newTitle = newTitle.replace("메인A", "메인C");
        }

        if (menuMap.containsKey(newTitle)) {
            logger.warn("중복된 메뉴 코너가 발생했습니다: {}", newTitle);
            throw new IllegalStateException("Duplicate menu corner found: " + newTitle);
        }
        menuMap.put(newTitle, new ArrayList<>(menuList));
    }

    private String cleanMenuName(String menuName) {
        String cleaned = menuName;

        // 시험기간 event와 같은 부분은 메뉴로 표시하지 않기
        if(cleaned.contains("시험기간") || cleaned.equals("**땅콩함유**")){
            return "";
        }

        // 앞뒤 * 제거
        cleaned = cleaned.replaceAll("^\\*(.*?)\\*$", "$1");

        // 대괄호 정보 제거 (원산지, 특수 알레르기)
        cleaned = cleaned.replaceAll("\\[.*?\\]", "");

        // 숫자 알레르기 코드 제거 (5,6,9,10,16 형태)
        cleaned = cleaned.replaceAll("\\d+(,\\d+)*", "");

        // 우유(공통)으로 오면 우유로 변경
        cleaned = cleaned.replace("(공통)", "");

        // 필요없는 문자 제거
        cleaned = cleaned.replace(".", "").replace(",", "");

        return cleaned.trim();
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
            LocalDate date = LocalDate.parse(dateString, DATE);

            dates.add(date);
        }

        if(dates.size() != 2){
            throw new IllegalArgumentException("날짜는 월요일, 일요일 총 2개만 나와야합니다.");
        }

        return dates;
    }
}
