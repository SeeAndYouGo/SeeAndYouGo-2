package com.SeeAndYouGo.SeeAndYouGo.holiday.calendar;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HolidayChecker {

    public List<LocalDate> getHolidayList(LocalDate date) throws IOException {
        List<LocalDate> holidayList = new ArrayList<>();
        int year = date.getYear();

        StaticHoliday[] staticHolidays = StaticHoliday.values();
        for(StaticHoliday staticHoliday : staticHolidays) {
            if(staticHoliday.isSunTypeCalendar()) { //양력인 공휴일
                LocalDate localDate = LocalDate.of(year, staticHoliday.getMonth(), staticHoliday.getDay());

                //공휴일을 추가
                holidayList.add(localDate);

                //대체 공휴일이 적용되는 경우
                LocalDate subHoliday = calculateSubHoliday(staticHoliday, localDate);
                if(subHoliday != null) holidayList.add(subHoliday);

            } else { //음력인 공휴일
                LocalDate localDate = LocalDate.of(year, staticHoliday.getMonth(), staticHoliday.getDay());

                //석가탄신일인 경우
                if(staticHoliday == StaticHoliday.BUDDHA_COMING_DAY) {
                    LocalDate mainHoliday = transferToSunCalendarType(localDate);
                    holidayList.add(mainHoliday);
                    continue;
                }

                //설과 추석인 경우
                LocalDate mainHoliday = transferToSunCalendarType(localDate);
                LocalDate plusOneMainHoliday = mainHoliday.plusDays(1);
                LocalDate minusOneMainHoliday = mainHoliday.minusDays(1);

                //공휴일을 추가
                holidayList.add(mainHoliday);
                holidayList.add(plusOneMainHoliday);
                holidayList.add(minusOneMainHoliday);

                //대체 공휴일 계산
                LocalDate subHoliday = calcSubHoliday(mainHoliday, plusOneMainHoliday, minusOneMainHoliday);
                if(subHoliday != null) holidayList.add(subHoliday);
            }
        }

        return holidayList.stream().sorted().collect(Collectors.toList());
    }

    public LocalDate transferToSunCalendarType(LocalDate date) throws IOException {
        String year = String.valueOf(date.getYear());
        String month = String.valueOf(date.getMonthValue());
        month = month.length() == 1 ? "0"+ month : month;

        String day = String.valueOf(date.getDayOfMonth());
        day = day.length() == 1 ? "0"+ day : day;

        StringBuilder urlBuilder = new StringBuilder("https://apis.data.go.kr/B090041/openapi/service/LrsrCldInfoService/getSolCalInfo");

        urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=bS7JrrKeBuSJ3SdLK%2F5GaBp7lJXwaealtT%2FUiJt2DHqVVVEEUzS8DEiSvxud1uaGSKgBBCT7IsChi2LXSPF1PA%3D%3D"); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("lunYear","UTF-8") + "=" + URLEncoder.encode(year, "UTF-8")); /*연*/
        urlBuilder.append("&" + URLEncoder.encode("lunMonth","UTF-8") + "=" + URLEncoder.encode(month, "UTF-8")); /*월*/
        urlBuilder.append("&" + URLEncoder.encode("lunDay","UTF-8") + "=" + URLEncoder.encode(day, "UTF-8")); /*일*/
        urlBuilder.append("&" + URLEncoder.encode("_type", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8"));

        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        System.out.println("Response code: " + conn.getResponseCode());
        BufferedReader rd;
        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();
        System.out.println(sb);

        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(sb.toString()).getAsJsonObject();
        JsonObject item = jsonObject.getAsJsonObject("response")
                                    .getAsJsonObject("body")
                                    .getAsJsonObject("items")
                                    .getAsJsonObject("item");

        int lunYear = item.get("solYear").getAsInt();
        int lunMonth = item.get("solMonth").getAsInt();
        int lunDay = item.get("solDay").getAsInt();

        return LocalDate.of(lunYear, lunMonth, lunDay);
    }

    private LocalDate calculateSubHoliday(StaticHoliday staticHoliday, LocalDate localDate) {
        DayOfWeek dayOfWeek = localDate.getDayOfWeek();
        int value = dayOfWeek.getValue(); //1: Monday ~ 7: Sunday

        if(staticHoliday.isSubHoliday()) {
            //토요일인 경우 +2일을 더해서 다음주 월요일을 대체 휴일로 만든다.
            if(value == 6) return localDate.plusDays(2);

            //일요일인 경우 +1일을 더해서 다음주 월요일을 대체 휴일로 만든다.
            if(value == 7) return localDate.plusDays(1);
        }

        return null;
    }

    private LocalDate calcSubHoliday(LocalDate mainHoliday, LocalDate plusOneMainHoliday, LocalDate minusOneMainHoliday) {
        if(minusOneMainHoliday.getDayOfWeek().getValue() == 7)
            return minusOneMainHoliday.plusDays(3);

        if(mainHoliday.getDayOfWeek().getValue() == 7)
            return mainHoliday.plusDays(2);

        if(plusOneMainHoliday.getDayOfWeek().getValue() == 7)
            return plusOneMainHoliday.plusDays(1);

        return null;
    }
}
