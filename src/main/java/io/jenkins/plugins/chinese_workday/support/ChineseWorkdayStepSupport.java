package io.jenkins.plugins.chinese_workday.support;

import hudson.AbortException;

public final class ChineseWorkdayStepSupport {

    private ChineseWorkdayStepSupport() {}

    public static <T> T abortOnIllegalArgument(Computation<T> computation) throws AbortException {
        try {
            return computation.run();
        } catch (IllegalArgumentException ex) {
            throw new AbortException(ex.getMessage());
        }
    }

    @FunctionalInterface
    public interface Computation<T> {
        T run();
    }
}
