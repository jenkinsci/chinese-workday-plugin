package io.jenkins.plugins.chinese_workday.repository;

import io.jenkins.plugins.chinese_workday.model.HolidaySchedule;
import io.jenkins.plugins.chinese_workday.parser.HolidayCalendarParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

public final class ExternalHolidayCalendarLoader {

    private final Path calendarDirectory;

    public ExternalHolidayCalendarLoader(Path calendarDirectory) {
        this.calendarDirectory = calendarDirectory;
    }

    public Path calendarDirectory() {
        return calendarDirectory;
    }

    public List<Integer> discoverYears() {
        if (!Files.isDirectory(calendarDirectory)) {
            return List.of();
        }

        try (Stream<Path> stream = Files.list(calendarDirectory)) {
            return stream.filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.endsWith(".properties"))
                    .map(name -> name.substring(0, name.length() - ".properties".length()))
                    .filter(ExternalHolidayCalendarLoader::isYearToken)
                    .map(Integer::parseInt)
                    .sorted()
                    .toList();
        } catch (IOException ex) {
            throw new UncheckedIOException(
                    "Failed to scan external Chinese holiday calendars in " + calendarDirectory + ".", ex);
        }
    }

    public Map<Integer, HolidaySchedule> loadAll() {
        Map<Integer, HolidaySchedule> schedules = new LinkedHashMap<>();
        for (Integer year : discoverYears()) {
            schedules.put(year, loadYear(year));
        }
        return Map.copyOf(schedules);
    }

    private HolidaySchedule loadYear(int year) {
        Path file = calendarDirectory.resolve(year + ".properties");
        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(file)) {
            properties.load(inputStream);
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to load external Chinese holiday calendar: " + file + ".", ex);
        }
        return HolidayCalendarParser.parse(year, properties, file.toString());
    }

    private static boolean isYearToken(String token) {
        if (token.isBlank()) {
            return false;
        }
        for (int i = 0; i < token.length(); i++) {
            if (!Character.isDigit(token.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
