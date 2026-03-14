package io.jenkins.plugins.chinese_workday;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

final class BundledHolidayCalendarLoader {

    private static final String RESOURCE_DIRECTORY = "io/jenkins/plugins/chinese_workday/calendars";
    private static final String RESOURCE_PREFIX = "/" + RESOURCE_DIRECTORY + "/";
    private static final String INDEX_RESOURCE = RESOURCE_PREFIX + "index.properties";

    private BundledHolidayCalendarLoader() {}

    static List<Integer> discoverYears() {
        List<Integer> years = discoverYearsFromResources();
        if (!years.isEmpty()) {
            return years;
        }
        return discoverYearsFromIndex();
    }

    static Map<Integer, HolidaySchedule> load(Iterable<Integer> years) {
        Map<Integer, HolidaySchedule> schedules = new LinkedHashMap<>();
        for (Integer year : years) {
            schedules.put(year, loadYear(year));
        }
        return Map.copyOf(schedules);
    }

    private static List<Integer> discoverYearsFromResources() {
        URL resourceUrl = BundledHolidayCalendarLoader.class.getClassLoader().getResource(RESOURCE_DIRECTORY);
        if (resourceUrl == null) {
            return List.of();
        }
        try {
            return switch (resourceUrl.getProtocol()) {
                case "file" -> discoverYearsFromFileSystem(resourceUrl);
                case "jar" -> discoverYearsFromJar(resourceUrl);
                default -> List.of();
            };
        } catch (IOException | URISyntaxException ex) {
            throw new IllegalStateException("Failed to discover bundled holiday calendars.", ex);
        }
    }

    private static List<Integer> discoverYearsFromFileSystem(URL resourceUrl)
            throws IOException, URISyntaxException {
        try (Stream<Path> stream = Files.list(Path.of(resourceUrl.toURI()))) {
            return stream.map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.endsWith(".properties"))
                    .filter(name -> !"index.properties".equals(name))
                    .map(name -> name.substring(0, name.length() - ".properties".length()))
                    .map(Integer::parseInt)
                    .sorted()
                    .toList();
        }
    }

    private static List<Integer> discoverYearsFromJar(URL resourceUrl) throws IOException {
        JarURLConnection connection = (JarURLConnection) resourceUrl.openConnection();
        try (JarFile jarFile = connection.getJarFile()) {
            List<Integer> years = new ArrayList<>();
            String prefix = RESOURCE_DIRECTORY + "/";
            for (JarEntry entry : Collections.list(jarFile.entries())) {
                String name = entry.getName();
                if (!name.startsWith(prefix) || !name.endsWith(".properties")) {
                    continue;
                }
                String fileName = name.substring(prefix.length());
                if (fileName.contains("/") || "index.properties".equals(fileName)) {
                    continue;
                }
                years.add(Integer.parseInt(fileName.substring(0, fileName.length() - ".properties".length())));
            }
            Collections.sort(years);
            return List.copyOf(years);
        }
    }

    private static List<Integer> discoverYearsFromIndex() {
        Properties properties = loadProperties(INDEX_RESOURCE);
        String rawYears = properties.getProperty("years");
        if (rawYears == null || rawYears.isBlank()) {
            return List.of();
        }
        List<Integer> years = new ArrayList<>();
        for (String token : rawYears.split(",")) {
            years.add(Integer.parseInt(token.trim()));
        }
        Collections.sort(years);
        return List.copyOf(years);
    }

    private static HolidaySchedule loadYear(int year) {
        String resourcePath = RESOURCE_PREFIX + year + ".properties";
        Properties properties = loadProperties(resourcePath);
        return HolidayCalendarParser.parse(year, properties, resourcePath);
    }

    private static Properties loadProperties(String resourcePath) {
        Properties properties = new Properties();
        try (InputStream inputStream = BundledHolidayCalendarLoader.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing bundled holiday calendar resource: " + resourcePath);
            }
            properties.load(inputStream);
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to load bundled holiday calendar: " + resourcePath, ex);
        }
        return properties;
    }
}
