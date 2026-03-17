package io.jenkins.plugins.chinese_workday.model;

import hudson.Extension;
import hudson.Util;
import hudson.model.Describable;
import hudson.model.Descriptor;
import io.jenkins.plugins.chinese_workday.parser.HolidayCalendarParser;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class ConfiguredHolidayCalendar implements Describable<ConfiguredHolidayCalendar> {

    private final String year;
    private String holidays = "";
    private String makeUpWorkdays = "";

    @DataBoundConstructor
    public ConfiguredHolidayCalendar(String year) {
        this.year = Util.fixNull(year).trim();
    }

    public String getYear() {
        return year;
    }

    public String getHolidays() {
        return holidays;
    }

    @DataBoundSetter
    public void setHolidays(String holidays) {
        this.holidays = normalizeDateList(holidays);
    }

    public String getMakeUpWorkdays() {
        return makeUpWorkdays;
    }

    @DataBoundSetter
    public void setMakeUpWorkdays(String makeUpWorkdays) {
        this.makeUpWorkdays = normalizeDateList(makeUpWorkdays);
    }

    public boolean isBlank() {
        return year.isBlank() && holidays.isBlank() && makeUpWorkdays.isBlank();
    }

    public int resolveYear() {
        return parseYear(year);
    }

    public HolidaySchedule toSchedule() {
        int resolvedYear = resolveYear();
        Properties properties = new Properties();
        if (!holidays.isBlank()) {
            properties.setProperty("holidays", holidays);
        }
        if (!makeUpWorkdays.isBlank()) {
            properties.setProperty("makeUpWorkdays", makeUpWorkdays);
        }
        return HolidayCalendarParser.parse(resolvedYear, properties, sourceDescription());
    }

    String sourceDescription() {
        return "Jenkins system configuration for Chinese workday year " + (year.isBlank() ? "(blank)" : year) + ".";
    }

    static int parseYear(String value) {
        String trimmedValue = Util.fixEmptyAndTrim(value);
        if (trimmedValue == null) {
            throw new IllegalArgumentException("Year is required.");
        }
        if (!trimmedValue.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("Year must contain digits only.");
        }
        int parsedYear;
        try {
            parsedYear = Integer.parseInt(trimmedValue);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Year is out of range.", ex);
        }
        if (parsedYear < 1900 || parsedYear > 9999) {
            throw new IllegalArgumentException("Year must be between 1900 and 9999.");
        }
        return parsedYear;
    }

    public static void validateDateList(String rawValue, String fieldName) {
        parseDateList(rawValue, fieldName);
    }

    public static void validateCalendarEntry(String year, String holidays, String makeUpWorkdays) {
        Set<LocalDate> holidayDates = parseDateList(holidays, "holidays");
        Set<LocalDate> makeUpWorkdayDates = parseDateList(makeUpWorkdays, "makeUpWorkdays");
        validateConflicts(holidayDates, makeUpWorkdayDates, sourceDescription(year));

        String trimmedYear = Util.fixEmptyAndTrim(year);
        if (trimmedYear == null) {
            return;
        }

        int resolvedYear;
        try {
            resolvedYear = parseYear(trimmedYear);
        } catch (IllegalArgumentException ex) {
            return;
        }

        Properties properties = new Properties();
        String normalizedHolidays = normalizeDateList(holidays);
        String normalizedMakeUpWorkdays = normalizeDateList(makeUpWorkdays);
        if (!normalizedHolidays.isBlank()) {
            properties.setProperty("holidays", normalizedHolidays);
        }
        if (!normalizedMakeUpWorkdays.isBlank()) {
            properties.setProperty("makeUpWorkdays", normalizedMakeUpWorkdays);
        }
        HolidayCalendarParser.parse(resolvedYear, properties, sourceDescription(trimmedYear));
    }

    public static String normalizeDateList(String value) {
        String trimmedValue = Util.fixEmptyAndTrim(value);
        if (trimmedValue == null) {
            return "";
        }
        return Arrays.stream(trimmedValue.split("[\\r\\n,]+"))
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .collect(Collectors.joining(","));
    }

    private static Set<LocalDate> parseDateList(String rawValue, String fieldName) {
        String normalizedValue = normalizeDateList(rawValue);
        if (normalizedValue.isBlank()) {
            return Set.of();
        }

        Set<LocalDate> dates = new LinkedHashSet<>();
        for (String token : normalizedValue.split(",")) {
            if (token.contains("..")) {
                addDateRange(token, fieldName, dates);
            } else {
                dates.add(parseDateToken(token, fieldName));
            }
        }
        return Set.copyOf(dates);
    }

    private static void addDateRange(String value, String fieldName, Set<LocalDate> dates) {
        String[] bounds = value.split("\\.\\.");
        if (bounds.length != 2) {
            throw new IllegalArgumentException("Invalid date range '" + value + "' for " + fieldName + ".");
        }

        LocalDate start = parseDateToken(bounds[0].trim(), fieldName);
        LocalDate end = parseDateToken(bounds[1].trim(), fieldName);
        if (end.isBefore(start)) {
            throw new IllegalArgumentException(
                    "Invalid date range '" + value + "' for " + fieldName + ": end date is before start date.");
        }

        LocalDate current = start;
        while (!current.isAfter(end)) {
            dates.add(current);
            current = current.plusDays(1);
        }
    }

    private static void validateConflicts(
            Set<LocalDate> holidays, Set<LocalDate> makeUpWorkdays, String sourceDescription) {
        Set<LocalDate> conflicts = new LinkedHashSet<>(holidays);
        conflicts.retainAll(makeUpWorkdays);
        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException("Conflicting dates in "
                    + sourceDescription
                    + ": "
                    + conflicts
                    + " appear in both holidays and makeUpWorkdays.");
        }
    }

    private static String sourceDescription(String year) {
        String trimmedYear = Util.fixEmptyAndTrim(year);
        return "Jenkins system configuration for Chinese workday year "
                + (trimmedYear == null ? "(blank)" : trimmedYear)
                + ".";
    }

    private static LocalDate parseDateToken(String value, String fieldName) {
        try {
            return LocalDate.parse(value);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Invalid date '" + value + "' for " + fieldName + ".", ex);
        }
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<ConfiguredHolidayCalendar> {

        @Override
        public String getDisplayName() {
            return "";
        }
    }
}
