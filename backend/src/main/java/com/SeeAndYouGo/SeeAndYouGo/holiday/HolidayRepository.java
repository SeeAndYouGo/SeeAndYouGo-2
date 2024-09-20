package com.SeeAndYouGo.SeeAndYouGo.holiday;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface HolidayRepository extends JpaRepository<Holiday, LocalDate> {
}
