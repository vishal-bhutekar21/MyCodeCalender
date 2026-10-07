# Phase 2 Execution Plan: Data Integrity & Security Hardening (P1/P2)

## 1. Executive Summary
Phase 2 focuses on securing data flows, protecting client authentication states, closing administrative database rule gaps, and ensuring SQLite/Room database resilience.

---

## 2. Issues Addressed & Technical Specifications

### 2.1 DATA-02: Missing Security Rule for `/feedback` Collection
* **Affected Files**:
  * [codecalendar-admin/firestore.rules](file:///d:/Projects2026/dsaapp/codecalendar-admin/firestore.rules)
* **Root Cause**:
  The Android client allows users to submit bug reports and feedback to `/feedback`. However, `firestore.rules` has no match rule for `/feedback`, causing all submissions to fail with `PERMISSION_DENIED`.
* **Implementation Plan**:
  Add the following rule to `firestore.rules`:
  ```javascript
  // 7. User Feedback & Bug Reports: Authenticated user create, Admin read/update/delete
  match /feedback/{feedbackId} {
    allow create: if request.auth != null;
    allow read, update, delete: if isAdmin();
  }
  ```
* **Verification**:
  Submit a feedback document as an authenticated user; confirm success in Firestore emulator or test suite.

---

### 2.2 SEC-04: Remove Foreign Project Fallback from Admin Portal
* **Affected Files**:
  * [codecalendar-admin/src/services/firebase.ts](file:///d:/Projects2026/dsaapp/codecalendar-admin/src/services/firebase.ts)
* **Root Cause**:
  `firebase.ts` hardcoded fallback values pointing to `shetkari-mitra-7721` if environment variables were missing.
* **Implementation Plan**:
  1. Update `firebaseConfig` in `firebase.ts` to use explicit `mycodecalendar` defaults or require environment variables.
  2. Add sanity check warning in development mode if default credentials are detected.
* **Verification**:
  Verify the admin app does not target or leak third-party project IDs.

---

### 2.3 REL-03: Real Google Sign-In Credential Linking
* **Affected Files**:
  * [feature/onboarding/src/main/java/com/mycodecalendar/feature/onboarding/AuthScreen.kt](file:///d:/Projects2026/dsaapp/feature/onboarding/src/main/java/com/mycodecalendar/feature/onboarding/AuthScreen.kt)
* **Root Cause**:
  In `AuthScreen.kt`, after the user selected their Google account, the code called `auth.signInAnonymously()` instead of linking the real Google ID token. This resulted in an anonymous, ephemeral session that was lost if the app data cleared.
* **Implementation Plan**:
  1. Configure `GoogleSignInOptions` to request an ID token using `requestIdToken(context.getString(com.vishal.mycodecalendar.R.string.default_web_client_id))` when available.
  2. In the activity result launcher, obtain `account.idToken`.
  3. If `account.idToken != null`, authenticate via `GoogleAuthProvider.getCredential(idToken, null)` and call `auth.signInWithCredential(credential)`.
  4. If `idToken` is null (e.g. offline or unconfigured client ID), fall back gracefully to anonymous or profile-based login with a clear log warning.
* **Verification**:
  Sign in with Google, inspect `FirebaseAuth.getInstance().currentUser.isAnonymous` (must be `false`), and verify the Google provider is attached.

---

### 2.4 DATA-03: Room Database DAOs & Schema Integrity
* **Affected Files**:
  * [core/database/src/main/java/com/mycodecalendar/core/database/MyCodeCalendarDatabase.kt](file:///d:/Projects2026/dsaapp/core/database/src/main/java/com/mycodecalendar/core/database/MyCodeCalendarDatabase.kt)
  * [core/database/src/main/java/com/mycodecalendar/core/database/dao/ReminderDao.kt](file:///d:/Projects2026/dsaapp/core/database/src/main/java/com/mycodecalendar/core/database/dao/ReminderDao.kt)
* **Root Cause**:
  `ReminderEntity` was added to `MyCodeCalendarDatabase` entities in version 2, but no `ReminderDao` was declared on the abstract database class.
* **Implementation Plan**:
  1. Create `ReminderDao` interface with methods:
     - `insertOrUpdate(reminder: ReminderEntity)`
     - `getAllActiveReminders(): List<ReminderEntity>`
     - `getRemindersAfter(timestamp: Instant): List<ReminderEntity>`
     - `deleteByContestId(contestId: String)`
  2. Declare `abstract fun reminderDao(): ReminderDao` in `MyCodeCalendarDatabase`.
* **Verification**:
  Ensure database builds and provides full typed CRUD access to scheduled reminders.
