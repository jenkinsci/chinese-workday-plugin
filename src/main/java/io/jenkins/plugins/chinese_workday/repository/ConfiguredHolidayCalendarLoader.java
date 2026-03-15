package io.jenkins.plugins.chinese_workday.repository;

import io.jenkins.plugins.chinese_workday.model.ConfiguredHolidayCalendar;
import io.jenkins.plugins.chinese_workday.model.HolidaySchedule;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ConfiguredHolidayCalendarLoader {

    private final List<ConfiguredHolidayCalendar> calendars;

    public ConfiguredHolidayCalendarLoader(List<ConfiguredHolidayCalendar> calendars) {
        this.calendars = calendars == null ? List.of() : List.copyOf(calendars);
    }

    public List<Integer> discoverYears() {
        return loadAll().keySet().stream().sorted().toList();
    }

    public Map<Integer, HolidaySchedule> loadAll() {
        Map<Integer, HolidaySchedule> schedules = new LinkedHashMap<>();
        Set<Integer> discoveredYears = new LinkedHashSet<>();
        for (ConfiguredHolidayCalendar calendar : calendars) {
            if (calendar == null || calendar.isBlank()) {
                continue;
            }
            int year = calendar.resolveYear();
            if (!discoveredYears.add(year)) {
                throw new IllegalArgumentException(
                        "Duplicate Chinese workday calendar configuration for year " + year + ".");
            }
            schedules.put(year, calendar.toSchedule());
        }
        return Map.copyOf(schedules);
    }
}
