# Critical Issues (P0 — Production Blockers)

This document contains deep technical specifications for all **P0 (Production Blocker)** issues that prevent safe deployment to production.

---

## DATA-01: Account Login Clears Local Data and Overwrites Cloud Profile

Severity:
Critical

Category:
Database

Location:
- [app/src/main/java/com/vishal/mycodecalendar/MainActivity.kt:L439-L475](file:///d:/Projects2026/dsaapp/app/src/main/java/com/vishal/mycodecalendar/MainActivity.kt#L439-L475)
- [app/src/main/java/com/vishal/mycodecalendar/CloudAdminSyncService.kt:L112-L139](file:///d:/Projects2026/dsaapp/app/src/main/java/com/vishal/mycodecalendar/CloudAdminSyncService.kt#L112-L139)
- [data/repository/src/main/java/com/mycodecalendar/data/repository/FakeRepository.kt:L1325-L1351](file:///d:/Projects2026/dsaapp/data/repository/src/main/java/com/mycodecalendar/data/repository/FakeRepository.kt#L1325-L1351)

Evidence:
In `MainActivity.kt`:
```kotlin
onAuthSuccess = { user, method, email, photoUrl ->
    repository.clearAllUserData() // Wipes connectedPlatforms to emptyList()
    ...
    val connMap = connectedAccounts.associate { it.platform.name.lowercase() to it.username }
    CloudAdminSyncService.syncUserProfileToCloud(
        uid = currentUid,
        ...
        connectedPlatforms = connectedAccounts.map { it.platform.name }, // Pass emptyList()
        connectedAccountsMap = connMap // Pass emptyMap()
    )
```

Current Behavior:
When a user authenticates in `AuthScreen`, `MainActivity` immediately executes `repository.clearAllUserData()`. This empties the in-memory `connectedPlatforms` and `connectedAccounts` lists. Immediately afterward, `syncUserProfileToCloud()` is invoked using the now-empty lists, overwriting the Firestore document at `/users/{uid}` with `connectedPlatforms: []` and `connectedAccountsMap: {}`.

Why This Is A Problem:
Any user logging into an existing account (or logging in on a secondary device) has all of their cloud-stored competitive programming handles wiped out in Firestore before the app has a chance to download and restore them.

Potential Impact:
Permanent data loss for 100% of returning authenticated users.

Trigger:
Occurs every time a user signs in via Google, Email, or Create Account.

Recommended Solution:
1. Invert the synchronization order: First call `CloudAdminSyncService.fetchConnectedAccountsFromCloud(currentUid)` and `fetchUserStreakFromCloud(currentUid)`.
2. Merge the cloud-restored platforms into Room and memory *before* emitting updates.
3. Only call `repository.clearAllUserData()` during an explicit **Sign Out** action, never during a **Sign In** action.

Implementation Complexity:
Medium

Priority:
P0

Verification:
Link accounts on Device A, log out, log in on Device B, and confirm all linked platforms appear intact without being cleared in Firestore.

Confidence:
Confirmed

---

## SEC-01: Plaintext Production Keystore Passwords Committed in Version Control

Severity:
Critical

Category:
Security

Location:
[app/build.gradle.kts:L22-L31](file:///d:/Projects2026/dsaapp/app/build.gradle.kts#L22-L31)

Evidence:
```kotlin
signingConfigs {
    create("release") {
        storeFile = file("release.jks")
        storePassword = "8261830043"
        keyAlias = "key0"
        keyPassword = "8261830043"
        enableV1Signing = true
        enableV2Signing = true
    }
}
```

Current Behavior:
Production keystore passwords (`8261830043`) are committed directly into the Git repository in plaintext.

Why This Is A Problem:
Secrets in Git history are permanently exposed to anyone with repository access. If the `release.jks` binary is obtained, unauthorized parties can sign and distribute malicious updates that Android OS will accept as authentic updates to CodeCalendar.

Potential Impact:
Full compromise of the production application release chain.

Trigger:
Present continuously in Git commit history.

Recommended Solution:
1. Remove plaintext passwords from `app/build.gradle.kts`.
2. Configure Gradle to read credentials from environment variables (`System.getenv("KEYSTORE_PASSWORD")`) or a gitignored `keystore.properties` file.
3. Rotate the production keystore alias password.

Implementation Complexity:
Low

Priority:
P0

Verification:
Inspect `app/build.gradle.kts` and verify no password strings exist; verify `./gradlew assembleRelease` reads credentials from environment variables.

Confidence:
Confirmed

---

## SEC-02: Super-Admin Privilege Escalation via Unverified Email in Firestore Rules

Severity:
Critical

Category:
Security

Location:
[codecalendar-admin/firestore.rules:L6-L15](file:///d:/Projects2026/dsaapp/codecalendar-admin/firestore.rules#L6-L15)

Evidence:
```javascript
function isAdmin() {
  return request.auth != null && (
    request.auth.token.email in [
      'vishal.bhutekar1@gmail.com',
      'vishalbhutekar33772@gmail.com',
      'admin@mycodecalendar.app',
      'admin@codecalendar.com'
    ] || request.auth.token.admin == true
  );
}
```

Current Behavior:
The `isAdmin()` security rule grants full administrative read/write access to anyone whose token email matches the whitelist, without checking `request.auth.token.email_verified == true`.

Why This Is A Problem:
Firebase Email/Password Auth allows anyone to create an account with any email address without verifying ownership upfront. An attacker can register an account using `admin@codecalendar.com` or `admin@mycodecalendar.app`, obtain a valid Firebase JWT, and bypass all security rules.

Potential Impact:
Complete unauthorized takeover of the CMS: attackers can delete user directories, read private user data, and post malicious announcement broadcasts to all mobile apps.

Trigger:
Any attacker signing up via standard Firebase Auth endpoints using an unverified administrative email.

Recommended Solution:
Require email verification in Firestore rules:
```javascript
function isAdmin() {
  return request.auth != null &&
         request.auth.token.email_verified == true &&
         request.auth.token.email in [
           'vishal.bhutekar1@gmail.com',
           'vishalbhutekar33772@gmail.com'
         ];
}
```
Or migrate super-admin privileges to custom user claims (`request.auth.token.admin == true`).

Implementation Complexity:
Low

Priority:
P0

Verification:
Attempt to write to `/broadcasts` using an unverified account created with an admin email in the Firebase Emulator; verify write is denied with `PERMISSION_DENIED`.

Confidence:
Confirmed

---

## BUILD-01: Machine-Specific Absolute JDK Path in gradle.properties Breaks Builds

Severity:
Critical

Category:
Deployment

Location:
[gradle.properties:L4](file:///d:/Projects2026/dsaapp/gradle.properties#L4)

Evidence:
```properties
org.gradle.java.home=C:\\Program Files\\Java\\jdk-21
```

Current Behavior:
Gradle hardcodes an absolute Windows path to Java 21 on the developer's local machine.

Why This Is A Problem:
Any team member, external contributor, or CI/CD environment (such as GitHub Actions Ubuntu runners) that does not possess this exact folder structure fails immediately upon executing `./gradlew` with: `Value 'C:\Program Files\Java\jdk-21' given for org.gradle.java.home Gradle property is invalid`.

Potential Impact:
Project cannot be compiled or tested on CI/CD or other developer workstations.

Trigger:
Running any Gradle command on a machine without `C:\Program Files\Java\jdk-21`.

Recommended Solution:
Delete `org.gradle.java.home=C:\\Program Files\\Java\\jdk-21` from `gradle.properties`. Use standard Gradle Foojay toolchains (configured in `settings.gradle.kts`) or the system `JAVA_HOME`.

Implementation Complexity:
Low

Priority:
P0

Verification:
Execute `./gradlew --version` on a machine or runner where `C:\Program Files\Java\jdk-21` does not exist and confirm successful execution.

Confidence:
Confirmed

---

## BUILD-02: GitHub Actions CI Workflow pr.yml Fails on Non-Existent :backend:build Module

Severity:
Critical

Category:
Deployment

Location:
- [.github/workflows/pr.yml:L36-L37](file:///d:/Projects2026/dsaapp/.github/workflows/pr.yml#L36-L37)
- [settings.gradle.kts](file:///d:/Projects2026/dsaapp/settings.gradle.kts)

Evidence:
In `.github/workflows/pr.yml`:
```yaml
- name: Build Ktor Backend
  run: ./gradlew :backend:build
```
In `settings.gradle.kts`:
`include(":app")` and other modules are included, but `:backend` is completely absent.

Current Behavior:
When a pull request triggers the CI pipeline, the job executes `./gradlew :backend:build` and terminates with a project resolution error.

Why This Is A Problem:
Blocks continuous integration; no pull requests can pass automated verification.

Potential Impact:
All pull requests fail CI automatically.

Trigger:
Every pull request event targeting `main`.

Recommended Solution:
Remove the `Build Ktor Backend` step from `.github/workflows/pr.yml` until the backend is integrated or migrated to a Cloudflare Worker deployment workflow.

Implementation Complexity:
Low

Priority:
P0

Verification:
Run the PR check workflow in GitHub Actions and verify the build passes without referencing `:backend`.

Confidence:
Confirmed

---

## REL-01: Contest Alarms Silently Wiped on Device Reboot Due to Missing Boot Receiver

Severity:
Critical

Category:
Reliability

Location:
- [app/src/main/AndroidManifest.xml](file:///d:/Projects2026/dsaapp/app/src/main/AndroidManifest.xml)
- [core/notifications/src/main/java/com/mycodecalendar/core/notifications/ReminderScheduler.kt](file:///d:/Projects2026/dsaapp/core/notifications/src/main/java/com/mycodecalendar/core/notifications/ReminderScheduler.kt)

Evidence:
1. `AndroidManifest.xml` lacks `<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />`.
2. No BroadcastReceiver is registered for `android.intent.action.BOOT_COMPLETED`.
3. The app never reschedules alarms from the Room `reminders` table after phone reboot.

Current Behavior:
Android OS wipes all `AlarmManager` pending intents when the device shuts down or reboots. The app provides no recovery mechanism.

Why This Is A Problem:
Users setting reminders for coding contests taking place hours or days in the future will miss them if their phone restarts in the interim.

Potential Impact:
Silent failure of core reminder feature for all users upon device reboot.

Trigger:
Any user restarting or powering off their smartphone after scheduling a contest reminder.

Recommended Solution:
1. Add `RECEIVE_BOOT_COMPLETED` permission to `AndroidManifest.xml`.
2. Create a `BootCompletedReceiver : BroadcastReceiver` that queries the Room `reminders` table for active future reminders and reschedules them via `ReminderScheduler`.

Implementation Complexity:
Medium

Priority:
P0

Verification:
Set a reminder for 30 minutes in the future, reboot the device/emulator, inspect `adb shell dumpsys alarm`, and confirm the alarm is re-registered.

Confidence:
Confirmed

---

## REL-02: Calendar Export Triggers SecurityException Due to Missing Manifest Permissions

Severity:
Critical

Category:
Reliability

Location:
- [app/src/main/AndroidManifest.xml](file:///d:/Projects2026/dsaapp/app/src/main/AndroidManifest.xml)
- [core/calendar/src/main/java/com/mycodecalendar/core/calendar/CalendarContractManager.kt:L13-L55](file:///d:/Projects2026/dsaapp/core/calendar/src/main/java/com/mycodecalendar/core/calendar/CalendarContractManager.kt#L13-L55)

Evidence:
In `CalendarContractManager.kt`:
```kotlin
val uri: Uri? = cr.insert(CalendarContract.Events.CONTENT_URI, values)
```
In `AndroidManifest.xml`:
Neither `android.permission.READ_CALENDAR` nor `android.permission.WRITE_CALENDAR` is declared.

Current Behavior:
When the user taps "Add to Google Calendar", Android OS throws a `SecurityException` for missing calendar permissions. The operation fails permanently.

Why This Is A Problem:
The calendar integration advertised in the application [README.md](file:///d:/Projects2026/dsaapp/README.md#L98-L101) is completely broken in production.

Potential Impact:
Feature crash or silent failure for 100% of calendar export attempts.

Trigger:
Tapping "Export to Calendar" on any contest detail screen.

Recommended Solution:
1. Declare `READ_CALENDAR` and `WRITE_CALENDAR` in `AndroidManifest.xml`.
2. Implement Compose runtime permission requesting via `rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions())` before calling `CalendarContractManager`.

Implementation Complexity:
Low

Priority:
P0

Verification:
Tap "Export to Calendar" on an emulator, accept the runtime permission prompt, and verify the contest event appears in the Google Calendar app.

Confidence:
Confirmed
