package io.jenkins.plugins.chinese_workday.parser;

import io.jenkins.plugins.chinese_workday.model.HolidaySchedule;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

public final class HolidayCalendarParser {

    private HolidayCalendarParser() {}

    public static HolidaySchedule parse(int year, Properties properties, String sourceDescription) {
        Set<LocalDate> holidays = parseDateSet(year, properties.getProperty("holidays"), "holidays", sourceDescription);
        Set<LocalDate> makeUpWorkdays =
                parseDateSet(year, properties.getProperty("makeUpWorkdays"), "makeUpWorkdays", sourceDescription);

        Set<LocalDate> conflicts = new LinkedHashSet<>(holidays);
        conflicts.retainAll(makeUpWorkdays);
        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException("Conflicting dates in "
                    + sourceDescription
                    + ": "
                    + conflicts
                    + " appear in both holidays and makeUpWorkdays.");
        }

        return new HolidaySchedule(holidays, makeUpWorkdays);
    }

    private static Set<LocalDate> parseDateSet(int year, String rawValue, String fieldName, String sourceDescription) {
        if (rawValue == null || rawValue.isBlank()) {
            return Set.of();
        }
        Set<LocalDate> dates = new LinkedHashSet<>();
        for (String token : rawValue.split(",")) {
            String value = token.trim();
            if (value.isEmpty()) {
                continue;
            }
            if (value.contains("..")) {
                parseDateRange(year, value, fieldName, sourceDescription, dates);
            } else {
                dates.add(parseDate(year, value, fieldName, sourceDescription));
            }
        }
        return Set.copyOf(dates);
    }

    private static void parseDateRange(
            int year, String value, String fieldName, String sourceDescription, Set<LocalDate> dates) {
        String[] bounds = value.split("\\.\\.");
        if (bounds.length != 2) {
            throw new IllegalArgumentException(
                    "Invalid date range '" + value + "' for " + fieldName + " in " + sourceDescription + ".");
        }

        LocalDate start = parseDate(year, bounds[0].trim(), fieldName, sourceDescription);
        LocalDate end = parseDate(year, bounds[1].trim(), fieldName, sourceDescription);
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Invalid date range '"
                    + value
                    + "' for "
                    + fieldName
                    + " in "
                    + sourceDescription
                    + ": end date is before start date.");
        }

        LocalDate current = start;
        while (!current.isAfter(end)) {
            dates.add(current);
            current = current.plusDays(1);
        }
    }

    private static LocalDate parseDate(int year, String value, String fieldName, String sourceDescription) {
        LocalDate date;
        try {
            date = LocalDate.parse(value);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException(
                    "Invalid date '" + value + "' for " + fieldName + " in " + sourceDescription + ".", ex);
        }

        if (date.getYear() != year) {
            throw new IllegalArgumentException(
                    "Date " + date + " in " + sourceDescription + " does not belong to year " + year + ".");
        }
        return date;
    }
}
