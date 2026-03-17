package io.jenkins.plugins.chinese_workday.entry.freestyle;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import io.jenkins.plugins.chinese_workday.Messages;
import io.jenkins.plugins.chinese_workday.service.DefaultChineseWorkdayService;
import io.jenkins.plugins.chinese_workday.support.ChineseWorkdayResolver;
import io.jenkins.plugins.chinese_workday.support.ChineseWorkdayStepSupport;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDate;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

public class ChineseWorkdayBuilder extends Builder implements SimpleBuildStep {

    private String date = "";
    private boolean failOnNonWorkday;

    @DataBoundConstructor
    public ChineseWorkdayBuilder() {}

    public String getDate() {
        return date;
    }

    @DataBoundSetter
    public void setDate(String date) {
        this.date = Util.fixNull(date).trim();
    }

    public boolean isFailOnNonWorkday() {
        return failOnNonWorkday;
    }

    @DataBoundSetter
    public void setFailOnNonWorkday(boolean failOnNonWorkday) {
        this.failOnNonWorkday = failOnNonWorkday;
    }

    @Override
    public boolean requiresWorkspace() {
        return false;
    }

    @Override
    public void perform(Run<?, ?> run, EnvVars env, TaskListener listener) throws InterruptedException, IOException {
        LocalDate resolvedDate = ChineseWorkdayResolver.resolveDate(date);
        DefaultChineseWorkdayService service = new DefaultChineseWorkdayService();
        boolean workday = ChineseWorkdayStepSupport.abortOnIllegalArgument(() -> service.isWorkday(resolvedDate));
        boolean holiday = !workday;

        PrintStream logger = listener.getLogger();
        logger.println("Chinese Workday check");
        logger.println("Date: " + resolvedDate);
        logger.println("Time zone: " + ChineseWorkdayResolver.DEFAULT_TIME_ZONE);
        logger.println("Workday: " + workday);
        logger.println("Holiday: " + holiday);

        if (!workday && failOnNonWorkday) {
            throw new AbortException(Messages.ChineseWorkdayBuilder_errors_nonWorkdayDetected(resolvedDate.toString()));
        }
    }

    @Symbol("chineseWorkday")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @POST
        public FormValidation doCheckDate(@AncestorInPath Item item, @QueryParameter String value) {
            if (item != null) {
                item.checkPermission(Item.CONFIGURE);
            } else {
                Jenkins.get().checkPermission(Jenkins.MANAGE);
            }
            String trimmedValue = Util.fixEmptyAndTrim(value);
            if (trimmedValue == null) {
                return FormValidation.warning(Messages.ChineseWorkdayBuilder_DescriptorImpl_warnings_blankDate());
            }
            try {
                LocalDate.parse(trimmedValue);
                return FormValidation.ok();
            } catch (RuntimeException ex) {
                return FormValidation.error(Messages.ChineseWorkdayBuilder_DescriptorImpl_errors_invalidDate());
            }
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.ChineseWorkdayBuilder_DescriptorImpl_DisplayName();
        }
    }
}
