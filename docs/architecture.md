# Architecture Guide

This document explains the runtime structure of the Chinese Workday plugin and how holiday data
flows from different sources into a final workday decision.

It focuses on stable design rules and the current implementation shape. Specific classes or
integration details may evolve as the plugin gains new features.

## Goals

The plugin provides Jenkins-native Chinese workday checks for:

- Freestyle jobs
- Pipeline steps
- Jenkins global configuration

It focuses on Chinese statutory holidays, make-up workdays, and a predictable fallback in the
`Asia/Shanghai` time zone.

## Core concepts

- `HolidaySchedule`: the resolved calendar for one year, containing `holidays` and
  `makeUpWorkdays`
- `ConfiguredHolidayCalendar`: the administrator-facing model used by Jenkins system configuration
- `workday`: a day that should be treated as a working day in China
- `holiday`: a day that should be treated as a non-working day in China
- `make-up workday`: a weekend day that should still be treated as a working day

## Runtime architecture

The high-level runtime path is:

```text
Jenkins entry
  -> support
  -> service
  -> repository
  -> parser / model
  -> final workday decision
```

The current implementation can be summarized as:

```text
Freestyle / Pipeline / Global config
  -> ChineseWorkdayResolver
  -> DefaultChineseWorkdayService
  -> ChineseWorkdayCalendarRepository
  -> Bundled / External / Configured loaders
  -> HolidayCalendarParser
  -> HolidaySchedule
  -> ChineseWorkdayScheduleEvaluator
```

## Package responsibilities

Base package: `src/main/java/io/jenkins/plugins/chinese_workday/`

- `entry/`: Jenkins-facing entry points such as Freestyle builders and Pipeline steps
- `config/`: Jenkins global configuration integration
- `model/`: shared data models
- `parser/`: parsing and validation of calendar definitions
- `repository/`: calendar discovery, loading, and merge orchestration
- `service/`: workday evaluation and service-level behavior
- `support/`: shared helpers such as date resolution

## Entry points

The plugin currently exposes these main entry points:

- `entry/freestyle/ChineseWorkdayBuilder`: Freestyle build step
- `entry/pipeline/ChineseWorkdayCheckStep`: `isChineseWorkday(...)`
- `entry/pipeline/ChineseHolidayCheckStep`: `isChineseHoliday(...)`
- `entry/pipeline/ChineseWorkdaySupportedYearsStep`: `chineseWorkdaySupportedYears()`
- `config/ChineseWorkdayGlobalConfiguration`: system configuration UI

These entry classes are intentionally thin. They mainly:

- accept Jenkins input
- resolve the target date
- call the service layer
- convert failures into Jenkins-friendly errors or logs

## Service layer

`DefaultChineseWorkdayService` is the main runtime service.

Its job is to:

- ask the repository for the effective calendars
- find the calendar for the requested year
- fail clearly when the year is unsupported
- delegate the final decision to `ChineseWorkdayScheduleEvaluator`

The service layer is responsible for turning a resolved date plus an effective yearly calendar into
the final boolean result.

## Repository layer

`ChineseWorkdayCalendarRepository` builds the final year-to-calendar map.

It merges three sources in this order:

1. bundled resource files
2. external file overrides
3. Jenkins system configuration overrides

This means the effective precedence is:

```text
bundled < external file < system config
```

At present, the repository layer is the single place where this merge policy is enforced.

## Data sources

### Bundled resources

Bundled calendars live under:

`src/main/resources/io/jenkins/plugins/chinese_workday/calendars/`

These are versioned with the plugin and are the default source for supported years.

### External files

For compatibility, the plugin can also load file-based overrides from:

`$JENKINS_HOME/chinese-workday/calendars/`

This path remains supported, but it is no longer the preferred administrative workflow.

### Jenkins system configuration

Administrators can define or override years in:

`Manage Jenkins -> System -> Chinese Workday`

This is the preferred override mechanism because it avoids direct server-side file edits and has the
highest precedence.

## Parsing and validation

`HolidayCalendarParser` converts properties data into `HolidaySchedule`.

It validates:

- ISO date format
- date ranges using `..`
- year consistency for every date
- conflicts between `holidays` and `makeUpWorkdays`

`ConfiguredHolidayCalendar` performs additional normalization for Jenkins form input, such as
converting comma-separated or multi-line values into a canonical internal format.

## Workday decision rules

`ChineseWorkdayScheduleEvaluator` applies the final decision in this order:

1. if the date is in `makeUpWorkdays`, return `true`
2. if the date is in `holidays`, return `false`
3. otherwise, use weekday/weekend fallback:
   - Monday to Friday -> `true`
   - Saturday and Sunday -> `false`

This ordering is important because Chinese make-up workdays can fall on weekends.

## Time zone behavior

When the caller does not pass a date, `ChineseWorkdayResolver` uses the current date in
`Asia/Shanghai`.

The plugin currently does not expose a custom time zone parameter because the business rule is tied
to Chinese holiday schedules.

## Unsupported years

If no effective calendar exists for a requested year, the plugin fails explicitly instead of
guessing.

This is intentional:

- it avoids ambiguous behavior for future years
- it prevents accidental misuse of generic weekend-only logic
- it pushes administrators toward adding a temporary override until official data is available

## Representative request flow

Example: `isChineseWorkday(date: '2025-10-03')`

1. Pipeline calls `ChineseWorkdayCheckStep`
2. the step resolves the string date to `LocalDate`
3. `DefaultChineseWorkdayService` requests effective calendars
4. the repository builds the merged schedule map
5. the service picks the `2025` schedule
6. the evaluator sees that `2025-10-03` is a holiday
7. the final result is `false`

This example illustrates the current flow. Future enhancements may add intermediate caching,
additional service methods, or more entry points without changing the core decision rules.

## Extension and maintenance guidance

When extending the plugin:

- put new Jenkins-facing APIs under `entry/`
- keep business decisions in `service/`
- keep merge policy in `repository/`
- keep parsing and input validation in `parser/` and `model/`
- preserve the precedence rule `bundled < external file < system config`

For annual holiday data updates, see `docs/calendar-maintenance.en.md`.
