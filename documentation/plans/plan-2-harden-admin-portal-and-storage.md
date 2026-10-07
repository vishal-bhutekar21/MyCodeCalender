# Plan 2: Harden Admin Portal & Cloud Storage

## 1. Objective
Eliminate all security vulnerabilities, query inefficiencies, missing permissions, and linter warnings in the CodeCalendar React Admin Web Portal (`codecalendar-admin`).

---

## 2. Issues Addressed & Technical Specifications

### 2.1 Missing Admin Emails in Storage Security Rules
* **File**: `codecalendar-admin/storage.rules`
* **Evidence**:
  * Lines 7–10: Only `vishal.bhutekar1@gmail.com` and `admin@mycodecalendar.app` are declared.
  * `vishalbhutekar33772@gmail.com` and `admin@codecalendar.com` are omitted, and `request.auth.token.email_verified == true` is missing.
* **Remediation**:
  Update `storage.rules` to match Firestore rules with verified email enforcement.

### 2.2 Unbounded User Directory Subscriptions
* **File**: `codecalendar-admin/src/services/firestoreService.ts`
* **Evidence**:
  * `subscribeToUsers` attaches an unbounded real-time snapshot listener on the entire `users` collection without a `limit()`.
* **Remediation**:
  Add `limit(100)` and ordering to `subscribeToUsers` so large user bases do not cause high memory usage or Firestore read spikes.

### 2.3 Oxlint Fast-Refresh Component Warning
* **File**: `codecalendar-admin/src/context/AuthContext.tsx`
* **Evidence**:
  * Line 85: Exporting both component and hook in the same file triggers React fast refresh warning.
* **Remediation**:
  Resolve hook export or file organization to achieve 0 lint warnings and 0 errors.

### 2.4 Verification Strategy
1. Run `npm run lint` in `codecalendar-admin` and verify 0 errors and 0 warnings.
2. Run `npm run build` in `codecalendar-admin` and verify clean Vite bundle generation.
