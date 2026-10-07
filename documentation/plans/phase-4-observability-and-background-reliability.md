# Phase 4 Execution Plan: Observability & Background Reliability (P1/P2)

## 1. Executive Summary
Phase 4 implements background synchronization, observability, and administrative query optimization:
- Replaces the placeholder `ContestSyncWorker` with a functional background synchronization task (`REL-04`).
- Schedules periodic sync in `MainActivity` so contests stay fresh without manual user interaction.
- Optimizes Firestore Admin queries to use `getCountFromServer()` instead of unbounded document downloads (`PERF-04`, `PERF-05`).
- Configures observability and error logging patterns (`OBS-01`).

---

## 2. Issues Addressed & Technical Specifications

### 2.1 REL-04: Implement Functional Background Contest Sync
* **Affected Files**:
  * [sync/src/main/java/com/mycodecalendar/sync/SyncManager.kt](file:///d:/Projects2026/dsaapp/sync/src/main/java/com/mycodecalendar/sync/SyncManager.kt)
  * [MainActivity.kt](file:///d:/Projects2026/dsaapp/app/src/main/java/com/vishal/mycodecalendar/MainActivity.kt)
* **Root Cause**:
  `ContestSyncWorker` only had `println("Background ContestSyncWorker executing successfully.")` and was never scheduled in `MainActivity`.
* **Implementation Plan**:
  1. In `ContestSyncWorker.doWork()`:
     - Access application context.
     - Call network/repository contest refresh logic.
     - Catch and handle exceptions with `Result.retry()` on network errors or `Result.failure()` on unrecoverable errors.
  2. In `MainActivity.onCreate()`:
     - Instantiate `SyncManager(applicationContext).schedulePeriodicSync()`.
     - Ensures Android's WorkManager keeps contest radar fresh in the background while complying with battery and network constraints.

---

### 2.2 PERF-04 & PERF-05: Firestore Count & Pagination in Admin Portal
* **Affected Files**:
  * [codecalendar-admin/src/services/firestoreService.ts](file:///d:/Projects2026/dsaapp/codecalendar-admin/src/services/firestoreService.ts)
  * [codecalendar-admin/src/pages/UsersDirectory.tsx](file:///d:/Projects2026/dsaapp/codecalendar-admin/src/pages/UsersDirectory.tsx)
* **Root Cause**:
  `fetchDashboardMetrics()` used `getDocs(collection(firestore, 'users'))` and read entire documents just to get `.size`.
* **Implementation Plan**:
  1. In `firestoreService.ts`:
     - Import `getCountFromServer` from `firebase/firestore`.
     - Replace `getDocs(collection(firestore, 'users'))` with `getCountFromServer(collection(firestore, 'users'))`.
     - Replace `getDocs(collection(firestore, 'featured_materials'))` and `getDocs(collection(firestore, 'custom_contests'))` with `getCountFromServer()`.
     - Extract counts via `snapshot.data().count`.
  2. In `UsersDirectory.tsx`:
     - Ensure queries limit initial fetches (e.g. `limit(50)`).
* **Expected Outcome**:
  Reduces Firestore read billing by 99% for admin dashboard visits and eliminates browser memory spikes.

---

### 2.3 OBS-01: Application Telemetry & Observability
* **Affected Files**:
  * [app/build.gradle.kts](file:///d:/Projects2026/dsaapp/app/build.gradle.kts)
  * [core/common/src/main/java/com/mycodecalendar/core/common/Logger.kt](file:///d:/Projects2026/dsaapp/core/common/Logger.kt)
* **Implementation Plan**:
  1. Create a structured logger utility `AppLogger` in `core:common` with log levels, tag management, and safe production masking.
  2. Provide hooks for remote error logging (Crashlytics / Sentry) in non-debug builds.
* **Verification**:
  Verify logs are structured and do not expose user PII or sensitive keys in production logs.
