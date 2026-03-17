package io.jenkins.plugins.chinese_workday.entry.freestyle;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import io.jenkins.plugins.chinese_workday.config.ChineseWorkdayGlobalConfiguration;
import io.jenkins.plugins.chinese_workday.model.ConfiguredHolidayCalendar;
import java.util.List;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class ChineseWorkdayBuilderTest {

    @Test
    void configRoundTrip(JenkinsRule jenkins) throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        ChineseWorkdayBuilder builder = new ChineseWorkdayBuilder();
        builder.setDate("2026-01-28");
        builder.setFailOnNonWorkday(true);
        project.getBuildersList().add(builder);

        project = jenkins.configRoundtrip(project);

        ChineseWorkdayBuilder reloaded =
                (ChineseWorkdayBuilder) project.getBuildersList().get(0);
        jenkins.assertEqualDataBoundBeans(builder, reloaded);
    }

    @Test
    void freestyleBuildLogsPlaceholderResult(JenkinsRule jenkins) throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        ChineseWorkdayBuilder builder = new ChineseWorkdayBuilder();
        builder.setDate("2025-01-26");
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);

        jenkins.assertLogContains("Date: 2025-01-26", build);
        jenkins.assertLogContains("Time zone: Asia/Shanghai", build);
        jenkins.assertLogContains("Workday: true", build);
        jenkins.assertLogContains("Holiday: false", build);
    }

    @Test
    void freestyleBuildFailsForUnsupportedYear(JenkinsRule jenkins) throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        ChineseWorkdayBuilder builder = new ChineseWorkdayBuilder();
        builder.setDate("2030-01-01");
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));

        jenkins.assertLogContains("No Chinese holiday calendar is available for 2030.", build);
        jenkins.assertLogContains("Supported years: 2020, 2021, 2022, 2023, 2024, 2025, 2026", build);
        jenkins.assertLogContains("Manage Jenkins > System > Chinese Workday", build);
    }

    @Test
    void freestyleBuildFailsOnNonWorkdayWhenConfigured(JenkinsRule jenkins) throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        ChineseWorkdayBuilder builder = new ChineseWorkdayBuilder();
        builder.setDate("2025-10-03");
        builder.setFailOnNonWorkday(true);
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));

        jenkins.assertLogContains("Workday: false", build);
        jenkins.assertLogContains("Holiday: true", build);
        jenkins.assertLogContains("Resolved date '2025-10-03' is not a Chinese workday", build);
    }

    @Test
    void scriptedPipelineUsesSymbol(JenkinsRule jenkins) throws Exception {
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
        String pipelineScript = "chineseWorkday date: '2025-10-03'";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));

        WorkflowRun build = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));

        jenkins.assertLogContains("Chinese Workday check", build);
        jenkins.assertLogContains("Workday: false", build);
    }

    @Test
    void isChineseWorkdayPipelineStepReturnsBooleanResult(JenkinsRule jenkins) throws Exception {
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-pipeline-step");
        String pipelineScript = """
                def result = isChineseWorkday date: '2025-10-03'
                echo "workday=${result}"
                """;
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));

        WorkflowRun build = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));

        jenkins.assertLogContains("workday=false", build);
    }

    @Test
    void isChineseHolidayPipelineStepReturnsBooleanResult(JenkinsRule jenkins) throws Exception {
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-holiday-pipeline-step");
        String pipelineScript = """
                def result = isChineseHoliday date: '2025-10-03'
                echo "holiday=${result}"
                """;
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));

        WorkflowRun build = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));

        jenkins.assertLogContains("holiday=true", build);
    }

    @Test
    void isChineseWorkdayPipelineStepUsesAsiaShanghaiByDefault(JenkinsRule jenkins) throws Exception {
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-default-timezone-step");
        String pipelineScript = """
                def result = isChineseWorkday date: '2025-10-03'
                echo "workday=${result}"
                """;
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));

        WorkflowRun build = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));

        jenkins.assertLogContains("workday=false", build);
    }

    @Test
    void supportedYearsPipelineStepReturnsBundledYears(JenkinsRule jenkins) throws Exception {
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-supported-years-step");
        String pipelineScript = """
                def years = chineseWorkdaySupportedYears()
                echo "years=${years.join(',')}"
                """;
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));

        WorkflowRun build = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));

        jenkins.assertLogContains("years=2020,2021,2022,2023,2024,2025,2026", build);
    }

    @Test
    void supportedYearsPipelineStepIncludesConfiguredYears(JenkinsRule jenkins) throws Exception {
        configureGlobalCalendar(2027, "2027-10-01..2027-10-03", "2027-09-26");

        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-supported-years-step-with-configured");
        String pipelineScript = """
                def years = chineseWorkdaySupportedYears()
                echo "years=${years.join(',')}"
                """;
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));

        WorkflowRun build = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));

        jenkins.assertLogContains("years=2020,2021,2022,2023,2024,2025,2026,2027", build);
    }

    @Test
    void pipelineStepReadsConfiguredCalendarYear(JenkinsRule jenkins) throws Exception {
        configureGlobalCalendar(2027, "2027-10-01..2027-10-03", "2027-09-26");

        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-configured-year-step");
        String pipelineScript = """
                echo "workday=${isChineseWorkday(date: '2027-10-02')}"
                echo "makeUp=${isChineseWorkday(date: '2027-09-26')}"
                """;
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));

        WorkflowRun build = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));

        jenkins.assertLogContains("workday=false", build);
        jenkins.assertLogContains("makeUp=true", build);
    }

    private void configureGlobalCalendar(int year, String holidays, String makeUpWorkdays) {
        ChineseWorkdayGlobalConfiguration configuration = ChineseWorkdayGlobalConfiguration.get();
        ConfiguredHolidayCalendar calendar = new ConfiguredHolidayCalendar(String.valueOf(year));
        calendar.setHolidays(holidays);
        calendar.setMakeUpWorkdays(makeUpWorkdays);
        configuration.setCalendars(List.of(calendar));
        configuration.save();
    }
}
