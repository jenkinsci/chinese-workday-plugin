# Chinese Workday Plugin

[中文说明](README_ZH.md)

## Status

This repository is in active development.

The plugin already provides a usable Chinese workday implementation for Jenkins jobs and Pipelines.

Current capabilities include:

- bundled Chinese holiday calendars for `2025` and `2026`
- freestyle build step support
- Pipeline steps for `isWorkday(...)`, `isHoliday(...)`, and `chineseWorkdaySupportedYears()`
- Jenkins system configuration for adding or overriding year-specific holiday calendars

The feature set is still intentionally focused, and more years plus related integrations will be
added over time.

## Installation

You can install the plugin in either of these ways.

### Option 1: Install from the Jenkins Plugin Center

If the plugin is available in your Jenkins update center:

1. Open `Manage Jenkins -> Plugins`
2. Search for `Chinese Workday`
3. Select the plugin and install it
4. Restart Jenkins if prompted

### Option 2: Manually upload an `hpi` package

First build the plugin locally:

```bash
mvn package
```

After the build completes, upload the generated `.hpi` from the `target/` directory in Jenkins:

1. Open `Manage Jenkins -> Plugins`
2. Open the `Advanced settings` section
3. In `Deploy Plugin`, choose the generated `.hpi` file
4. Upload the plugin and restart Jenkins if prompted

For local development, you can also run a test Jenkins with the plugin preloaded:

```bash
mvn hpi:run
```

## Planned scope

The goal of this plugin is to provide Jenkins features related to Chinese workday rules, such as:

- determining whether a date is a workday
- handling official holidays
- handling adjusted working days
- supporting usage in Jenkins jobs and pipelines

Current implementation notes:

- bundled calendars are currently available for `2025` and `2026`
- bundled calendars are derived from official State Council holiday notices
- administrators can add future years or override bundled years in `Manage Jenkins -> System -> Chinese Workday`
- supported years are discovered from bundled calendar resources
- supported years return `true` for workdays and `false` for non-workdays
- unsupported years fail with an error instead of returning an ambiguous result
- all date evaluation uses the fixed time zone `Asia/Shanghai`
- Pipeline can use `isWorkday(...)` to get a boolean result
- Pipeline can use `isHoliday(...)` to get a boolean result
- Pipeline can use `chineseWorkdaySupportedYears()` to query bundled and custom years

The exact feature set is still being refined.

## Usage

### Freestyle job

Add the build step `Chinese Workday Check`.

Available fields:

- `Date`: optional, ISO format `yyyy-MM-dd`; blank means "today" in `Asia/Shanghai`
- `Fail build on non-workday`: optional; if enabled, the build step fails on non-workdays so later
  Freestyle build steps do not run

Example:

- `Date`: `2025-10-03`

Example build log output:

```text
Chinese Workday check
Date: 2025-10-03
Time zone: Asia/Shanghai
Workday: false
Holiday: true
```

### Pipeline: check result

Use `isWorkday(...)` when you want a boolean result in Pipeline. `date` is optional; when omitted
it uses the current date in `Asia/Shanghai`. The step does not expose a separate time zone
parameter.

Scripted or Declarative `script` example:

```groovy
def result = isWorkday(date: '2025-10-03')

echo "workday=${result}"

if (!result) {
    echo 'Skip release actions on non-workdays.'
}
```

Return value semantics:

- `true`: the date is a Chinese workday
- `false`: the date is a Chinese non-workday
- unsupported year: the step fails with an error

Example using the default date:

```groovy
def todayIsWorkday = isWorkday()
echo "todayIsWorkday=${todayIsWorkday}"
```

### Pipeline: holiday result

Use `isHoliday(...)` when you want a boolean "non-workday" result. `date` is optional; when omitted
it uses the current date in `Asia/Shanghai`. The step does not expose a separate time zone
parameter.

```groovy
def holiday = isHoliday(date: '2025-10-03')
echo "holiday=${holiday}"
```

Return value semantics:

- `true`: the date is a Chinese holiday / non-workday
- `false`: the date is a Chinese workday
- unsupported year: the step fails with an error

### Pipeline: list supported years

Use `chineseWorkdaySupportedYears()` to query which bundled and custom calendars are available.

```groovy
def years = chineseWorkdaySupportedYears()
echo "supportedYears=${years.join(',')}"
```

### Pipeline: builder-style step

The plugin also exposes the builder-style step `chineseWorkday(...)`, which writes the result to the
build log.

```groovy
node {
    chineseWorkday(
        date: '2025-10-03'
    )
}
```

This form is useful when you want log output instead of a returned boolean.

## System configuration

Administrators can add or override holiday calendars in `Manage Jenkins -> System -> Chinese Workday`.

Recommended steps:

1. Open `Manage Jenkins -> System`
2. Find the `Chinese Workday` section
3. Click `Add calendar`
4. Fill in the target year plus holiday and make-up workday dates
5. Save the system configuration

Each calendar entry includes:

- `Year`: the calendar year, for example `2027`
- `Holidays`: ISO dates or date ranges using `..`
- `Make-up workdays`: ISO dates or date ranges using `..` that should be treated as workdays even
  if they fall on weekends

Input format rules:

- dates must use ISO format `yyyy-MM-dd`
- date ranges use `..`, for example `2027-10-01..2027-10-07`
- separate dates with commas or new lines
- every date in a `2027` entry must belong to year `2027`
- a date cannot appear in both `Holidays` and `Make-up workdays`
- a configured year overrides the bundled calendar for the same year

Example entry:

```text
Year: 2027
Holidays:
2027-01-01
2027-02-10..2027-02-16
2027-04-05
2027-10-01..2027-10-07

Make-up workdays:
2027-02-07
2027-02-20
2027-09-26
```

After saving the system configuration, the next build or Pipeline step uses the new calendar data.

For compatibility, the plugin still reads optional file-based overrides from
`$JENKINS_HOME/chinese-workday/calendars/`, and system configuration entries take precedence over
those files.

## Holiday data maintenance

Bundled holiday data should be updated through a documented review process rather than ad hoc file
edits.

- source of truth: annual State Council holiday notices
- bundled resource files should keep a `# Source: ...` comment
- new bundled years should update both the year file and `index.properties`
- new or changed bundled data should include automated test updates

See `docs/calendar-maintenance.md` for the maintenance checklist and validation flow.

## Development

Environment requirements:

- JDK `17+`
- Maven `3.9.6+`

These constraints come from the Jenkins plugin parent pom used by this repository.

Common commands:

```bash
mvn test
mvn package
mvn hpi:run
```

Notes:

- `mvn test` runs Jenkins plugin tests
- `mvn package` builds the plugin artifact
- `mvn hpi:run` starts a local Jenkins instance for manual testing

## Repository notes

- `src/main/java/` contains plugin implementation code
- `src/main/resources/` contains Jelly views, help files, and localization resources
- `src/test/java/` contains automated tests

## Contributing

Contributions are welcome once the initial plugin direction is stabilized.

Until then, prefer small, incremental changes that help replace the starter template with real
plugin behavior.

Refer to the Jenkins community contribution guidelines:

- https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md

## License

Licensed under MIT. See `LICENSE.md`.
