# PastelLife

PastelLife is an Android personal task and finance manager starter. It uses Kotlin, Jetpack Compose and Room for offline-first local storage.

## Current working foundation
- Pastel dashboard with available account balance, net savings and net financial position
- Persistent tasks: add, complete, reopen and delete
- Multiple accounts with manually entered opening balances
- Income and expense entries with transaction history
- Transfers between accounts, kept separate from income/expenses
- Contacts
- Receivables and payables linked to contacts
- Partial repayments, remaining balances and payment history
- Daily unfinished-task notification scheduled for approximately 9 AM local time (Android may delay background work; notifications require permission)

## Important limitations
- Data is stored locally on this device; cloud login/sync and attachments are not connected yet.
- Recurring task rules, task-specific due-date reminders, calendar view, time tracking, savings goals and weekly/monthly chart views remain to be built.
- This is an early development build, not a production-ready financial application. Backups, account transfers and debt payment sequences need more automated tests before relying on it for important records.
- A debug APK is only considered available after GitHub Actions reports a successful build and the artifact is verified.

## Build
Open the repository in Android Studio or run the GitHub Actions workflow from the Actions tab. The workflow is configured to build `app-debug.apk` and upload it as an artifact.
