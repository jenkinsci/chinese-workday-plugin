package io.jenkins.plugins.chinese_workday;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChineseWorkdayServiceTest {

    private final ChineseWorkdayService service = new BundledChineseWorkdayService();

    @Test
    void discoversSupportedYearsFromBundledCalendars() {
        assertEquals(List.of(2025, 2026), BundledHolidayCalendarLoader.discoverYears());
    }

    @Test
    void loadsBundledHolidayCalendarFromResources() {
        assertFalse(service.isWorkday(LocalDate.of(2026, 2, 18), ZoneId.of("Asia/Shanghai")));
    }

    @Test
    void returnsWorkdayForMakeUpSundayIn2025Schedule() {
        assertTrue(service.isWorkday(LocalDate.of(2025, 1, 26), ZoneId.of("Asia/Shanghai")));
    }

    @Test
    void returnsNonWorkdayForOfficialHolidayIn2025Schedule() {
        assertFalse(service.isWorkday(LocalDate.of(2025, 10, 3), ZoneId.of("Asia/Shanghai")));
    }

    @Test
    void returnsHolidayForOfficialHolidayIn2025Schedule() {
        assertTrue(service.isHoliday(LocalDate.of(2025, 10, 3), ZoneId.of("Asia/Shanghai")));
    }

    @Test
    void returnsFalseForHolidayOnRegularWorkday() {
        assertFalse(service.isHoliday(LocalDate.of(2025, 2, 5), ZoneId.of("Asia/Shanghai")));
    }

    @Test
    void returnsRegularWorkdayForNormalWeekday() {
        assertTrue(service.isWorkday(LocalDate.of(2025, 2, 5), ZoneId.of("Asia/Shanghai")));
    }

    @Test
    void throwsForUnsupportedYear() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.isWorkday(LocalDate.of(2030, 1, 1), ZoneId.of("Asia/Shanghai")));

        assertTrue(exception.getMessage().contains("No bundled Chinese holiday calendar is available for 2030."));
        assertTrue(exception.getMessage().contains("Supported years: 2025, 2026."));
    }
}
