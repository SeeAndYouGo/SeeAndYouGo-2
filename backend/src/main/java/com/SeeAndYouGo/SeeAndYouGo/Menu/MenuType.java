package com.SeeAndYouGo.SeeAndYouGo.Menu;

import java.time.LocalTime;

public enum MenuType {
    BREAKFAST, LUNCH, DINNER;

    public static MenuType changeStringToMenuType(String name) {
        if(name.contains("조식")) return MenuType.BREAKFAST;
        else if(name.contains("중식")) return MenuType.LUNCH;
        else return MenuType.DINNER;
    }

    /**
     * timeStr에 맞는 MenuType을 return.
     * @param timeStr 형식은 08:15나 08:15:22 와 같은 형태여야한다.
     */
    public static MenuType resolveToMenuType(String timeStr) {
        LocalTime time = LocalTime.parse(timeStr);

        return resolveToMenuType(time);
    }

    public static MenuType resolveToMenuType(LocalTime time) {

        if(time.isBefore(LocalTime.of(11, 0, 0))){
            return BREAKFAST;
        }else if(time.isBefore(LocalTime.of(15, 0, 0))){
            return LUNCH;
        }else return DINNER;
    }
}
