package com.SeeAndYouGo.SeeAndYouGo.global;

import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

/**
 * 애플리케이션 전역에서 사용하는 DateTimeFormatter 상수 모음
 */
public final class DateTimeFormatters {

    private DateTimeFormatters() {
        // 인스턴스 생성 방지
    }

    /**
     * 기본 날짜 포맷: yyyy-MM-dd
     * 예: 2024-01-15
     */
    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 엄격한 날짜 검증 포맷: uuuu-MM-dd (STRICT)
     * 존재하지 않는 날짜(예: 2월 30일) 입력 시 예외 발생
     */
    public static final DateTimeFormatter DATE_STRICT = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);

    /**
     * API용 압축 날짜 포맷: yyyyMMdd
     * 예: 20240115
     */
    public static final DateTimeFormatter DATE_COMPACT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 날짜+시간 포맷: yyyy-MM-dd HH:mm:ss
     * 예: 2024-01-15 14:30:00
     */
    public static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
}
