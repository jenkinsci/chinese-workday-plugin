package io.jenkins.plugins.chinese_workday;

import hudson.AbortException;
import hudson.Util;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;

final class ChineseWorkdayResolver {

    private ChineseWorkdayResolver() {}

    static LocalDate resolveDate(String date, ZoneId zoneId) throws AbortException {
        String trimmedDate = Util.fixEmptyAndTrim(date);
        if (trimmedDate == null) {
            return LocalDate.now(zoneId);
        }
        try {
            return LocalDate.parse(trimmedDate);
        } catch (DateTimeException ex) {
            throw new AbortException(Messages.ChineseWorkdayBuilder_errors_invalidDate(trimmedDate));
        }
    }

    static ZoneId resolveTimeZone(String timeZone) throws AbortException {
        String resolvedTimeZone = defaultTimeZone(timeZone);
        try {
            return ZoneId.of(resolvedTimeZone);
        } catch (DateTimeException ex) {
            throw new AbortException(Messages.ChineseWorkdayBuilder_errors_invalidTimeZone(resolvedTimeZone));
        }
    }

    static String defaultTimeZone(String value) {
        String trimmed = Util.fixEmptyAndTrim(value);
        return trimmed == null ? ChineseWorkdayBuilder.DEFAULT_TIME_ZONE : trimmed;
    }
}
