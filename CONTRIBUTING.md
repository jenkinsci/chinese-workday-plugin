# Contributing

Thanks for contributing to the Chinese Workday plugin.

## Before you start

- Use JDK `17+`
- Use Maven `3.9.6+`
- Read `README.md` or `README_EN.md` for plugin features and user-facing usage

## Local development

Common commands:

```bash
mvn spotless:apply
mvn test
mvn package
mvn hpi:run
```

Recommended workflow:

1. Make a focused change
2. Add or update automated tests when behavior changes
3. Run relevant local verification
4. Update docs or help text if user-facing behavior changed

## Repository-specific guides

- `docs/development.md`: local development workflow and code structure
- `docs/architecture.md`: runtime architecture and design rules
- `docs/calendar-maintenance.md`: bundled holiday calendar maintenance

## Pull requests

When opening a pull request:

- describe the motivation and the concrete change
- link the related issue when applicable
- describe how the change was tested
- note any README, help text, or other documentation updates
- mention compatibility impact if jobs or system configuration may be affected

## Commit messages

Use this format:

```text
<type>: <description>
```

Examples:

```text
feat: add supported years pipeline step
fix: validate empty configured calendars
docs: refine pipeline usage examples
```

Recommended types:

- `feat`: new functionality
- `fix`: bug fixes
- `docs`: documentation changes
- `style`: formatting or style-only changes
- `refactor`: code restructuring without intended behavior change
- `test`: test-related changes
- `chore`: build, tooling, or maintenance changes

Guidelines:

- write commit messages in English
- keep the description short and action-oriented
- prefer one focused change per commit when practical

## Tests

Prefer automated coverage for:

- Pipeline steps
- Freestyle builder behavior
- Jenkins global configuration
- holiday calendar parsing and precedence rules

For UI changes, include manual verification steps in the pull request when needed.

## Bundled holiday data updates

If your change adds or updates bundled calendar data:

- use an official holiday notice as the primary source
- keep the source link in the changed calendar file
- update representative tests
- follow `docs/calendar-maintenance.md`

## Compatibility

Please keep these behaviors stable unless the change intentionally updates them:

- default time zone is `Asia/Shanghai`
- data precedence is `bundled < external file < system configuration`
- unsupported years fail explicitly
