package com.SeeAndYouGo.SeeAndYouGo.holiday;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Holiday {

    @Id
    private LocalDate date;
    private boolean isHoliday;

    public Holiday(LocalDate date, boolean isHoliday) {
        this.date = date;
        this.isHoliday = isHoliday;
    }
}
