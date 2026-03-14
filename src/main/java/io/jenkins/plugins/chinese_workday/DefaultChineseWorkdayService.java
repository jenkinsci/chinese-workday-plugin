package io.jenkins.plugins.chinese_workday;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

final class DefaultChineseWorkdayService implements ChineseWorkdayService {

    private final ChineseWorkdayCalendarRepository repository;

    DefaultChineseWorkdayService() {
        this(new ChineseWorkdayCalendarRepository());
    }

    DefaultChineseWorkdayService(ChineseWorkdayCalendarRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean isWorkday(LocalDate date, ZoneId zoneId) {
        Map<Integer, HolidaySchedule> schedules = repository.loadSchedules();
        HolidaySchedule schedule = schedules.get(date.getYear());
        if (schedule == null) {
            throw new IllegalArgumentException(unsupportedYearMessage(date.getYear()));
        }
        return ChineseWorkdayScheduleEvaluator.isWorkday(date, schedule);
    }

    List<Integer> supportedYears() {
        return repository.supportedYears();
    }

    String supportedYearsDisplay() {
        return String.join(", ", supportedYears().stream().map(String::valueOf).toList());
    }

    String unsupportedYearMessage(int year) {
        return "No Chinese holiday calendar is available for "
                + year
                + ". Supported years: "
                + supportedYearsDisplay()
                + ". Add year "
                + year
                + " under Manage Jenkins > System > Chinese Workday.";
    }
}
