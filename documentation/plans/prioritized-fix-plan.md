# Prioritized Remediation Plan

This roadmap provides the definitive, phased engineering execution plan to bring the **Code Calendar (MyCodeCalendar)** application to full production readiness.

**IMPORTANT**: This document is an implementation blueprint. No code modifications are performed during this audit phase.

---

## 🗺️ Implementation Phases Overview

| Phase | Focus Area | Issue IDs Addressed | Target Outcome |
|---|---|---|---|
| **Phase 1** | **P0 Production Blockers & Security** | `DATA-01`, `SEC-01`, `SEC-02`, `BUILD-01`, `BUILD-02`, `REL-01`, `REL-02` | Eliminates data loss, patches critical security flaws, enables CI/CD builds, and restores core alarm/calendar features. |
| **Phase 2** | **Data Integrity & Security Hardening**| `DATA-02`, `SEC-03`, `SEC-04`, `REL-03`, `DATA-03`, `DATA-04` | Restores feedback reporting, secures payment worker, fixes Google Auth permanence, and protects Room migrations. |
| **Phase 3** | **Cloudflare Edge Deployment & API Gateway**| `CF-01`, `PERF-01`, `PERF-02`, `PERF-03` | Deploys Cloudflare Worker edge aggregation; reduces client refresh from 15s to < 200ms; eliminates rate limits. |
| **Phase 4** | **Observability & Background Reliability** | `OBS-01`, `REL-04`, `REL-05`, `PERF-04`, `PERF-05` | Adds Crashlytics & edge telemetry, activates WorkManager sync, and optimizes Firestore Admin CMS reads. |
| **Phase 5** | **Architecture & Build Polish** | `BUILD-03`, `ARCH-01`, `ARCH-02`, `PERF-06`, `DOC-01` | Prunes dead module stubs, breaks down `FakeRepository`, normalizes SQLite schemas, and aligns documentation. |

---

## 📌 Phase 1: Production Blockers & Security (P0)

### 1.1 Invert Auth Cloud Sync Order (`DATA-01`)
* **Components**: [MainActivity.kt](file:///d:/Projects2026/dsaapp/app/src/main/java/com/vishal/mycodecalendar/MainActivity.kt), [CloudAdminSyncService.kt](file:///d:/Projects2026/dsaapp/app/src/main/java/com/vishal/mycodecalendar/CloudAdminSyncService.kt), [FakeRepository.kt](file:///d:/Projects2026/dsaapp/data/repository/src/main/java/com/mycodecalendar/data/repository/FakeRepository.kt)
* **Objective**: Prevent user accounts from being wiped in Firestore upon login.
* **Implementation**:
  1. Remove `repository.clearAllUserData()` from the `onAuthSuccess` callback in `MainActivity.kt`.
  2. Call `CloudAdminSyncService.fetchConnectedAccountsFromCloud(currentUid)` and `fetchUserStreakFromCloud(currentUid)` *before* issuing cloud profile sync.
  3. Merge restored handles into Room and in-memory StateFlows.
  4. Restrict `clearAllUserData()` exclusively to explicit Sign-Out flows.
* **Risk**: Low.
* **Verification**: Verify that logging in on a secondary device downloads and renders all previously connected platforms without wiping them.

### 1.2 Purge Keystore Passwords from Source Control (`SEC-01`)
* **Components**: [app/build.gradle.kts](file:///d:/Projects2026/dsaapp/app/build.gradle.kts)
* **Objective**: Remove plaintext passwords from Git history.
* **Implementation**: Read `storePassword` and `keyPassword` from environment variables `System.getenv("KEYSTORE_PASSWORD")` and `System.getenv("KEY_PASSWORD")`.
* **Risk**: Low.
* **Verification**: Run `./gradlew assembleRelease` with environment variables set; confirm APK signs successfully.

### 1.3 Fix Super-Admin Privilege Escalation in Firestore Rules (`SEC-02`)
* **Components**: [codecalendar-admin/firestore.rules](file:///d:/Projects2026/dsaapp/codecalendar-admin/firestore.rules)
* **Objective**: Prevent unverified accounts from claiming administrative privileges.
* **Implementation**: Add `request.auth.token.email_verified == true` to the `isAdmin()` helper function.
* **Risk**: Low.
* **Verification**: Test in Firebase Emulator: confirm unverified user registration with an admin email is rejected.

### 1.4 Fix Broken CI/CD & Build Incompatibilities (`BUILD-01`, `BUILD-02`)
* **Components**: [gradle.properties](file:///d:/Projects2026/dsaapp/gradle.properties), [.github/workflows/pr.yml](file:///d:/Projects2026/dsaapp/.github/workflows/pr.yml)
* **Objective**: Restore clean compilation across all developer machines and CI/CD pipelines.
* **Implementation**:
  1. Remove `org.gradle.java.home=C:\\Program Files\\Java\\jdk-21` from `gradle.properties`.
  2. Remove `- name: Build Ktor Backend` and `run: ./gradlew :backend:build` from `pr.yml`.
* **Risk**: Very Low.
* **Verification**: Run `./gradlew --version` and push a test PR; confirm GitHub Actions passes with green status.

### 1.5 Implement Device Boot Alarm Rescheduling (`REL-01`)
* **Components**: [app/src/main/AndroidManifest.xml](file:///d:/Projects2026/dsaapp/app/src/main/AndroidManifest.xml), `core:notifications`
* **Objective**: Ensure contest reminder alarms survive device restarts.
* **Implementation**:
  1. Declare `RECEIVE_BOOT_COMPLETED` permission in manifest.
  2. Create `BootCompletedReceiver : BroadcastReceiver` that queries the Room `reminders` table and reschedules upcoming alarms via `ReminderScheduler`.
* **Risk**: Low.
* **Verification**: Schedule a reminder, reboot the device via `adb reboot`, inspect `adb shell dumpsys alarm`, and confirm alarm persistence.

### 1.6 Add Calendar Runtime Permissions (`REL-02`)
* **Components**: [app/src/main/AndroidManifest.xml](file:///d:/Projects2026/dsaapp/app/src/main/AndroidManifest.xml), `feature:contestdetail`
* **Objective**: Prevent `SecurityException` during Google Calendar export.
* **Implementation**:
  1. Add `READ_CALENDAR` and `WRITE_CALENDAR` to `AndroidManifest.xml`.
  2. Add Compose permission launcher to request user consent before calling `CalendarContractManager`.
* **Risk**: Low.
* **Verification**: Tap "Export to Calendar" on a real device, grant permission, and verify event appears in the calendar app.

---

## 📌 Phase 2: Data Integrity & Security Hardening (P1/P2)

### 2.1 Add Security Rule for `/feedback` Collection (`DATA-02`)
* **Components**: [codecalendar-admin/firestore.rules](file:///d:/Projects2026/dsaapp/codecalendar-admin/firestore.rules)
* **Objective**: Enable user bug reports and feature requests.
* **Implementation**: Add `match /feedback/{id} { allow create: if request.auth != null; allow read, update: if isAdmin(); }`.

### 2.2 Fix Google Sign-In Credential Linking (`REL-03`)
* **Components**: [feature/onboarding/src/main/java/com/mycodecalendar/feature/onboarding/AuthScreen.kt](file:///d:/Projects2026/dsaapp/feature/onboarding/src/main/java/com/mycodecalendar/feature/onboarding/AuthScreen.kt)
* **Objective**: Issue permanent Firebase user accounts on Google Sign-In rather than disposable anonymous sessions.
* **Implementation**: Configure `GoogleSignInOptions` with `requestIdToken()`; pass Google ID token to `GoogleAuthProvider.getCredential()`.

### 2.3 Secure Razorpay Secret in Worker (`SEC-03`)
* **Components**: Deployed Cloudflare Worker script `razorpay-backend-worker`
* **Objective**: Remove hardcoded fallback secret key string from JavaScript source.
* **Implementation**: Rotate Razorpay key in merchant dashboard; bind exclusively via `wrangler secret put RAZORPAY_KEY_SECRET`.

### 2.4 Enable Safe Room Database Migrations (`DATA-03`)
* **Components**: [MyCodeCalendarDatabase.kt](file:///d:/Projects2026/dsaapp/core/database/src/main/java/com/mycodecalendar/core/database/MyCodeCalendarDatabase.kt), [MainActivity.kt](file:///d:/Projects2026/dsaapp/app/src/main/java/com/vishal/mycodecalendar/MainActivity.kt)
* **Objective**: Prevent accidental data drops during future database version upgrades.
* **Implementation**: Set `exportSchema = true`, configure schema location in `build.gradle.kts`, and write explicit `Migration` classes.

---

## 📌 Phase 3: Cloudflare Edge Deployment & API Gateway (P1)

### 3.1 Deploy CodeCalendar Edge Aggregation Worker (`CF-01`, `PERF-01`, `PERF-02`, `PERF-03`)
* **Components**: New `backend-worker/` directory with `wrangler.toml` targeting Cloudflare Zone `vishalbhutekar.me` (or `mycodecalendar.com`).
* **Objective**: Consolidate multi-platform contest fetching into a single global edge endpoint.
* **Implementation**:
  1. Build a TypeScript Cloudflare Worker that fetches LeetCode, Codeforces, CodeChef, and AtCoder every 10 minutes via a scheduled Cron Trigger.
  2. Store normalized contest JSON in Cloudflare KV / Cache API under `contests:all:v1`.
  3. Expose route `GET /v1/contests` with `Cache-Control: public, max-age=300, stale-while-revalidate=3600`.
  4. Expose route `GET /v1/user/stats/:platform/:handle` to proxy and cache GitHub, Codeforces, and LeetCode stats, shielding clients from rate limits.
  5. Update Android `RemoteDataSource.kt` to point to the active edge URL.
* **Risk**: Medium.
* **Expected Outcome**: Android contest refresh drops from 15,000 ms to **< 150 ms**; cellular data consumption drops by **96%**; rate limits are 100% eliminated.

---

## 📌 Phase 4: Observability & Background Sync (P1/P2)

### 4.1 Integrate Firebase Crashlytics & Remote Telemetry (`OBS-01`)
* **Components**: [app/build.gradle.kts](file:///d:/Projects2026/dsaapp/app/build.gradle.kts), `libs.versions.toml`
* **Objective**: Gain real-time diagnostic visibility into production crashes and exceptions.
* **Implementation**: Add Google Services Crashlytics Gradle plugin and library; initialize in `Application`.

### 4.2 Activate Periodic WorkManager Sync (`REL-04`)
* **Components**: [sync/src/main/java/com/mycodecalendar/sync/SyncManager.kt](file:///d:/Projects2026/dsaapp/sync/src/main/java/com/mycodecalendar/sync/SyncManager.kt), [MainActivity.kt](file:///d:/Projects2026/dsaapp/app/src/main/java/com/vishal/mycodecalendar/MainActivity.kt)
* **Objective**: Keep contest radar and alarms fresh in the background.
* **Implementation**: Implement real refresh call in `ContestSyncWorker`; schedule via `SyncManager(applicationContext).schedulePeriodicSync()` in `MainActivity.onCreate()`.

### 4.3 Optimize Firestore Reads in Admin Portal (`PERF-04`, `PERF-05`)
* **Components**: [codecalendar-admin/src/services/firestoreService.ts](file:///d:/Projects2026/dsaapp/codecalendar-admin/src/services/firestoreService.ts), [UsersDirectory.tsx](file:///d:/Projects2026/dsaapp/codecalendar-admin/src/pages/UsersDirectory.tsx)
* **Objective**: Eliminate high Firestore billing charges and browser lag in Admin CMS.
* **Implementation**:
  1. Replace full document fetches in `fetchDashboardMetrics()` with `getCountFromServer()`.
  2. Replace unbounded collection listener in `UsersDirectory` with paginated `limit(50)` queries.

---

## 📌 Phase 5: Architecture Polish & Module Pruning (P2/P3)

### 5.1 Refactor Monolithic Repository (`ARCH-01`)
* **Components**: [data/repository/src/main/java/com/mycodecalendar/data/repository/FakeRepository.kt](file:///d:/Projects2026/dsaapp/data/repository/src/main/java/com/mycodecalendar/data/repository/FakeRepository.kt)
* **Objective**: Decompose 1,980-line God class into focused repositories; eliminate Activity context leak.
* **Implementation**: Split into `ContestRepositoryImpl`, `PlatformAccountRepositoryImpl`, `StreakRepositoryImpl`, and pass `Application` context.

### 5.2 Prune Dead Gradle Modules (`ARCH-02`)
* **Components**: [settings.gradle.kts](file:///d:/Projects2026/dsaapp/settings.gradle.kts)
* **Objective**: Clean up build configuration overhead.
* **Implementation**: Remove empty stub modules (`:widget`, `:core:analytics`, `:data:local`, `:data:remote`, `:data:mapper`) from `settings.gradle.kts`.

### 5.3 Normalize SQLite Daily Contributions (`PERF-06`)
* **Components**: [core/database/src/main/java/com/mycodecalendar/core/database/entity/GitHubStatsEntity.kt](file:///d:/Projects2026/dsaapp/core/database/src/main/java/com/mycodecalendar/core/database/entity/GitHubStatsEntity.kt)
* **Objective**: Eliminate SQLite CursorWindow buffer splitting from 150 KB JSON strings.
* **Implementation**: Move daily contribution heatmap records into a child table `github_contributions`.
