package com.SeeAndYouGo.SeeAndYouGo.global;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static java.time.DayOfWeek.MONDAY;

/**
 * 날짜 관련 유틸리티 메서드를 제공하는 클래스.
 */
public final class DateUtils {

    private DateUtils() {
        // 유틸리티 클래스이므로 인스턴스화 방지
    }

    /**
     * 주어진 날짜 기준으로 해당 주의 월요일을 반환한다.
     */
    public static LocalDate getNearestMonday(LocalDate date) {
        return date.with(MONDAY);
    }

    /**
     * 주어진 월요일 기준으로 해당 주의 일요일을 반환한다.
     * @param monday 월요일 날짜
     * @return 해당 주 일요일
     * @throws IllegalArgumentException 입력이 월요일이 아닌 경우
     */
    public static LocalDate getSundayOfWeek(LocalDate monday) {
        if (!monday.getDayOfWeek().equals(MONDAY)) {
            throw new IllegalArgumentException("월요일을 입력받아야 합니다.");
        }
        return monday.plusDays(6);
    }

    /**
     * 주어진 날짜 기준으로 해당 주 또는 다음 주의 금요일을 반환한다.
     */
    public static LocalDate getFridayOfWeek(LocalDate inputDate) {
        DayOfWeek dayOfWeek = inputDate.getDayOfWeek();
        int daysUntilFriday = DayOfWeek.FRIDAY.getValue() - dayOfWeek.getValue()
                + (dayOfWeek.getValue() >= DayOfWeek.FRIDAY.getValue() ? 7 : 0);
        return inputDate.plusDays(daysUntilFriday);
    }
}
