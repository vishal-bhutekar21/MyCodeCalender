# Medium-Priority Issues (P2 — Important)

This document contains technical specifications for all **P2 (Important)** issues that should be addressed before or shortly after production deployment.

---

## DATA-03: Destructive Migration Enabled in Room with Unexported Schema

Severity:
Medium

Category:
Database

Location:
- [app/src/main/java/com/vishal/mycodecalendar/MainActivity.kt:L93](file:///d:/Projects2026/dsaapp/app/src/main/java/com/vishal/mycodecalendar/MainActivity.kt#L93)
- [core/database/src/main/java/com/mycodecalendar/core/database/MyCodeCalendarDatabase.kt:L35](file:///d:/Projects2026/dsaapp/core/database/src/main/java/com/mycodecalendar/core/database/MyCodeCalendarDatabase.kt#L35)

Evidence:
`.fallbackToDestructiveMigration()` in `MainActivity.kt` and `exportSchema = false` in `MyCodeCalendarDatabase.kt`.

Current Behavior:
Whenever the Room database version increments, SQLite tables are wiped without attempting data migration.

Why This Is A Problem:
Any application update that alters database tables causes users to lose all cached offline contests, bookmark flags, and alarm reminders.

Recommended Solution:
Set `exportSchema = true`, configure the schema export directory in Gradle, and write explicit `Migration` classes or `AutoMigration` definitions.

Implementation Complexity:
Medium

Priority:
P2

Verification:
Upgrade database version from 2 to 3 with a migration script and verify existing rows persist.

Confidence:
Confirmed

---

## DATA-04: Incomplete GDPR Account Deletion

Severity:
Medium

Category:
Database / Privacy

Location:
[codecalendar-admin/src/services/firestoreService.ts:L208-L226](file:///d:/Projects2026/dsaapp/codecalendar-admin/src/services/firestoreService.ts#L208-L226)

Evidence:
`approveAndExecuteDataDeletion()` deletes only `/users/{userUid}` top document; leaves `/users/{userUid}/connected_accounts` subcollections and does not delete the user in Firebase Auth.

Current Behavior:
User data is only partially deleted.

Why This Is A Problem:
Violates Google Play Data Safety policy and GDPR requirements for comprehensive user account deletion.

Recommended Solution:
Use a Cloud Function or Admin SDK script to recursively delete all user subcollections and execute `admin.auth().deleteUser(userUid)`.

Implementation Complexity:
Medium

Priority:
P2

Verification:
Execute deletion and confirm zero residual documents or subcollections remain under the user's UID.

Confidence:
Confirmed

---

## SEC-04: Hardcoded Fallback Firebase Credentials to Wrong Project

Severity:
Medium

Category:
Configuration

Location:
[codecalendar-admin/src/services/firebase.ts:L6-L13](file:///d:/Projects2026/dsaapp/codecalendar-admin/src/services/firebase.ts#L6-L13)

Evidence:
Default values fall back to project `shetkari-mitra-7721` with real API key.

Current Behavior:
If environment variables fail to load, the Admin CMS connects to an unrelated project.

Why This Is A Problem:
Admins could inadvertently corrupt data in another application.

Recommended Solution:
Remove hardcoded project fallback strings; fail with a fatal initialization error if variables are missing.

Implementation Complexity:
Low

Priority:
P2

Verification:
Remove `.env` and verify Vite build or app startup fails with an explicit configuration error.

Confidence:
Confirmed

---

## REL-05: NotificationHelper Cloud Push Channel Inoperable

Severity:
Medium

Category:
Notifications

Location:
[core/notifications/src/main/java/com/mycodecalendar/core/notifications/NotificationHelper.kt:L22](file:///d:/Projects2026/dsaapp/core/notifications/src/main/java/com/mycodecalendar/core/notifications/NotificationHelper.kt#L22)

Evidence:
`CHANNEL_FCM_BROADCASTS` is defined, but no `FirebaseMessagingService` class or `firebase-messaging` library exists.

Current Behavior:
Cloud announcements cannot be delivered via remote push.

Why This Is A Problem:
The advertised cloud push capability cannot function.

Recommended Solution:
Add `firebase-messaging` dependency, implement `CodeCalendarMessagingService`, and declare it in `AndroidManifest.xml`.

Implementation Complexity:
High

Priority:
P2

Verification:
Send a test message from Firebase Notifications console and confirm device receives the notification.

Confidence:
Confirmed

---

## BUILD-03: Release Build References Missing release.jks & Suppresses Lint

Severity:
Medium

Category:
Deployment

Location:
[app/build.gradle.kts:L24-L58](file:///d:/Projects2026/dsaapp/app/build.gradle.kts#L24-L58)

Evidence:
`storeFile = file("release.jks")` (file absent); `lint { checkReleaseBuilds = false; abortOnError = false }`.

Current Behavior:
Building a release APK fails; release lint verification is suppressed.

Why This Is A Problem:
Release APKs cannot be assembled without developer intervention; defects bypass lint verification.

Recommended Solution:
Add automated debug keystore fallback for non-production environments; re-enable `abortOnError = true` for release builds.

Implementation Complexity:
Low

Priority:
P2

Verification:
Execute `./gradlew lintRelease` and verify checks run and report any issues.

Confidence:
Confirmed

---

## ARCH-01: Monolithic FakeRepository Violates Clean Architecture & Leaks Context

Severity:
Medium

Category:
Architecture

Location:
- [data/repository/src/main/java/com/mycodecalendar/data/repository/FakeRepository.kt](file:///d:/Projects2026/dsaapp/data/repository/src/main/java/com/mycodecalendar/data/repository/FakeRepository.kt)
- [app/src/main/java/com/vishal/mycodecalendar/MainActivity.kt:L95](file:///d:/Projects2026/dsaapp/app/src/main/java/com/vishal/mycodecalendar/MainActivity.kt#L95)

Evidence:
`FakeRepository(this, database)` passes `MainActivity` Activity context directly into a repository holding `CoroutineScope(Dispatchers.IO)`.

Current Behavior:
A single 1,980-line class coordinates database queries, network dispatch, SharedPreferences, JSON parsing, and domain logic while retaining an Activity reference.

Why This Is A Problem:
Retaining Activity context across configuration changes risks memory leaks. High cyclomatic complexity makes testing and refactoring difficult.

Recommended Solution:
1. Pass `applicationContext` instead of Activity context.
2. Break `FakeRepository` into focused domain repositories (`ContestRepositoryImpl`, `PlatformAccountRepositoryImpl`, `StreakRepositoryImpl`).

Implementation Complexity:
High

Priority:
P2

Verification:
Verify repositories can be instantiated with `Application` context and tested independently with unit test mocks.

Confidence:
Confirmed

---

## PERF-06: Room SQLite Row Bloat from Storing 150 KB JSON Blobs

Severity:
Medium

Category:
Performance

Location:
[core/database/src/main/java/com/mycodecalendar/core/database/entity/GitHubStatsEntity.kt:L28-L32](file:///d:/Projects2026/dsaapp/core/database/src/main/java/com/mycodecalendar/core/database/entity/GitHubStatsEntity.kt#L28-L32)

Evidence:
Stores serialized strings for 100 repositories and 365 daily contributions in single database row.

Current Behavior:
Row size reaches 50 KB – 150 KB, causing SQLite CursorWindow buffer allocations.

Why This Is A Problem:
Causes memory spikes and garbage collection pauses when reading basic stats on low-end devices.

Recommended Solution:
Normalize daily contributions into a separate table `github_contributions (username, date, count, intensity)`.

Implementation Complexity:
Medium

Priority:
P2

Verification:
Measure SQLite cursor read latency and memory consumption before and after normalization.

Confidence:
Confirmed
