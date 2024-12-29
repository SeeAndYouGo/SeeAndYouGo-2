package com.SeeAndYouGo.SeeAndYouGo.menu;

public enum Dept {
    NOODLE, WESTERN, SNACK, KOREAN, JAPANESE, CHINESE,
    STUDENT, STAFF, SPECIAL,
    DORM_A, DORM_C;

    public static Dept changeStringToDept(String name){
        if(name.contains("학생")) return Dept.STUDENT;
        else if(name.contains("교직원")) return Dept.STAFF;
        else if(name.contains("메인A")) return DORM_A;
        else if(name.contains("메인C")) return DORM_C;
        else return Dept.SPECIAL;
    }

    public String getKoreanDept(){
        switch (this){
            case STAFF:
                return "교직원";
            case STUDENT:
                return "학생";
            case DORM_A:
                return "메인 A";
            case DORM_C:
                return "메인 B";
        }

        return "기타";
    }
}
