# Development Guide

This document describes the repository structure and local development workflow for the Chinese
Workday plugin.

## Environment requirements

- JDK `17+`
- Maven `3.9.6+`

These constraints come from the Jenkins plugin parent pom used by this repository.

## Code structure

The Java code is organized by responsibility so it is easier to follow the flow from Jenkins entry
points to holiday data loading and final date evaluation.

Base package: `src/main/java/io/jenkins/plugins/chinese_workday/`

- `entry/`: Jenkins entry points for Freestyle and Pipeline usage
- `config/`: Jenkins global configuration entry points
- `model/`: shared data models such as configured calendars and resolved holiday schedules
- `parser/`: parsing and validation of holiday calendar properties and date ranges
- `repository/`: loading and merging bundled, external, and system-configured calendars
- `service/`: workday decision logic and service orchestration
- `support/`: shared helpers such as date resolution in `Asia/Shanghai`

## Main runtime flow

1. Jenkins calls an entry class under `entry/`
2. The entry class resolves the target date with `ChineseWorkdayResolver`
3. `DefaultChineseWorkdayService` requests the effective calendars from
   `ChineseWorkdayCalendarRepository`
4. The repository merges data in this order: bundled resources, external files, then Jenkins
   system configuration
5. `ChineseWorkdayScheduleEvaluator` applies make-up workdays, holidays, and weekend fallback rules

## Common commands

```bash
mvn spotless:apply
mvn test
mvn package
mvn hpi:run
```

## Notes

- `mvn spotless:apply` formats Java and pom files according to repository rules
- `mvn test` runs Jenkins plugin tests
- `mvn package` builds the plugin artifact
- `mvn hpi:run` starts a local Jenkins instance for manual testing

## References

Key documents:

- `README.md` and `README_EN.md` for plugin overview, installation, and usage
- `docs/architecture.md` for runtime design, data flow, and core decisions
- `docs/calendar-maintenance.md` for bundled holiday data updates

Key directories:

- `src/main/java/` contains plugin implementation code
- `src/main/resources/` contains Jelly views, help files, and localization resources
- `src/test/java/` contains automated tests
