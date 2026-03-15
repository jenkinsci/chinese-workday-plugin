package io.jenkins.plugins.chinese_workday.model;

import java.time.LocalDate;
import java.util.Set;

public record HolidaySchedule(Set<LocalDate> holidays, Set<LocalDate> makeUpWorkdays) {}
