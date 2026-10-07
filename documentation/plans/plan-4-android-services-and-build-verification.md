# Plan 4: Android Services & Core Functionality Verification

## 1. Objective
Ensure every individual service, background receiver, database DAO, and network client in the Android application compiles and functions without errors.

---

## 2. Issues Addressed & Technical Specifications

### 2.1 Service & Component Verification
* **Contest Reminder Service (`ReminderScheduler`)**:
  * Verify `AlarmManager` exact alarm scheduling with fallback for Android 12+ (`SCHEDULE_EXACT_ALARM`).
* **Boot Alarm Recovery (`BootCompletedReceiver`)**:
  * Verify `NotificationHelper.createNotificationChannels(context)` method resolution and broadcast handling.
* **System Calendar Sync (`CalendarContractManager`)**:
  * Verify `READ_CALENDAR` and `WRITE_CALENDAR` declarations and duplicate event detection.
* **Background WorkManager Sync (`SyncManager` & `ContestSyncWorker`)**:
  * Verify periodic 30-minute sync registration and constraint handling.
* **Room Database (`MyCodeCalendarDatabase`)**:
  * Verify typed `ReminderDao`, `ContestDao`, `GitHubStatsDao`, and `PlatformStatsDao`.
* **Firebase Services (`CloudAdminSyncService`)**:
  * Verify safe non-destructive merging and verified email admin checks.

### 2.2 Compilation & Dependency Resolution
* Resolve all compiler errors across `:core:notifications`, `:data:repository`, and `:app`.
* Ensure Gradle dependency downloads and Android SDK build-tools link without errors.

### 2.3 Verification Strategy
1. Run `./gradlew compileDebugKotlin` across all modules.
2. Confirm 0 syntax errors and 0 missing reference errors.
