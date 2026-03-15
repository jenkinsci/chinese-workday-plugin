package io.jenkins.plugins.chinese_workday.service;

import java.time.LocalDate;

public interface ChineseWorkdayService {

    boolean isWorkday(LocalDate date);

    default boolean isHoliday(LocalDate date) {
        return !isWorkday(date);
    }
}
