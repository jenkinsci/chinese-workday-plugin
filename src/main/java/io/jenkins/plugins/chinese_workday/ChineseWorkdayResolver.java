package io.jenkins.plugins.chinese_workday;

import hudson.AbortException;
import hudson.Util;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;

final class ChineseWorkdayResolver {

    static final String DEFAULT_TIME_ZONE = "Asia/Shanghai";
    static final ZoneId DEFAULT_ZONE_ID = ZoneId.of(DEFAULT_TIME_ZONE);

    private ChineseWorkdayResolver() {}

    static LocalDate resolveDate(String date) throws AbortException {
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
