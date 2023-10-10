package com.SeeAndYouGo.SeeAndYouGo.Menu;

import lombok.RequiredArgsConstructor;
import net.bytebuddy.asm.Advice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final RestaurantRepository restaurantRepository;

    public List<Menu> getOneDayRestaurantMenu(String placeName, String date) {
        String parseRestaurantName = parseRestaurantName(placeName);
        Restaurant restaurant = restaurantRepository.findbyNameAndDate(parseRestaurantName, date);
        return menuRepository.findMenusByNameAndDate(restaurant.getId(), date);
    }

    public List<Menu>[] getOneWeekRestaurantMenu(String placeName, String date) {
        // 날짜 문자열을 파싱
        LocalDate parsedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 해당 주(week)의 시작 날짜와 끝 날짜 계산
        LocalDate startOfWeek = parsedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = parsedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        List<Menu>[] weekMenuList = new List[5]; // mon to fri

        int idx = 0;
        for(LocalDate i = startOfWeek; i.compareTo(endOfWeek) <= 0; i.plusDays(1)){
            weekMenuList[idx++] = getOneDayRestaurantMenu(placeName, i.toString());
        }

        return weekMenuList;
    }

    public String parseRestaurantName(String name) {
        if (name.contains("1")) return "1학생회관";
        else if (name.contains("2")) return "2학생회관";
        else if (name.contains("3")) return "3학생회관";
        else if (name.contains("4")) return "상록회관";
        else if (name.contains("5")) return "생활과학대";
        return name;
    }
}
