package io.jenkins.plugins.chinese_workday;

import java.time.LocalDate;
import java.util.Set;

record HolidaySchedule(Set<LocalDate> holidays, Set<LocalDate> makeUpWorkdays) {}
