package io.jenkins.plugins.chinese_workday;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

final class BundledChineseWorkdayService implements ChineseWorkdayService {

    private static final List<Integer> SUPPORTED_YEARS = BundledHolidayCalendarLoader.discoverYears();
    private static final Map<Integer, HolidaySchedule> SCHEDULES =
            BundledHolidayCalendarLoader.load(SUPPORTED_YEARS);

    @Override
    public boolean isWorkday(LocalDate date, ZoneId zoneId) {
        HolidaySchedule schedule = SCHEDULES.get(date.getYear());
        if (schedule == null) {
            throw new IllegalArgumentException(
                    "No bundled Chinese holiday calendar is available for "
                            + date.getYear()
                            + ". Supported years: "
                            + supportedYearsDisplay()
                            + ".");
        }
        return ChineseWorkdayScheduleEvaluator.isWorkday(date, schedule);
    }

    String supportedYearsDisplay() {
        return String.join(", ", SUPPORTED_YEARS.stream().map(String::valueOf).toList());
    }

    java.util.List<Integer> supportedYears() {
        return SUPPORTED_YEARS;
    }
}
