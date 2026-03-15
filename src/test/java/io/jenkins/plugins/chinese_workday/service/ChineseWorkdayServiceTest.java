package io.jenkins.plugins.chinese_workday.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jenkins.plugins.chinese_workday.repository.BundledHolidayCalendarLoader;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChineseWorkdayServiceTest {

    private final ChineseWorkdayService service = new BundledChineseWorkdayService();

    @Test
    void discoversSupportedYearsFromBundledCalendars() {
        assertEquals(List.of(2020, 2021, 2022, 2023, 2024, 2025, 2026), BundledHolidayCalendarLoader.discoverYears());
    }

    @Test
    void returnsNonWorkdayForExtendedSpringFestivalWeekdayIn2020Schedule() {
        assertFalse(service.isWorkday(LocalDate.of(2020, 1, 31)));
    }

    @Test
    void returnsWorkdayForMakeUpSundayIn2021Schedule() {
        assertTrue(service.isWorkday(LocalDate.of(2021, 2, 7)));
    }

    @Test
    void returnsNonWorkdayForOfficialHolidayIn2022Schedule() {
        assertFalse(service.isWorkday(LocalDate.of(2022, 10, 4)));
    }

    @Test
    void returnsWorkdayForMakeUpSundayIn2023Schedule() {
        assertTrue(service.isWorkday(LocalDate.of(2023, 10, 8)));
    }

    @Test
    void returnsWorkdayForMakeUpSundayIn2024Schedule() {
        assertTrue(service.isWorkday(LocalDate.of(2024, 2, 18)));
    }

    @Test
    void loadsBundledHolidayCalendarFromResources() {
        assertFalse(service.isWorkday(LocalDate.of(2026, 2, 18)));
    }

    @Test
    void returnsWorkdayForMakeUpSundayIn2025Schedule() {
        assertTrue(service.isWorkday(LocalDate.of(2025, 1, 26)));
    }

    @Test
    void returnsNonWorkdayForOfficialHolidayIn2025Schedule() {
        assertFalse(service.isWorkday(LocalDate.of(2025, 10, 3)));
    }

    @Test
    void returnsHolidayForOfficialHolidayIn2025Schedule() {
        assertTrue(service.isHoliday(LocalDate.of(2025, 10, 3)));
    }

    @Test
    void returnsFalseForHolidayOnRegularWorkday() {
        assertFalse(service.isHoliday(LocalDate.of(2025, 2, 5)));
    }

    @Test
    void returnsRegularWorkdayForNormalWeekday() {
        assertTrue(service.isWorkday(LocalDate.of(2025, 2, 5)));
    }

    @Test
    void throwsForUnsupportedYear() {
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> service.isWorkday(LocalDate.of(2030, 1, 1)));

        assertTrue(exception.getMessage().contains("No bundled Chinese holiday calendar is available for 2030."));
        assertTrue(exception.getMessage().contains("Supported years: 2020, 2021, 2022, 2023, 2024, 2025, 2026."));
    }
}
