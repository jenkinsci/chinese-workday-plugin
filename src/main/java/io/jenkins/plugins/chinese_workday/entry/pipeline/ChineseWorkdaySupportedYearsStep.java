package io.jenkins.plugins.chinese_workday.entry.pipeline;

import hudson.Extension;
import io.jenkins.plugins.chinese_workday.Messages;
import io.jenkins.plugins.chinese_workday.service.DefaultChineseWorkdayService;
import java.io.Serial;
import java.util.List;
import java.util.Set;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

public class ChineseWorkdaySupportedYearsStep extends Step {

    @DataBoundConstructor
    public ChineseWorkdaySupportedYearsStep() {}

    @Override
    public StepExecution start(StepContext context) {
        return new Execution(context);
    }

    static final class Execution extends SynchronousStepExecution<List<Integer>> {

        @Serial
        private static final long serialVersionUID = 1L;

        Execution(StepContext context) {
            super(context);
        }

        @Override
        protected List<Integer> run() {
            return new DefaultChineseWorkdayService().supportedYears();
        }
    }

    @Extension
    public static final class DescriptorImpl extends StepDescriptor {

        @Override
        public String getFunctionName() {
            return "chineseWorkdaySupportedYears";
        }

        @Override
        public String getDisplayName() {
            return Messages.ChineseWorkdaySupportedYearsStep_DescriptorImpl_DisplayName();
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Set.of();
        }
    }
}
