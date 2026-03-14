package io.jenkins.plugins.chinese_workday;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExternalHolidayCalendarLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void discoversAndLoadsExternalYears() throws Exception {
        Files.writeString(
                tempDir.resolve("2027.properties"),
                "holidays=2027-01-01,2027-10-01..2027-10-03\nmakeUpWorkdays=2027-09-26\n");
        Files.writeString(tempDir.resolve("notes.txt"), "ignored");

        ExternalHolidayCalendarLoader loader = new ExternalHolidayCalendarLoader(tempDir);

        assertEquals(List.of(2027), loader.discoverYears());

        Map<Integer, HolidaySchedule> schedules = loader.loadAll();
        HolidaySchedule schedule = schedules.get(2027);
        assertTrue(schedule.holidays().contains(LocalDate.of(2027, 10, 2)));
        assertTrue(schedule.makeUpWorkdays().contains(LocalDate.of(2027, 9, 26)));
    }

    @Test
    void rejectsDatesOutsideFileYear() throws Exception {
        Files.writeString(tempDir.resolve("2027.properties"), "holidays=2028-01-01\nmakeUpWorkdays=\n");

        ExternalHolidayCalendarLoader loader = new ExternalHolidayCalendarLoader(tempDir);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, loader::loadAll);
        assertTrue(exception.getMessage().contains("2027.properties"));
        assertTrue(exception.getMessage().contains("does not belong to year 2027"));
    }

    @Test
    void rejectsConflictingHolidayAndMakeUpWorkday() throws Exception {
        Files.writeString(tempDir.resolve("2027.properties"), "holidays=2027-10-01\nmakeUpWorkdays=2027-10-01\n");

        ExternalHolidayCalendarLoader loader = new ExternalHolidayCalendarLoader(tempDir);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, loader::loadAll);
        assertTrue(exception.getMessage().contains("both holidays and makeUpWorkdays"));
    }
}
