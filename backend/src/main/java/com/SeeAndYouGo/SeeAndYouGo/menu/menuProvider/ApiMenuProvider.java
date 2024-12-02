package com.SeeAndYouGo.SeeAndYouGo.menu.menuProvider;

import com.SeeAndYouGo.SeeAndYouGo.dish.*;
import com.SeeAndYouGo.SeeAndYouGo.menu.Dept;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuType;
import com.SeeAndYouGo.SeeAndYouGo.menu.dto.MenuVO;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@RequiredArgsConstructor
public class ApiMenuProvider implements MenuProvider{

    @Value("${DISH_KEY}")
    private String DISH_KEY;

    @Value("${URL.DISH_URL}")
    private String DISH_URL;

    // DB에 메뉴가 있다면 그걸 가져오고, 아니면 받아와야함.
    // 근데 이러면 MenuService랑 다른게 뭐지...
    @Override
    public List<MenuVO> getWeeklyMenu(Restaurant restaurant, LocalDate monday, LocalDate sunday) throws Exception {
        List<MenuVO> result = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        String foodInfo = getWeeklyMenuToString(monday, sunday);
        JsonArray resultArray = getJsonArray(foodInfo);

        // 페이지(파라미터)에 있는 dish 정보 리스트
        List<DishDto> dishDtos = new ArrayList<>();

        // "OutBlock" 배열 순회
        for (JsonElement element : resultArray) {
            JsonObject menuObject = element.getAsJsonObject();

            // 필드 값 추출
            String restaurantName = menuObject.get("CAFE_DIV_NM").getAsString();
            restaurantName = Restaurant.parseName(restaurantName);

            if (!restaurantName.equals(restaurant.toString()) && !(restaurantName.equals("제4학생회관") && restaurant.toString().equals("상록회관"))) {
                // restaurantName과 restaurant가 불일치하면 저장하지 않고 넘어감.
                // API는 4학생회관으로 주고, 우리는 상록회관으로 저장하니까 하드코딩 해두기.
                continue;
            }

            String deptStr = menuObject.get("CAFE_DTL_DIV_NM").getAsString();
            Dept dept = Dept.changeStringToDept(deptStr);

            String menuTypeStr = menuObject.get("FOOM_DIV_NM").getAsString();
            MenuType menuType = MenuType.changeStringToMenuType(menuTypeStr);

            String menuName = menuObject.get("MENU_KORN_NM").getAsString();
            if (menuName.contains("매주 수요일은")) continue;

            int price = 0;
            String priceStr = menuObject.get("MENU_PRC").getAsString();
            if (!priceStr.isEmpty())
                price = Integer.parseInt(priceStr);

            String dateStr = menuObject.get("FOOM_YMD").getAsString();
            LocalDate objDate = LocalDate.parse(dateStr, formatter);

            // DishDto 구성하기
            if (objDate.isAfter(monday) && objDate.isBefore(sunday) || objDate.isEqual(monday) || objDate.isEqual(sunday)) {
                // 식당, 날짜, DEPT, 메뉴타입을 기준으로 해당하는 식당을 찾는다.
                DishDto dishDto = DishDto.builder()
                        .name(menuName)
                        .dept(dept)
                        .date(objDate.toString())
                        .dishType(DishType.SIDE)
                        .restaurant(restaurant)
                        .menuType(menuType)
                        .price(price)
                        .build();

                dishDtos.add(dishDto);
            }
        }

        // 오늘 날짜의 Dish를 만들었으면, 이걸 기준으로 Menu를 만든다.
        if (!dishDtos.isEmpty()) {
            result.addAll(createMenuWithDishes(dishDtos));
        }

        return result;
    }

    private JsonArray getJsonArray(String foodInfo) {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(foodInfo).getAsJsonObject();

        return jsonObject.getAsJsonArray("RESULT");
    }

    public List<MenuVO> createMenuWithDishes(List<DishDto> dishDtos) {
        if (dishDtos.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, MenuVO> responseMap = new HashMap<>();
        for (DishDto dishDto : dishDtos) {
            // 제2학생회관STUDENTMAIN2024-10-11LUCNH 이런 형식임
            String key = dishDto.getRestaurant().toString() + dishDto.getDept().toString() + dishDto.getDishType().toString() + dishDto.getDate() + dishDto.getMenuType().toString();

            if (!responseMap.containsKey(key)) {
                Dept dept = dishDto.getDept();
                MenuType menuType = dishDto.getMenuType();
                int price = dishDto.getPrice();
                String date = dishDto.getDate();
                Restaurant restaurant = dishDto.getRestaurant();
                MenuVO menuVO = createMenuVO(price, date, dept, restaurant, menuType);
                responseMap.put(key, menuVO);
            }

            MenuVO menuVO = responseMap.get(key);
            DishVO dishVO = dishDto.toDishVO();

            menuVO.addDishVO(dishVO);
        }

        return new ArrayList<>(responseMap.values());
    }

    /**
     * 그 날의 Menu 엔티티가 없다면 만들어야한다.
     */
    public MenuVO createMenuVO(Integer price, String date, Dept dept, Restaurant restaurant, MenuType menuType){

        return new MenuVO(price, date, dept, restaurant, menuType);
    }

    public String getWeeklyMenuToString(LocalDate monday, LocalDate sunday) throws Exception {
        StringBuilder rawMenu = new StringBuilder();
        int page = 1;

        // 월요일의 메뉴가 들어왔다면 0번째가 true가 됨.
        boolean[] dayOfWeek = new boolean[7];

        while(true) {
            if(allTrue(dayOfWeek) || page > 3){
                // 어차피 3페이지 안에 다 나온다
                break;
            }

            String apiUrl = DISH_URL + "?page=" + page + "&AUTH_KEY=" + DISH_KEY;

            // URL 생성
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            // 응답 코드 확인
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                StringBuilder response = new StringBuilder();

                while ((line = reader.readLine()) != null) response.append(line);

                checkDate(response.toString(), dayOfWeek, monday, sunday);

                reader.close();

                mergeJsonResults(rawMenu, response);
            }

            page++;
        }

        return rawMenu.toString();
    }

    private void mergeJsonResults(StringBuilder string, StringBuilder response) {
        if(string.toString().isEmpty()){
            string.append(response);
            return;
        }

        JsonObject jsonObject1 = JsonParser.parseString(string.toString()).getAsJsonObject();
        JsonArray resultArray1 = jsonObject1.getAsJsonArray("RESULT");

        JsonObject jsonObject2 = JsonParser.parseString(response.toString()).getAsJsonObject();
        JsonArray resultArray2 = jsonObject2.getAsJsonArray("RESULT");

        // 두 번째 JSON의 RESULT 배열 요소들을 첫 번째 JSON의 RESULT 배열에 추가
        for (JsonElement element : resultArray2) {
            resultArray1.add(element);
        }

        // 병합된 결과를 첫 번째 StringBuilder에 반영
        jsonObject1.add("RESULT", resultArray1);
        string.setLength(0);  // 기존 내용 지우기
        string.append(jsonObject1);  // 병합된 JSON을 추가
    }

    private void checkDate(String line, boolean[] dayOfWeek, LocalDate monday, LocalDate sunday) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        for(LocalDate date = monday; !date.isAfter(sunday); date = date.plusDays(1)){
            String dateToString = date.format(formatter);
            if(line.contains(dateToString)){
                // date에 해당하는 날짜가 line에 포함되면 dayOfWeek의 해당 인덱스 true로 바꿔주기
                int i = date.getDayOfWeek().getValue() - 1;
                dayOfWeek[i] = true;
            }

        }
    }

    public static boolean allTrue(boolean[] boolArray) {
        for (boolean value : boolArray) {
            if (!value) {
                return false;
            }
        }
        return true;
    }
}
