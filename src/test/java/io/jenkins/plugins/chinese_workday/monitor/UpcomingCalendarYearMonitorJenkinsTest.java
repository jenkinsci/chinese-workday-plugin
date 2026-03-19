package io.jenkins.plugins.chinese_workday.monitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.model.AdministrativeMonitor;
import java.time.LocalDate;
import java.util.List;
import jenkins.model.Jenkins;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class UpcomingCalendarYearMonitorJenkinsTest {

    @Test
    void monitorIsRegistered(JenkinsRule jenkins) {
        UpcomingCalendarYearMonitor monitor = AdministrativeMonitor.all().get(UpcomingCalendarYearMonitor.class);

        assertNotNull(monitor);
        assertEquals(Jenkins.MANAGE, monitor.getRequiredPermission());
    }

    @Test
    void activeMonitorAppearsOnManagePage(JenkinsRule jenkins) throws Exception {
        HtmlPage page = jenkins.createWebClient().goTo("manage");

        String text = page.asNormalizedText();
        assertTrue(text.contains("Chinese Workday calendar for 2027 is still missing."));
        assertTrue(text.contains("Manage Jenkins -> System -> Chinese Workday"));
        assertTrue(page.asXml().contains("jenkins-alert-warning"));
        assertTrue(page.asXml().contains("/manage/configure"));
    }

    @TestExtension("activeMonitorAppearsOnManagePage")
    public static final class ActivatedUpcomingCalendarYearMonitor extends UpcomingCalendarYearMonitor {

        @Override
        protected LocalDate today() {
            return LocalDate.of(2026, 12, 1);
        }

        @Override
        protected List<Integer> supportedYears() {
            return List.of(2020, 2021, 2022, 2023, 2024, 2025, 2026);
        }
    }
}
