package io.jenkins.plugins.chinese_workday.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.jenkins.plugins.chinese_workday.model.ConfiguredHolidayCalendar;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class ChineseWorkdayGlobalConfigurationRoundTripTest {

    @Test
    void globalConfigurationRoundTripPersistsCalendars(JenkinsRule jenkins) throws Exception {
        ChineseWorkdayGlobalConfiguration configuration = ChineseWorkdayGlobalConfiguration.get();
        ConfiguredHolidayCalendar calendar = new ConfiguredHolidayCalendar("2027");
        calendar.setHolidays("2027-01-01\n2027-10-01..2027-10-03");
        calendar.setMakeUpWorkdays("2027-09-26");
        configuration.setCalendars(List.of(calendar));
        configuration.save();

        jenkins.configRoundtrip();

        ChineseWorkdayGlobalConfiguration reloaded = ChineseWorkdayGlobalConfiguration.get();
        assertEquals(1, reloaded.getCalendars().size());

        ConfiguredHolidayCalendar reloadedCalendar = reloaded.getCalendars().get(0);
        assertEquals("2027", reloadedCalendar.getYear());
        assertEquals("2027-01-01,2027-10-01..2027-10-03", reloadedCalendar.getHolidays());
        assertEquals("2027-09-26", reloadedCalendar.getMakeUpWorkdays());
    }
}
