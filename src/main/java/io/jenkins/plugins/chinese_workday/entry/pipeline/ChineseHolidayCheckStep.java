package io.jenkins.plugins.chinese_workday.entry.pipeline;

import hudson.AbortException;
import hudson.Extension;
import hudson.Util;
import io.jenkins.plugins.chinese_workday.Messages;
import io.jenkins.plugins.chinese_workday.service.DefaultChineseWorkdayService;
import io.jenkins.plugins.chinese_workday.support.ChineseWorkdayResolver;
import io.jenkins.plugins.chinese_workday.support.ChineseWorkdayStepSupport;
import java.io.Serial;
import java.time.LocalDate;
import java.util.Set;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

public class ChineseHolidayCheckStep extends Step {

    private String date = "";

    @DataBoundConstructor
    public ChineseHolidayCheckStep() {}

    public String getDate() {
        return date;
    }

    @org.kohsuke.stapler.DataBoundSetter
    public void setDate(String date) {
        this.date = Util.fixNull(date).trim();
    }

    @Override
    public StepExecution start(StepContext context) {
        return new Execution(context, this);
    }

    static final class Execution extends SynchronousStepExecution<Boolean> {

        @Serial
        private static final long serialVersionUID = 1L;

        private final transient ChineseHolidayCheckStep step;

        Execution(StepContext context, ChineseHolidayCheckStep step) {
            super(context);
            this.step = step;
        }

        @Override
        protected Boolean run() throws Exception {
            LocalDate resolvedDate = ChineseWorkdayResolver.resolveDate(step.getDate());
            return ChineseWorkdayStepSupport.abortOnIllegalArgument(
                    () -> new DefaultChineseWorkdayService().isHoliday(resolvedDate));
        }
    }

    @Extension
    public static final class DescriptorImpl extends StepDescriptor {

        @Override
        public String getFunctionName() {
            return "isChineseHoliday";
        }

        @Override
        public String getDisplayName() {
            return Messages.ChineseHolidayCheckStep_DescriptorImpl_DisplayName();
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Set.of();
        }
    }
}
