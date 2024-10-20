package com.SeeAndYouGo.SeeAndYouGo.Dish;

import com.SeeAndYouGo.SeeAndYouGo.IterService;
import com.SeeAndYouGo.SeeAndYouGo.Menu.*;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DishService {
    private final DishRepository dishRepository;
    private final MenuService menuService;

    @Value("${DISH_KEY}")
    private String DISH_KEY;

    @Value("${URL.DISH_URL}")
    private String DISH_URL;

    @Transactional
    public void saveAndCacheWeekDish(Integer page) throws Exception{
        String foodInfo = fetchDishInfoToString(page);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate monday = IterService.getNearestMonday(LocalDate.now());
        LocalDate friday = IterService.getFridayOfWeek(monday);

        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(foodInfo).getAsJsonObject();

        JsonArray resultArray = jsonObject.getAsJsonArray("RESULT");

        // 페이지(파라미터)에 있는 dish 정보 리스트
        List<DishDto> dishDtos = new ArrayList<>();

        // "OutBlock" 배열 순회
        for (JsonElement element : resultArray) {
            JsonObject menuObject = element.getAsJsonObject();

            // 필드 값 추출
            String restaurantName = menuObject.get("CAFE_DIV_NM").getAsString();
            restaurantName = Restaurant.parseName(restaurantName);

            String deptStr = menuObject.get("CAFE_DTL_DIV_NM").getAsString();
            Dept dept = Dept.changeStringToDept(deptStr);

            String menuTypeStr = menuObject.get("FOOM_DIV_NM").getAsString();
            MenuType menuType = MenuType.changeStringToMenuType(menuTypeStr);

//            if(!menuType.equals(MenuType.LUNCH)) continue; 이제부터는 점심만 보여주는 것이 아니라, 아침/점심/저녁을 보여준다.

            String menuName = menuObject.get("MENU_KORN_NM").getAsString();
            if(menuName.contains("매주 수요일은")) continue;

            int price = 0;
            String priceStr = menuObject.get("MENU_PRC").getAsString();
            if(priceStr.length()!=0)
                price = Integer.parseInt(priceStr);

            String dateStr = menuObject.get("FOOM_YMD").getAsString();
            LocalDate objDate = LocalDate.parse(dateStr, formatter);

            // DishDto 구성하기
            if(objDate.isAfter(monday) && objDate.isBefore(friday) || objDate.isEqual(monday) || objDate.isEqual(friday)) {
                // 식당, 날짜, DEPT, 메뉴타입을 기준으로 해당하는 식당을 찾는다.
                Restaurant restaurant = Restaurant.valueOf(restaurantName);
                DishDto dishDto = DishDto.builder()
                        .name(menuName)
                        .dept(dept)
                        .date(objDate.toString())
                        .dishType(DishType.SIDE)
                        .restaurant(restaurant)
                        .menuType(menuType)
                        .price(price)
                        .build();

                // DB에 없던 dish면 등록하기
                if (dishRepository.findByName(dishDto.getName()) == null) {
                    dishRepository.save(dishDto.toDish());
                }
                dishDtos.add(dishDto);
            }

            // 오늘 날짜의 Dish를 만들었으면, 이걸 기준으로 Menu를 만든다.
            if (dishDtos.size() > 0) {
                menuService.createMenuWithDishes(dishDtos);
            }
        }
    }

    public String fetchDishInfoToString(Integer page) throws Exception {
        StringBuilder rawMenu = new StringBuilder();

        String apiUrl = DISH_URL + "?page=" + page+"&AUTH_KEY="+DISH_KEY;

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

            reader.close();
            rawMenu.append(response);
        }

    return rawMenu.toString();
    }

    @Transactional
    public void updateMainDish(List<MainDishRequestDto> mainDishRequestDtos) {

        for (MainDishRequestDto mainDishRequestDto : mainDishRequestDtos) {
            List<String> mainDishNames = mainDishRequestDto.getMainDishList();

            for (String mainDishName : mainDishNames) {
                Dish dish = dishRepository.findByName(mainDishName);
                dish.updateMainDish();
            }

            for (String sideDishName : mainDishRequestDto.getSideDishList()) {
                Dish sideDish = dishRepository.findByName(sideDishName);
                sideDish.updateSideDish();
            }
        }
    }

    public boolean checkSecretKey(String authKey) {
        return DISH_KEY.equals(authKey);
    }
}
