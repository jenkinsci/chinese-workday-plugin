package io.jenkins.plugins.chinese_workday.config;

import hudson.Extension;
import hudson.RelativePath;
import hudson.model.Descriptor.FormException;
import hudson.util.FormValidation;
import io.jenkins.plugins.chinese_workday.Messages;
import io.jenkins.plugins.chinese_workday.model.ConfiguredHolidayCalendar;
import io.jenkins.plugins.chinese_workday.repository.ConfiguredHolidayCalendarLoader;
import java.util.ArrayList;
import java.util.List;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest2;

@Extension
public class ChineseWorkdayGlobalConfiguration extends GlobalConfiguration {

    private List<ConfiguredHolidayCalendar> calendars = new ArrayList<>();

    public ChineseWorkdayGlobalConfiguration() {
        if (Jenkins.getInstanceOrNull() != null) {
            load();
        }
    }

    public static ChineseWorkdayGlobalConfiguration get() {
        ChineseWorkdayGlobalConfiguration configuration =
                GlobalConfiguration.all().get(ChineseWorkdayGlobalConfiguration.class);
        return configuration != null ? configuration : new ChineseWorkdayGlobalConfiguration();
    }

    @Override
    public String getDisplayName() {
        return Messages.ChineseWorkdayGlobalConfiguration_DisplayName();
    }

    public List<ConfiguredHolidayCalendar> getCalendars() {
        return List.copyOf(calendars);
    }

    @DataBoundSetter
    public void setCalendars(List<ConfiguredHolidayCalendar> calendars) {
        this.calendars = normalizeCalendars(calendars);
    }

    @Override
    public boolean configure(StaplerRequest2 request, JSONObject json) throws FormException {
        request.bindJSON(this, json);
        validateCalendars();
        save();
        return true;
    }

    public FormValidation doCheckYear(
            @QueryParameter String value,
            @RelativePath("..") @QueryParameter String holidays,
            @RelativePath("..") @QueryParameter String makeUpWorkdays) {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);
        return validateCalendarEntry(value, holidays, makeUpWorkdays);
    }

    public FormValidation doCheckHolidays(
            @QueryParameter String value,
            @RelativePath("..") @QueryParameter String year,
            @RelativePath("..") @QueryParameter String makeUpWorkdays) {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);
        return validateCalendarEntry(year, value, makeUpWorkdays);
    }

    public FormValidation doCheckMakeUpWorkdays(
            @QueryParameter String value,
            @RelativePath("..") @QueryParameter String year,
            @RelativePath("..") @QueryParameter String holidays) {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);
        return validateCalendarEntry(year, holidays, value);
    }

    private void validateCalendars() throws FormException {
        try {
            new ConfiguredHolidayCalendarLoader(calendars).loadAll();
        } catch (IllegalArgumentException ex) {
            throw new FormException(ex.getMessage(), "calendars");
        }
    }

    private static FormValidation validateCalendarEntry(String year, String holidays, String makeUpWorkdays) {
        try {
            ConfiguredHolidayCalendar.validateCalendarEntry(year, holidays, makeUpWorkdays);
            return FormValidation.ok();
        } catch (IllegalArgumentException ex) {
            return FormValidation.error(ex.getMessage());
        }
    }

    private static List<ConfiguredHolidayCalendar> normalizeCalendars(List<ConfiguredHolidayCalendar> calendars) {
        if (calendars == null) {
            return new ArrayList<>();
        }
        List<ConfiguredHolidayCalendar> normalizedCalendars = new ArrayList<>();
        for (ConfiguredHolidayCalendar calendar : calendars) {
            if (calendar == null || calendar.isBlank()) {
                continue;
            }
            normalizedCalendars.add(calendar);
        }
        return normalizedCalendars;
    }
}
