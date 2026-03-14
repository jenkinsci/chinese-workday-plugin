package io.jenkins.plugins.chinese_workday;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class CalendarResourceIntegrityTest {

    @Test
    void bundledCalendarFilesMatchIndex() {
        assertEquals(BundledHolidayCalendarLoader.discoverYears(), indexedYears());
    }

    @Test
    void bundledCalendarFilesContainSourceCommentAndParse() throws Exception {
        for (Integer year : indexedYears()) {
            String resourcePath = "/io/jenkins/plugins/chinese_workday/calendars/" + year + ".properties";
            String content = readResource(resourcePath);

            assertTrue(content.contains("# Source:"), "Missing source comment for " + resourcePath);

            Properties properties = new Properties();
            try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(
                            CalendarResourceIntegrityTest.class.getResourceAsStream(resourcePath),
                            StandardCharsets.UTF_8))) {
                properties.load(reader);
            }

            HolidaySchedule schedule = HolidayCalendarParser.parse(year, properties, resourcePath);
            assertFalse(schedule.holidays().isEmpty(), "Expected holidays for " + resourcePath);
        }
    }

    private static List<Integer> indexedYears() {
        Properties properties = new Properties();
        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(
                        CalendarResourceIntegrityTest.class.getResourceAsStream(
                                "/io/jenkins/plugins/chinese_workday/calendars/index.properties"),
                        StandardCharsets.UTF_8))) {
            properties.load(reader);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load bundled calendar index.", ex);
        }

        String rawYears = properties.getProperty("years", "");
        return rawYears.isBlank()
                ? List.of()
                : java.util.Arrays.stream(rawYears.split(","))
                        .map(String::trim)
                        .filter(token -> !token.isEmpty())
                        .map(Integer::parseInt)
                        .sorted()
                        .toList();
    }

    private static String readResource(String resourcePath) throws Exception {
        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(
                        CalendarResourceIntegrityTest.class.getResourceAsStream(resourcePath),
                        StandardCharsets.UTF_8))) {
            return reader.lines().reduce("", (left, right) -> left + right + "\n");
        }
    }
}
