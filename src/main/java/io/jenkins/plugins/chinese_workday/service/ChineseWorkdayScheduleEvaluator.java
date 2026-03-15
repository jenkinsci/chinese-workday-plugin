package io.jenkins.plugins.chinese_workday.service;

import io.jenkins.plugins.chinese_workday.model.HolidaySchedule;
import java.time.DayOfWeek;
import java.time.LocalDate;

final class ChineseWorkdayScheduleEvaluator {

    private ChineseWorkdayScheduleEvaluator() {}

    static boolean isWorkday(LocalDate date, HolidaySchedule schedule) {
        if (schedule.makeUpWorkdays().contains(date)) {
            return true;
        }
        if (schedule.holidays().contains(date)) {
            return false;
        }
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }
}
