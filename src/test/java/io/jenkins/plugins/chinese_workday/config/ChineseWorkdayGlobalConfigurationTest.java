package io.jenkins.plugins.chinese_workday.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import hudson.util.FormValidation;
import org.junit.jupiter.api.Test;

class ChineseWorkdayGlobalConfigurationTest {

    private final ChineseWorkdayGlobalConfiguration configuration = new ChineseWorkdayGlobalConfiguration();

    @Test
    void validatesHolidayDateFormat() {
        FormValidation validation = configuration.doCheckHolidays("2027-13-01", "2027", "");

        assertEquals(FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    void validatesMakeUpWorkdayDateRangeOrder() {
        FormValidation validation = configuration.doCheckMakeUpWorkdays("2027-09-27..2027-09-26", "2027", "");

        assertEquals(FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    void validatesConflictBetweenHolidayAndMakeUpWorkday() {
        FormValidation validation = configuration.doCheckMakeUpWorkdays("2027-10-01", "2027", "2027-10-01..2027-10-03");

        assertEquals(FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    void validatesYearAgainstConfiguredDates() {
        FormValidation validation = configuration.doCheckYear("2027", "2028-01-01", "");

        assertEquals(FormValidation.Kind.ERROR, validation.kind);
    }
}
