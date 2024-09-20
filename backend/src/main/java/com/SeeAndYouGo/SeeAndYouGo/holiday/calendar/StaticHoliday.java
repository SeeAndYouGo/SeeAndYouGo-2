package com.SeeAndYouGo.SeeAndYouGo.holiday.calendar;

import lombok.Getter;

@Getter
public enum StaticHoliday {
    NEW_YEAR_DAY(CalendarType.SUN, 1, 1, false),             //신정
    THREE_ONE_DAY(CalendarType.SUN, 3, 1, true),             //삼일절 - 대체 공휴일: 토요일, 일요일
    CHILDREN_DAY(CalendarType.SUN, 5, 5, true),              //어린이날 - 대체 공휴일: 토요일, 일요일
    MEMORIAL_DAY(CalendarType.SUN, 6, 6, false),             //현충일
    NATIONAL_LIBERATION_DAY(CalendarType.SUN, 8, 15, true),  //광복절 - 대체 공휴일: 토요일, 일요일
    NATIONAL_FOUNDATION_DAY(CalendarType.SUN, 10, 3, true),  //개천절 - 대체 공휴일: 토요일, 일요일
    HANGUL_DAY(CalendarType.SUN, 10, 9, true),               //한글날 - 대체 공휴일: 토요일, 일요일
    CHRISTMAS_DAY(CalendarType.SUN, 12, 25, false),          //크리스마스
    MOON_NEW_YEAR_DAY(CalendarType.MOON, 1, 1, true),        //설날 - 대체 공휴일: 일요일
    BUDDHA_COMING_DAY(CalendarType.MOON, 4, 8, false),       //부처님 오신날
    THANKSGIVING_DAY(CalendarType.MOON, 8, 15, true);        //추석 - 대체 공휴일: 일요일

    private CalendarType calendarType;
    private int month;
    private int day;
    private boolean subHoliday; //대체 휴일 여부

    StaticHoliday(CalendarType calendarType, int month, int day, boolean subHoliday) {
        this.calendarType = calendarType;
        this.month = month;
        this.day = day;
        this.subHoliday = subHoliday;
    }

    public boolean isSunTypeCalendar() {
        return this.calendarType == CalendarType.SUN;
    }
}
