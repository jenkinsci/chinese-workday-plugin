# Calendar Maintenance

[中文](calendar-maintenance.md)

This document describes how to add or update bundled Chinese holiday calendars.

## Data Source Policy

- Primary source: the State Council General Office annual holiday notice
- Preferred publication channel: official State Council or government web pages
- If an official page changes location, keep the final canonical source link in the calendar file
- Temporary Jenkins-side overrides may be configured in `Manage Jenkins -> System -> Chinese Workday`, but bundled data in this repository should still be updated through code review

## Update Checklist

When adding a new bundled year such as `2027`:

1. Find the official holiday notice for that year
2. Confirm all holiday ranges and make-up workdays from the notice
3. Add `src/main/resources/io/jenkins/plugins/chinese_workday/calendars/2027.properties`
4. Add a `# Source: ...` comment at the top of the file
5. Update `src/main/resources/io/jenkins/plugins/chinese_workday/calendars/index.properties`
6. Add or update automated tests that cover representative holidays and make-up workdays
7. Run local validation commands
8. Include the source link and validation result in the pull request description

## File Format

Example:

```properties
# Source: https://www.gov.cn/example-notice
holidays=\
  2027-01-01,\
  2027-02-10..2027-02-16,\
  2027-10-01..2027-10-07
makeUpWorkdays=\
  2027-02-07,\
  2027-02-20,\
  2027-09-26
```

Rules:

- use ISO dates in `yyyy-MM-dd`
- use `..` for ranges
- every date in `2027.properties` must belong to `2027`
- a date cannot appear in both `holidays` and `makeUpWorkdays`
- keep entries readable; multi-line values are preferred for bundled data files

## Validation Flow

Recommended local commands:

```bash
mvn -Dtest=CalendarResourceIntegrityTest,ChineseWorkdayServiceTest surefire:test
mvn test
```

What to verify:

- bundled resource files match `index.properties`
- each bundled file parses successfully
- each bundled file contains a recorded source comment
- representative service tests still pass for existing years

## Release Note

If a new year is not yet bundled in a released plugin version, administrators can use the Jenkins system configuration page as a temporary workaround until the next plugin release is published.
