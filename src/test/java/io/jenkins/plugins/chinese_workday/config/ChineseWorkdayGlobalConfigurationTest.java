package io.jenkins.plugins.chinese_workday.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import hudson.util.FormValidation;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class ChineseWorkdayGlobalConfigurationTest {

    @Test
    void validatesHolidayDateFormat(JenkinsRule jenkins) {
        ChineseWorkdayGlobalConfiguration configuration = ChineseWorkdayGlobalConfiguration.get();
        FormValidation validation = configuration.doCheckHolidays("2027-13-01", "2027", "");

        assertEquals(FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    void validatesMakeUpWorkdayDateRangeOrder(JenkinsRule jenkins) {
        ChineseWorkdayGlobalConfiguration configuration = ChineseWorkdayGlobalConfiguration.get();
        FormValidation validation = configuration.doCheckMakeUpWorkdays("2027-09-27..2027-09-26", "2027", "");

        assertEquals(FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    void validatesConflictBetweenHolidayAndMakeUpWorkday(JenkinsRule jenkins) {
        ChineseWorkdayGlobalConfiguration configuration = ChineseWorkdayGlobalConfiguration.get();
        FormValidation validation = configuration.doCheckMakeUpWorkdays("2027-10-01", "2027", "2027-10-01..2027-10-03");

        assertEquals(FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    void validatesYearAgainstConfiguredDates(JenkinsRule jenkins) {
        ChineseWorkdayGlobalConfiguration configuration = ChineseWorkdayGlobalConfiguration.get();
        FormValidation validation = configuration.doCheckYear("2027", "2028-01-01", "");

        assertEquals(FormValidation.Kind.ERROR, validation.kind);
    }
}
