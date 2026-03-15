package io.jenkins.plugins.chinese_workday.support;

import hudson.AbortException;
import hudson.Util;
import io.jenkins.plugins.chinese_workday.Messages;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;

public final class ChineseWorkdayResolver {

    public static final String DEFAULT_TIME_ZONE = "Asia/Shanghai";
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of(DEFAULT_TIME_ZONE);

    private ChineseWorkdayResolver() {}

    public static LocalDate resolveDate(String date) throws AbortException {
        String trimmedDate = Util.fixEmptyAndTrim(date);
        if (trimmedDate == null) {
            return LocalDate.now(DEFAULT_ZONE_ID);
        }
        try {
            return LocalDate.parse(trimmedDate);
        } catch (DateTimeException ex) {
            throw new AbortException(Messages.ChineseWorkdayBuilder_errors_invalidDate(trimmedDate));
        }
    }
}
