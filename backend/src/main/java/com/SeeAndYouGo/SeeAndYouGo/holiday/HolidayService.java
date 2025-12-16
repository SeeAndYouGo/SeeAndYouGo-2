package com.SeeAndYouGo.SeeAndYouGo.holiday;

import com.SeeAndYouGo.SeeAndYouGo.holiday.calendar.HolidayChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HolidayService {
    private final HolidayRepository holidayRepository;
    private final HolidayChecker holidayChecker;

    @Transactional
    public void saveThisYearHoliday(LocalDate date){
        int currentYear = date.getYear();

        try {
            List<LocalDate> holidayList = holidayChecker.getHolidayList(date);

            // 해당 연도의 첫 번째 날과 마지막 날 구하기
            LocalDate startDate = LocalDate.of(currentYear, 1, 1);
            LocalDate endDate = LocalDate.of(currentYear, 12, 31);

            List<Holiday> notHolidays = new ArrayList<>();

            // 모든 날짜를 도는 for문
            for (LocalDate tmpDate = startDate; !tmpDate.isAfter(endDate); tmpDate = tmpDate.plusDays(1)) {
                int value = tmpDate.getDayOfWeek().getValue();
                if(value == 6 || value == 7){
                    // 여기에 해당하는 값은 토요일, 일요일임.
                    notHolidays.add(new Holiday(tmpDate, true));
                }else{
                    notHolidays.add(new Holiday(tmpDate, false));
                }
            }

            holidayRepository.saveAll(notHolidays);

            List<Holiday> holidays = new ArrayList<>();

            for (LocalDate localDate : holidayList) {
                holidays.add(new Holiday(localDate, true));
            }

            holidayRepository.saveAll(holidays);

        } catch (IOException e) {
            log.error("Failed to save holiday data for year: {}", date.getYear(), e);
            throw new RuntimeException(e);
        }
    }

    public boolean isHoliday(LocalDate date) {
        Holiday referenceById = holidayRepository.getReferenceById(date);
        return referenceById.isHoliday();
    }
}
