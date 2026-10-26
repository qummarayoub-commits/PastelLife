# PastelLife

PastelLife is an early Android personal task and finance manager built with Kotlin, Jetpack Compose, Room and WorkManager.

## Implemented in the current starter
- Pastel dashboard with available balance and net financial position
- Persistent tasks with Personal/Work/Project categories, priority, due dates, editable title/notes/due date, local file attachments, daily/weekly recurrence and time tracking
- Calendar-style agenda for dated tasks
- Daily unfinished-task notification and per-task due-date reminders targeted for 9 AM local time (Android may defer background work; notifications require permission)
- Multiple accounts with manually entered opening balances
- Income and expense entries, selected account and transaction history
- Transfers between own accounts, excluded from income/expense totals
- Contacts, receivables/payables, partial repayments and repayment history
- Savings goals, target dates, progress and contributions
- Weekly/monthly summaries and finance calculation unit tests
- JSON export and restore of local app data, with confirmation before replacing current data (file attachment links and contents are not included)
- Room migrations for recurring tasks, time tracking, savings goals, task attachments and project names

## Still to build / verify
- Cloud login and sync are not connected; no Firebase project configuration is included. See CLOUD_SYNC_SETUP.md for the exact setup blocker and secure requirements.
- Task-specific scheduled reminders, cloud attachment upload, richer calendar navigation, charts, account/contact edit controls and cloud attachment upload remain unfinished.
- This is not production-ready. The local Room database is not encrypted at rest yet. The code needs a successful automated build, device testing, data-integrity tests, encryption and a security review before storing highly sensitive financial records.
- A debug APK is only confirmed available after GitHub Actions succeeds and the artifact is checked.

## Build
Open the repository in Android Studio, or visit the Actions tab and inspect the latest “Android debug build” run. A successful run uploads app-debug.apk as an artifact.
