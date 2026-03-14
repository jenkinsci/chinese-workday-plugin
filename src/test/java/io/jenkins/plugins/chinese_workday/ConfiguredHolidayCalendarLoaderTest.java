package io.jenkins.plugins.chinese_workday;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ConfiguredHolidayCalendarLoaderTest {

    @Test
    void discoversAndLoadsConfiguredYears() {
        ConfiguredHolidayCalendar calendar = new ConfiguredHolidayCalendar("2027");
        calendar.setHolidays("2027-01-01\n2027-10-01..2027-10-03");
        calendar.setMakeUpWorkdays("2027-09-26");

        ConfiguredHolidayCalendarLoader loader = new ConfiguredHolidayCalendarLoader(List.of(calendar));

        assertEquals(List.of(2027), loader.discoverYears());

        Map<Integer, HolidaySchedule> schedules = loader.loadAll();
        HolidaySchedule schedule = schedules.get(2027);
        assertTrue(schedule.holidays().contains(LocalDate.of(2027, 10, 2)));
        assertTrue(schedule.makeUpWorkdays().contains(LocalDate.of(2027, 9, 26)));
    }

    @Test
    void rejectsDatesOutsideConfiguredYear() {
        ConfiguredHolidayCalendar calendar = new ConfiguredHolidayCalendar("2027");
        calendar.setHolidays("2028-01-01");

        ConfiguredHolidayCalendarLoader loader = new ConfiguredHolidayCalendarLoader(List.of(calendar));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, loader::loadAll);
        assertTrue(exception.getMessage().contains("Chinese workday year 2027"));
        assertTrue(exception.getMessage().contains("does not belong to year 2027"));
    }

    @Test
    void rejectsDuplicateConfiguredYears() {
        ConfiguredHolidayCalendar first = new ConfiguredHolidayCalendar("2027");
        ConfiguredHolidayCalendar second = new ConfiguredHolidayCalendar("2027");

        ConfiguredHolidayCalendarLoader loader = new ConfiguredHolidayCalendarLoader(List.of(first, second));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, loader::loadAll);
        assertTrue(exception.getMessage().contains("Duplicate Chinese workday calendar configuration for year 2027"));
    }
}
