package io.jenkins.plugins.chinese_workday.monitor;

import hudson.Extension;
import hudson.model.AdministrativeMonitor;
import hudson.security.Permission;
import io.jenkins.plugins.chinese_workday.Messages;
import io.jenkins.plugins.chinese_workday.repository.ChineseWorkdayCalendarRepository;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.List;
import jenkins.model.Jenkins;

@Extension
public class UpcomingCalendarYearMonitor extends AdministrativeMonitor {

    static final ZoneId CHINA_ZONE_ID = ZoneId.of("Asia/Shanghai");

    @Override
    public boolean isActivated() {
        LocalDate currentDate = today();
        return isReminderWindow(currentDate) && !supportedYears().contains(targetYear(currentDate));
    }

    @Override
    public String getDisplayName() {
        return Messages.UpcomingCalendarYearMonitor_DisplayName();
    }

    @Override
    public Permission getRequiredPermission() {
        return Jenkins.MANAGE;
    }

    public int getTargetYear() {
        return targetYear(today());
    }

    public String getTargetYearText() {
        return Integer.toString(getTargetYear());
    }

    boolean isReminderWindow(LocalDate date) {
        return date.getMonth() == Month.DECEMBER;
    }

    protected LocalDate today() {
        return LocalDate.now(CHINA_ZONE_ID);
    }

    protected List<Integer> supportedYears() {
        return new ChineseWorkdayCalendarRepository().supportedYears();
    }

    private static int targetYear(LocalDate date) {
        return date.getYear() + 1;
    }
}
