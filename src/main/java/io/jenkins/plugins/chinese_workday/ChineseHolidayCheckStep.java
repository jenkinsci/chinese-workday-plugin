package io.jenkins.plugins.chinese_workday;

import hudson.AbortException;
import hudson.Extension;
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

    private final String date;

    @DataBoundConstructor
    public ChineseHolidayCheckStep(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
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
            try {
                return new DefaultChineseWorkdayService().isHoliday(resolvedDate);
            } catch (IllegalArgumentException ex) {
                throw new AbortException(ex.getMessage());
            }
        }
    }

    @Extension
    public static final class DescriptorImpl extends StepDescriptor {

        @Override
        public String getFunctionName() {
            return "isHoliday";
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
