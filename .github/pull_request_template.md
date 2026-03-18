## Summary

- 

## Motivation / Context

- 

## What changed

- 

## Linked issue

- 

## Testing done

<!--
Describe how you verified the change.
- For code changes, mention automated tests added or updated.
- For UI changes, include manual verification steps and screenshots when helpful.
- For refactoring or cleanup, explain how you confirmed behavior stayed the same.
-->

- [ ] `mvn test`
- [ ] `mvn hpi:run`
- [ ] Not run locally

## Docs / help text

<!--
Mention whether any user-facing docs or help content were updated, for example:
- `README.md`
- `README_ZH.md`
- Jelly help pages under `src/main/resources` or `src/main/webapp`
-->

- 

## Checklist

- [ ] I updated tests or verified existing coverage is sufficient
- [ ] I updated docs/help text if user-facing behavior changed
- [ ] I considered compatibility for existing jobs and system configuration

<details>
<summary>Bundled holiday data checklist (only for calendar data PRs)</summary>

- [ ] I attached the official holiday notice link
- [ ] I updated `src/main/resources/io/jenkins/plugins/chinese_workday/calendars/<year>.properties`
- [ ] I updated `src/main/resources/io/jenkins/plugins/chinese_workday/calendars/index.properties` when needed
- [ ] I kept a `# Source: ...` comment in each changed calendar file
- [ ] I added or updated automated tests for representative holidays and make-up workdays

</details>

## Notes

- 
