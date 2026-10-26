# Cloud sync setup blocker

The current app works offline and can export/import a JSON backup. Live cloud sync is not enabled yet because this repository does not have a Firebase project configuration.

## Required project setup
1. Create or select a Firebase project in the account that should own the data.
2. Register an Android app with package name: com.pastellife.app
3. Enable Firebase Authentication (email/password or another chosen sign-in provider) and Cloud Firestore.
4. Download that app's google-services.json and place it in the local app/ folder. It is intentionally ignored by Git so local configuration is not accidentally committed.
5. Configure Firestore security rules so each authenticated user can read/write only their own user-scoped documents. Do not use public read/write rules for personal finance data.

## Sync implementation still required
- Add the Google Services Gradle plugin and Firebase BoM/Auth/Firestore dependencies.
- Add sign-in, sign-out, password recovery and account deletion flows.
- Define user-scoped collections and document IDs for tasks, accounts, transactions, contacts, debts, payments and savings goals.
- Implement conflict resolution, offline writes, retry behavior, migration/versioning and attachment storage.
- Test on two devices with airplane mode and conflicting edits before enabling sync for real financial records.

Until those steps are completed, the app should be treated as local-only. Use the in-app JSON backup to move or protect data.
