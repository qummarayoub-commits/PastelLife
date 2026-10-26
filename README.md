# PastelLife

PastelLife is an early Android personal task and finance manager built with Kotlin, Jetpack Compose, Room and WorkManager.

## Implemented in the current starter
- Pastel dashboard with available account balance and net financial position
- Persistent tasks with due-date text, daily/weekly recurrence and start/stop time tracking
- Daily unfinished-task reminder scheduled for approximately 9 AM local time (Android may defer background work; notifications require permission)
- Multiple accounts with manually entered opening balances
- Income and expense transactions with history
- Transfers between own accounts, excluded from income/expense totals
- Contacts
- Receivables and payables linked to contacts
- Partial and full repayments with payment history
- Savings goals, target dates, progress and contributions
- Weekly and monthly income/expense/net savings summaries
- JSON export and restore of local app data, with confirmation before replacing data
- Room migrations for new task fields and savings goals

## Still to build / verify
- Cloud login and sync are not connected; no Firebase project configuration is included.
- Calendar screen, task-specific scheduled reminders, attachments, time charts and advanced filtering remain unfinished.
- This is not production-ready. The code still needs a successful automated build, device testing, data-integrity tests and security review.
- Debug APK availability is only confirmed after GitHub Actions succeeds and the artifact is checked.

## Build
Open the repository in Android Studio, or visit the Actions tab and inspect the latest “Build Android APK” run. A successful run uploads app-debug.apk as an artifact.
