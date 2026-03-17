# Chinese Workday Plugin

[中文说明](README_ZH.md)

## Overview

The plugin provides Chinese workday checks for Jenkins Pipelines, Freestyle jobs, and
administrator-managed calendar overrides.

Current capabilities:

- bundled Chinese holiday calendars for `2020` through `2026`
- Pipeline steps for `isChineseWorkday(...)`, `isChineseHoliday(...)`, and `chineseWorkdaySupportedYears()`
- Freestyle build step support
- Jenkins system configuration for adding or overriding year-specific holiday calendars

## Installation

Choose one of these installation methods:

### Option 1: Install from the Jenkins Plugin Center

If the plugin is available in your Jenkins update center:

1. Open `Manage Jenkins -> Plugins`
2. Search for `Chinese Workday`
3. Select the plugin and install it
4. Restart Jenkins if prompted

If the plugin is not available in your update center yet, use the manual upload option below.

### Option 2: Manually upload an `hpi` package

First build the plugin locally:

```bash
mvn package
```

After the build completes, upload the generated `.hpi` from `target/` in Jenkins, for example
`target/chinese-workday.hpi`:

1. Open `Manage Jenkins -> Plugins`
2. Open the `Advanced settings` section
3. In `Deploy Plugin`, choose the generated `.hpi` file
4. Upload the plugin and restart Jenkins if prompted

## Quick start

Run a release stage only on a Chinese workday:

```groovy
pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                echo 'Build runs every day.'
            }
        }
        stage('Release') {
            when {
                expression {
                    isChineseWorkday()
                }
            }
            steps {
                echo 'Release runs only on a Chinese workday.'
            }
        }
    }
}
```

## Behavior summary

- default time zone: `Asia/Shanghai`
- data precedence: bundled resources < external files < Jenkins system configuration
- `isChineseWorkday(...)` and `isChineseHoliday(...)` return booleans
- unsupported years fail explicitly instead of silently falling back to weekend-only logic

## Usage

### Pipeline: step overview

- `isChineseWorkday(...)`: check whether today or a specific date is a Chinese workday
- `isChineseHoliday(...)`: check whether today or a specific date is a Chinese non-workday
- `chineseWorkdaySupportedYears()`: list bundled and configured calendar years that are available

### Pipeline: workday check

Use `isChineseWorkday(...)` when you want a boolean result in Pipeline. `date` is optional; when omitted
it uses the current date in `Asia/Shanghai`. The step does not expose a separate time zone
parameter.

Default-date example:

```groovy
def todayIsWorkday = isChineseWorkday()
echo "todayIsWorkday=${todayIsWorkday}"

if (!todayIsWorkday) {
    echo 'Skip release actions on a Chinese non-workday.'
}
```

Example with an explicit date:

```groovy
def result = isChineseWorkday(date: '2025-10-03')

echo "workday=${result}"

if (!result) {
    echo 'Skip release actions on non-workdays.'
}
```

Return value semantics:

- `true`: the date is a Chinese workday
- `false`: the date is a Chinese non-workday
- unsupported year: the step fails with an error

### Pipeline: holiday check

Use `isChineseHoliday(...)` when you want a boolean "non-workday" result. `date` is optional; when omitted
it uses the current date in `Asia/Shanghai`. The step does not expose a separate time zone
parameter.

Default-date example:

```groovy
if (isChineseHoliday()) {
    echo 'Today is a Chinese holiday or adjusted non-workday.'
}
```

Example with an explicit date:

```groovy
def holiday = isChineseHoliday(date: '2025-10-03')
echo "holiday=${holiday}"
```

Return value semantics:

- `true`: the date is a Chinese holiday / non-workday
- `false`: the date is a Chinese workday
- unsupported year: the step fails with an error

### Pipeline: supported years

Use `chineseWorkdaySupportedYears()` to query which bundled and custom calendars are available.

```groovy
def years = chineseWorkdaySupportedYears()
echo "supportedYears=${years.join(',')}"
```

### Pipeline: use cases

#### Run a scheduled job only on Chinese workdays

This is useful for recurring tasks such as reports, sync jobs, or weekday-only release trains.

```groovy
pipeline {
    agent any
    triggers {
        cron('H 9 * * *')
    }
    stages {
        stage('Daily Sync') {
            when {
                expression {
                    isChineseWorkday()
                }
            }
            steps {
                echo 'Run the scheduled job on a Chinese workday.'
            }
        }
    }
}
```

#### Compute once, then gate later stages

This pattern is useful when several later stages depend on the same decision.

```groovy
pipeline {
    agent any
    stages {
        stage('Prepare') {
            steps {
                script {
                    env.RUN_RELEASE = isChineseWorkday() ? 'true' : 'false'
                    echo "runRelease=${env.RUN_RELEASE}"
                }
            }
        }
        stage('Release') {
            when {
                expression {
                    env.RUN_RELEASE == 'true'
                }
            }
            steps {
                echo 'Release is enabled for today.'
            }
        }
        stage('Notify Skip') {
            when {
                expression {
                    env.RUN_RELEASE != 'true'
                }
            }
            steps {
                echo 'Release is skipped because today is a Chinese non-workday.'
            }
        }
    }
}
```

#### Guard a scripted Pipeline stage

If you use Scripted Pipeline, you can decide whether to enter a stage at all.

```groovy
node {
    stage('Build') {
        echo 'Build runs every day.'
    }

    if (isChineseWorkday()) {
        stage('Release') {
            echo 'Release runs only on a Chinese workday.'
        }
    } else {
        echo 'Skip Release stage on a Chinese non-workday.'
    }
}
```

#### End the Pipeline early on a Chinese non-workday

This pattern is useful when the whole Pipeline should stop as soon as the current day is known to
be a Chinese non-workday.

```groovy
pipeline {
    agent any
    stages {
        stage('Check Calendar') {
            steps {
                script {
                    if (!isChineseWorkday()) {
                        currentBuild.description = 'Skipped on a Chinese non-workday'
                        echo 'Stop the Pipeline early on a Chinese non-workday.'
                        return
                    }
                }
            }
        }
        stage('Release') {
            steps {
                echo 'Continue with release actions.'
            }
        }
    }
}
```

#### Check a business date from Pipeline parameters

This is useful when the job should validate a requested business date instead of always using
today.

```groovy
pipeline {
    agent any
    parameters {
        string(name: 'TARGET_DATE', defaultValue: '2025-10-03', description: 'yyyy-MM-dd')
    }
    stages {
        stage('Validate Date') {
            steps {
                script {
                    def workday = isChineseWorkday(date: params.TARGET_DATE)
                    echo "targetDate=${params.TARGET_DATE}, workday=${workday}"
                }
            }
        }
    }
}
```

#### Take a fallback path on a Chinese non-workday

This pattern is useful when you want to skip release work but still notify people or run a lighter
alternative flow.

```groovy
pipeline {
    agent any
    stages {
        stage('Release Decision') {
            steps {
                script {
                    if (isChineseHoliday()) {
                        echo 'Today is a Chinese non-workday. Send notifications instead of releasing.'
                    } else {
                        echo 'Today is a Chinese workday. Continue release actions.'
                    }
                }
            }
        }
    }
}
```

#### Check future-year support before using a custom date

This is useful when you run jobs against future schedules that may depend on administrator-provided
calendar data.

```groovy
def targetYear = 2027
def years = chineseWorkdaySupportedYears()

if (!years.contains(targetYear)) {
    error "Chinese workday calendar for ${targetYear} is not configured yet."
}

echo "isWorkday=${isChineseWorkday(date: '2027-10-02')}"
```

### Freestyle: build step

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

## Planned scope

The goal of this plugin is to provide Jenkins features around Chinese workday rules for jobs,
Pipelines, and administrator-managed calendar overrides.

Current implementation notes:

- bundled calendars are currently available for `2020` through `2026`
- bundled calendars are derived from official State Council holiday notices
- administrators can add future years or override bundled years in `Manage Jenkins -> System -> Chinese Workday`
- `chineseWorkdaySupportedYears()` returns bundled and custom years that are currently available
- unsupported years fail with an error instead of returning an ambiguous result
- all date evaluation uses the fixed time zone `Asia/Shanghai`

The exact feature set is still being refined.

## Troubleshooting

### Why does a future year fail?

If a year such as `2027` is not bundled yet and you have not configured an override, the plugin
fails explicitly instead of guessing. In that case, add a temporary calendar under
`Manage Jenkins -> System -> Chinese Workday`.

### Why can a weekend still be a workday?

Chinese make-up workdays can fall on weekends. Those dates are checked before normal weekend
fallback rules.

### Why does Jenkins system configuration override bundled data?

This is intentional. The effective precedence is:

```text
bundled < external file < system config
```

This lets administrators correct or extend data without rebuilding the plugin.

## Holiday data maintenance

Bundled holiday data should be updated through a documented review process rather than ad hoc file
edits. For the source policy, yearly update checklist, and validation flow, see
`docs/calendar-maintenance.md`.

## Development

For development notes, code structure, and local workflow details, see:

- `docs/development.md`
- `docs/architecture.md`
- `docs/calendar-maintenance.md`

## Contributing

Contributions are welcome once the initial plugin direction is stabilized.

Until then, prefer small, incremental changes that help replace the starter template with real
plugin behavior.

Refer to the Jenkins community contribution guidelines:

- https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md

## License

Licensed under MIT. See `LICENSE.md`.
