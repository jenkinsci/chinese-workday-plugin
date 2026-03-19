package io.jenkins.plugins.chinese_workday.monitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class UpcomingCalendarYearMonitorTest {

    @Test
    void doesNotActivateBeforeDecember() {
        UpcomingCalendarYearMonitor monitor =
                new TestUpcomingCalendarYearMonitor(LocalDate.of(2026, 11, 30), List.of(2020, 2021, 2022, 2023));

        assertFalse(monitor.isActivated());
    }

    @Test
    void activatesInDecemberWhenNextYearIsMissing() {
        UpcomingCalendarYearMonitor monitor = new TestUpcomingCalendarYearMonitor(
                LocalDate.of(2026, 12, 1), List.of(2020, 2021, 2022, 2023, 2024, 2025, 2026));

        assertTrue(monitor.isActivated());
        assertEquals(2027, monitor.getTargetYear());
    }

    @Test
    void doesNotActivateWhenNextYearIsAlreadyAvailable() {
        UpcomingCalendarYearMonitor monitor = new TestUpcomingCalendarYearMonitor(
                LocalDate.of(2026, 12, 1), List.of(2020, 2021, 2022, 2023, 2024, 2025, 2026, 2027));

        assertFalse(monitor.isActivated());
    }

    @Test
    void doesNotActivateOutsideDecemberEvenIfFollowingYearIsMissing() {
        UpcomingCalendarYearMonitor monitor =
                new TestUpcomingCalendarYearMonitor(LocalDate.of(2027, 1, 1), List.of(2020, 2021, 2022, 2023, 2027));

        assertFalse(monitor.isActivated());
    }

    private static final class TestUpcomingCalendarYearMonitor extends UpcomingCalendarYearMonitor {

        private final LocalDate currentDate;
        private final List<Integer> years;

        private TestUpcomingCalendarYearMonitor(LocalDate currentDate, List<Integer> years) {
            this.currentDate = currentDate;
            this.years = years;
        }

        @Override
        protected LocalDate today() {
            return currentDate;
        }

        @Override
        protected List<Integer> supportedYears() {
            return years;
        }
    }
}
