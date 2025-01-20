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
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
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

    private Map<Restaurant, List<MenuVO>> menuMap = new HashMap<>();

    @Override
    public List<MenuVO> getWeeklyMenu(Restaurant restaurant) throws Exception {
        return menuMap.get(restaurant);
    }

    @Override
    public List<MenuVO> getWeeklyMenuMap(Restaurant restaurant) throws Exception {
        return getWeeklyMenu(restaurant);
    }

    public void updateMenuMap(Restaurant restaurant, LocalDate monday, LocalDate sunday) throws IOException {
        Connection connection = Jsoup.connect(DORM_URL);
        Document document = connection.get();

        List<MenuVO> menuVOs = new ArrayList<>();

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

                    MenuVO menuVO = CreateMenuVO(dept, date, restaurant, MenuType.BREAKFAST);

                    for (String dishToStr : dishes.get(deptStr)) {

                        DishVO dishVO = new DishVO(dishToStr, DishType.SIDE);
                        menuVO.addDishVO(dishVO);
                    }

                    menuVOs.add(menuVO);
                }
            }

            // 중식
            String secondColumn = row.select("td:nth-child(3)").text();
            if (!secondColumn.isEmpty()) {
                Map<String, List<String>> dishes = getDishes(secondColumn);
                for (String deptStr : dishes.keySet()) {
                    Dept dept = Dept.changeStringToDept(deptStr);

                    MenuVO menuVO = CreateMenuVO(dept, date, restaurant, MenuType.LUNCH);

                    for (String dishToStr : dishes.get(deptStr)) {

                        DishVO dishVO = new DishVO(dishToStr, DishType.SIDE);
                        menuVO.addDishVO(dishVO);
                    }

                    menuVOs.add(menuVO);
                }
            }

            // 석식
            String lastColumn = row.select("td.left.last").text();
            if (!lastColumn.isEmpty()) {
                Map<String, List<String>> dishes = getDishes(secondColumn);
                for (String deptStr : dishes.keySet()) {
                    Dept dept = Dept.changeStringToDept(deptStr);

                    MenuVO menuVO = CreateMenuVO(dept, date, restaurant, MenuType.DINNER);

                    for (String dishToStr : dishes.get(deptStr)) {

                        DishVO dishVO = new DishVO(dishToStr, DishType.SIDE);
                        menuVO.addDishVO(dishVO);
                    }

                    menuVOs.add(menuVO);
                }
            }
        }

        menuMap.put(restaurant, menuVOs);
    }

    public String getWeeklyMenuToString(LocalDate monday, LocalDate sunday) throws Exception {
        throw new IllegalArgumentException(this.getClass().toString() + "의 getWeeklyMenuToString은 호출이 금지되어 있습니다.");
    }

    private MenuVO CreateMenuVO(Dept dept, LocalDate date, Restaurant restaurant, MenuType menuType) {
        return new MenuVO(0, date.toString(), dept, restaurant, menuType);
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
