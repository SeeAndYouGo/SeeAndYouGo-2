package com.SeeAndYouGo.SeeAndYouGo.Menu;

public enum Dept {
    STUDENT, STAFF, SPECIAL;

    public static Dept changeStringToDept(String name){
        if(name.contains("학생")) return Dept.STUDENT;
        else if(name.contains("교직원")) return Dept.STAFF;
        else return Dept.SPECIAL;
    }
}
