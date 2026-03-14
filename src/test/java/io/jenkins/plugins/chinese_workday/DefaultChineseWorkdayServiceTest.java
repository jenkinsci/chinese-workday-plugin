package io.jenkins.plugins.chinese_workday;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class DefaultChineseWorkdayServiceTest {

    @Test
    void loadsFutureYearFromSystemConfigurationCalendars() {
        DefaultChineseWorkdayService service = service(calendar(2027, """
                holidays=2027-01-01,2027-10-01..2027-10-03
                makeUpWorkdays=2027-09-26
                """));

        assertFalse(service.isWorkday(LocalDate.of(2027, 10, 2)));
        assertTrue(service.isWorkday(LocalDate.of(2027, 9, 26)));
        assertEquals(List.of(2025, 2026, 2027), service.supportedYears());
    }

    @Test
    void configuredCalendarOverridesBundledYear() {
        DefaultChineseWorkdayService service = service(calendar(2026, """
                holidays=2026-02-22
                makeUpWorkdays=
                """));

        assertFalse(service.isWorkday(LocalDate.of(2026, 2, 22)));
    }

    @Test
    void unsupportedYearMessageIncludesSystemConfigurationHint() {
        DefaultChineseWorkdayService service = service();

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> service.isWorkday(LocalDate.of(2030, 1, 1)));

        assertTrue(exception.getMessage().contains("No Chinese holiday calendar is available for 2030."));
        assertTrue(exception.getMessage().contains("Manage Jenkins > System > Chinese Workday"));
    }

    private DefaultChineseWorkdayService service(ConfiguredHolidayCalendar... calendars) {
        return new DefaultChineseWorkdayService(new ChineseWorkdayCalendarRepository(List.of(calendars)));
    }

    private ConfiguredHolidayCalendar calendar(int year, String content) {
        ConfiguredHolidayCalendar calendar = new ConfiguredHolidayCalendar(String.valueOf(year));
        for (String line : content.strip().split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("holidays=")) {
                calendar.setHolidays(trimmed.substring("holidays=".length()));
            } else if (trimmed.startsWith("makeUpWorkdays=")) {
                calendar.setMakeUpWorkdays(trimmed.substring("makeUpWorkdays=".length()));
            }
        }
        return calendar;
    }
}
