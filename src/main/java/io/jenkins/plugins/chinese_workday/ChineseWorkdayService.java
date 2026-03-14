package io.jenkins.plugins.chinese_workday;

import java.time.LocalDate;
import java.time.ZoneId;

public interface ChineseWorkdayService {

    boolean isWorkday(LocalDate date, ZoneId zoneId);

    default boolean isHoliday(LocalDate date, ZoneId zoneId) {
        return !isWorkday(date, zoneId);
    }
}
