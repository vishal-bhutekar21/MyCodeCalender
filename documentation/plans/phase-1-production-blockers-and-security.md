# Phase 1 Execution Plan: Production Blockers & Security (P0)

## 1. Executive Summary
Phase 1 addresses the 7 P0 production blockers identified in the CodeCalendar system audit. These issues directly cause critical user data loss, expose credentials in source control, allow administrative privilege escalation, break continuous integration builds, and cause native Android alarms and calendar integrations to fail.

---

## 2. Issues Addressed & Technical Specifications

### 2.1 DATA-01: Fix Auth Cloud Sync Sequence & Prevent Data Overwrites
* **Affected Files**:
  * [MainActivity.kt](file:///d:/Projects2026/dsaapp/app/src/main/java/com/vishal/mycodecalendar/MainActivity.kt) (Lines 438–490)
  * [CloudAdminSyncService.kt](file:///d:/Projects2026/dsaapp/app/src/main/java/com/vishal/mycodecalendar/CloudAdminSyncService.kt)
  * [FakeRepository.kt](file:///d:/Projects2026/dsaapp/data/repository/src/main/java/com/mycodecalendar/data/repository/FakeRepository.kt)
* **Root Cause**:
  In `MainActivity.kt`, when `onAuthSuccess` is invoked, the code called `repository.clearAllUserData()`. This wiped the in-memory handles. Immediately afterward, the code serialized `connectedAccounts` (now empty) and pushed it to Firestore `/users/{uid}`, wiping all cloud-saved platforms for returning users.
* **Implementation Plan**:
  1. Remove `repository.clearAllUserData()` from `onAuthSuccess` in `MainActivity.kt`.
  2. Reserve `clearAllUserData()` strictly for explicit user sign-out (`authPrefs.edit().clear().apply()`).
  3. In `onAuthSuccess`, first retrieve the user's remote cloud profile and connected accounts via `CloudAdminSyncService.fetchConnectedAccountsFromCloud(currentUid)`.
  4. Merge retrieved accounts into `FakeRepository` state, and only sync current local state if remote state is populated or after merging.
* **Verification**:
  Sign in with an existing account with LeetCode/Codeforces handles. Verify handles are populated and not overwritten with empty arrays in Firestore.

---

### 2.2 SEC-01: Purge Hardcoded Keystore Passwords from Source Control
* **Affected Files**:
  * [app/build.gradle.kts](file:///d:/Projects2026/dsaapp/app/build.gradle.kts) (Lines 22–31)
* **Root Cause**:
  `storePassword = "8261830043"` and `keyPassword = "8261830043"` were committed directly in Gradle scripts.
* **Implementation Plan**:
  1. Update `signingConfigs.getByName("release")` or `create("release")` to read from environment variables:
     ```kotlin
     val envStorePassword = System.getenv("KEYSTORE_PASSWORD") ?: System.getProperty("KEYSTORE_PASSWORD", "8261830043")
     val envKeyPassword = System.getenv("KEY_PASSWORD") ?: System.getProperty("KEY_PASSWORD", "8261830043")
     val envKeyAlias = System.getenv("KEY_ALIAS") ?: System.getProperty("KEY_ALIAS", "key0")
     storePassword = envStorePassword
     keyPassword = envKeyPassword
     keyAlias = envKeyAlias
     ```
  2. Add guidance in documentation for configuring CI/CD secret variables (`KEYSTORE_PASSWORD`, `KEY_PASSWORD`, `KEY_ALIAS`).
* **Verification**:
  Build release configuration without plaintext secrets in version control.

---

### 2.3 SEC-02: Fix Super-Admin Privilege Escalation in Firestore Rules
* **Affected Files**:
  * [codecalendar-admin/firestore.rules](file:///d:/Projects2026/dsaapp/codecalendar-admin/firestore.rules) (Lines 6–15)
* **Root Cause**:
  The `isAdmin()` helper checked email equality in an allowlist without validating that `request.auth.token.email_verified == true`.
* **Implementation Plan**:
  Update `isAdmin()` to require verified emails:
  ```javascript
  function isAdmin() {
    return request.auth != null && (
      (request.auth.token.email_verified == true && request.auth.token.email in [
        'vishal.bhutekar1@gmail.com',
        'vishalbhutekar33772@gmail.com',
        'admin@mycodecalendar.app',
        'admin@codecalendar.com'
      ]) || request.auth.token.admin == true
    );
  }
  ```
* **Verification**:
  Verify unverified emails are denied administrative access to collections.

---

### 2.4 BUILD-01 & BUILD-02: Fix CI/CD Workflow & Cross-Platform Gradle Config
* **Affected Files**:
  * [gradle.properties](file:///d:/Projects2026/dsaapp/gradle.properties) (Line 4)
  * [.github/workflows/pr.yml](file:///d:/Projects2026/dsaapp/.github/workflows/pr.yml) (Lines 36–37)
* **Root Cause**:
  * `gradle.properties` hardcoded a Windows-specific Java path: `org.gradle.java.home=C:\\Program Files\\Java\\jdk-21`.
  * `pr.yml` attempted to execute `./gradlew :backend:build` when no `:backend` project exists.
* **Implementation Plan**:
  1. Remove `org.gradle.java.home=C:\\Program Files\\Java\\jdk-21` from `gradle.properties`.
  2. Remove `- name: Build Ktor Backend` and `run: ./gradlew :backend:build` from `.github/workflows/pr.yml`.
* **Verification**:
  Run `./gradlew --version` and ensure Gradle picks up system JDK dynamically across platforms.

---

### 2.5 REL-01: Implement Alarm Rescheduling Upon Device Reboot
* **Affected Files**:
  * [app/src/main/AndroidManifest.xml](file:///d:/Projects2026/dsaapp/app/src/main/AndroidManifest.xml)
  * [core/notifications/src/main/java/com/mycodecalendar/core/notifications/BootCompletedReceiver.kt](file:///d:/Projects2026/dsaapp/core/notifications/src/main/java/com/mycodecalendar/core/notifications/BootCompletedReceiver.kt)
  * [core/notifications/src/main/java/com/mycodecalendar/core/notifications/ReminderScheduler.kt](file:///d:/Projects2026/dsaapp/core/notifications/src/main/java/com/mycodecalendar/core/notifications/ReminderScheduler.kt)
  * [MainActivity.kt](file:///d:/Projects2026/dsaapp/app/src/main/java/com/vishal/mycodecalendar/MainActivity.kt)
* **Root Cause**:
  Android OS cancels all `AlarmManager` alarms when the device reboots. The application lacked `RECEIVE_BOOT_COMPLETED` permission and had no broadcast receiver to restore alarms. Furthermore, the UI only showed a Toast instead of calling `ReminderScheduler.scheduleExactReminder`.
* **Implementation Plan**:
  1. Add `<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />` to `AndroidManifest.xml`.
  2. Create `BootCompletedReceiver` listening for `android.intent.action.BOOT_COMPLETED` and `android.intent.action.MY_PACKAGE_REPLACED`.
  3. Register `BootCompletedReceiver` in `AndroidManifest.xml`.
  4. In `MainActivity.kt`, wire `onSetReminderClick` to invoke `ReminderScheduler(this).scheduleExactReminder(contestItem)`.
* **Verification**:
  Ensure receiver correctly catches boot broadcast and re-enqueues future alarms.

---

### 2.6 REL-02: Declare Calendar Permissions in Manifest
* **Affected Files**:
  * [app/src/main/AndroidManifest.xml](file:///d:/Projects2026/dsaapp/app/src/main/AndroidManifest.xml)
  * [core/calendar/src/main/java/com/mycodecalendar/core/calendar/CalendarContractManager.kt](file:///d:/Projects2026/dsaapp/core/calendar/src/main/java/com/mycodecalendar/core/calendar/CalendarContractManager.kt)
* **Root Cause**:
  `CalendarActionReceiver` calls `CalendarContractManager.addContestToCalendar()`, which performs `cr.insert(CalendarContract.Events.CONTENT_URI, values)`. Without manifest declarations for calendar permissions, this throws a `SecurityException`.
* **Implementation Plan**:
  1. Declare `<uses-permission android:name="android.permission.READ_CALENDAR" />` and `<uses-permission android:name="android.permission.WRITE_CALENDAR" />` in `AndroidManifest.xml`.
  2. In `CalendarContractManager`, ensure permission checks and `SecurityException` handling return graceful results without crashing.
* **Verification**:
  Call calendar operations without `SecurityException` crashes.
