package com.SeeAndYouGo.SeeAndYouGo.Menu;

public enum Dept {
    STUDENT, STAFF, SPECIAL, NOODLE, WESTERN, SNACK, KOREAN, JAPANESE, CHINESE;

    public static Dept changeStringToDept(String name){
        if(name.contains("학생")) return Dept.STUDENT;
        else if(name.contains("교직원")) return Dept.STAFF;
        else return Dept.SPECIAL;
    }
}
