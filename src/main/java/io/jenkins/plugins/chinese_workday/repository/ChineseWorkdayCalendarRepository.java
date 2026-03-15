package io.jenkins.plugins.chinese_workday.repository;

import io.jenkins.plugins.chinese_workday.config.ChineseWorkdayGlobalConfiguration;
import io.jenkins.plugins.chinese_workday.model.ConfiguredHolidayCalendar;
import io.jenkins.plugins.chinese_workday.model.HolidaySchedule;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jenkins.model.Jenkins;

public final class ChineseWorkdayCalendarRepository {

    private static final String EXTERNAL_DIRECTORY = "chinese-workday/calendars";

    private final ExternalHolidayCalendarLoader externalLoader;
    private final ConfiguredHolidayCalendarLoader configuredLoader;

    public ChineseWorkdayCalendarRepository() {
        this(
                defaultExternalCalendarDirectory(),
                ChineseWorkdayGlobalConfiguration.get().getCalendars());
    }

    public ChineseWorkdayCalendarRepository(Path externalCalendarDirectory) {
        this(externalCalendarDirectory, List.of());
    }

    public ChineseWorkdayCalendarRepository(List<ConfiguredHolidayCalendar> configuredCalendars) {
        this(defaultExternalCalendarDirectory(), configuredCalendars);
    }

    public ChineseWorkdayCalendarRepository(
            Path externalCalendarDirectory, List<ConfiguredHolidayCalendar> configuredCalendars) {
        this.externalLoader = new ExternalHolidayCalendarLoader(externalCalendarDirectory);
        this.configuredLoader = new ConfiguredHolidayCalendarLoader(configuredCalendars);
    }

    public Path externalDirectory() {
        return externalLoader.calendarDirectory();
    }

    public List<Integer> supportedYears() {
        List<Integer> years = new ArrayList<>(loadSchedules().keySet());
        years.sort(Integer::compareTo);
        return List.copyOf(years);
    }

    public Map<Integer, HolidaySchedule> loadSchedules() {
        Map<Integer, HolidaySchedule> schedules = new LinkedHashMap<>();
        List<Integer> bundledYears = BundledHolidayCalendarLoader.discoverYears();
        schedules.putAll(BundledHolidayCalendarLoader.load(bundledYears));
        schedules.putAll(externalLoader.loadAll());
        schedules.putAll(configuredLoader.loadAll());

        List<Integer> sortedYears = new ArrayList<>(schedules.keySet());
        sortedYears.sort(Integer::compareTo);

        Map<Integer, HolidaySchedule> sortedSchedules = new LinkedHashMap<>();
        for (Integer year : sortedYears) {
            sortedSchedules.put(year, schedules.get(year));
        }
        return Map.copyOf(sortedSchedules);
    }

    private static Path defaultExternalCalendarDirectory() {
        Jenkins jenkins = Jenkins.getInstanceOrNull();
        Path root =
                jenkins != null ? jenkins.getRootDir().toPath() : Path.of(System.getProperty("user.home"), ".jenkins");
        return root.resolve(EXTERNAL_DIRECTORY);
    }
}
