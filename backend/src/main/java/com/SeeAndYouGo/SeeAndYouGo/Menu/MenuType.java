package com.SeeAndYouGo.SeeAndYouGo.Menu;

public enum MenuType {
    BREAKFAST, LUNCH, DINNER;

    public static MenuType changeStringToMenuType(String name) {
        if(name.contains("조식")) return MenuType.BREAKFAST;
        else if(name.contains("중식")) return MenuType.LUNCH;
        else return MenuType.DINNER;
    }
}
