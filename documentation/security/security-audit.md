# Security Audit

## 1. Executive Summary & Threat Classification

A comprehensive security audit of all application tiers, build configurations, client-side bundles, and Cloudflare workers was performed.

### Vulnerability Summary Table

| ID | Title | Severity | Impact | Location |
|---|---|---|---|---|
| **SEC-01** | Production Keystore Password Hardcoded in Plaintext | **Critical** | Attacker can sign malicious APK updates if keystore file is obtained. | [app/build.gradle.kts:L25-L27](file:///d:/Projects2026/dsaapp/app/build.gradle.kts#L25-L27) |
| **SEC-02** | Super-Admin Privilege Escalation via Unverified Email | **Critical** | Anyone registering an unverified account with an admin email gains full read/write access to all user data and CMS. | [codecalendar-admin/firestore.rules:L6-L15](file:///d:/Projects2026/dsaapp/codecalendar-admin/firestore.rules#L6-L15) |
| **SEC-03** | Razorpay Secret Fallback in Cloudflare Worker | **Critical** | Fallback key secret hardcoded in worker source code allows unauthorized payment verification. | `razorpay-backend-worker` script in live account |
| **SEC-04** | Client-Side Hardcoded Firebase Credentials & Admin Whitelist | **Medium** | Exposure of other project credentials (`shetkari-mitra-7721`) and admin email addresses. | [codecalendar-admin/src/services/firebase.ts:L7-L39](file:///d:/Projects2026/dsaapp/codecalendar-admin/src/services/firebase.ts#L7-L39) |
| **SEC-05** | Missing Firestore Security Rule for `/feedback` | **High** | All feedback and bug reports from users are blocked by Firestore's default deny rule. | [codecalendar-admin/firestore.rules](file:///d:/Projects2026/dsaapp/codecalendar-admin/firestore.rules) |
| **SEC-06** | Incomplete GDPR Data Deletion | **Medium** | Deletion requests leave orphaned subcollections and retain Firebase Auth accounts. | [firestoreService.ts:L208-L226](file:///d:/Projects2026/dsaapp/codecalendar-admin/src/services/firestoreService.ts#L208-L226) |

---

## 2. Detailed Threat Analysis

### 2.1 SEC-01: Production Keystore Passwords in Source Control
In [app/build.gradle.kts:L25-L27](file:///d:/Projects2026/dsaapp/app/build.gradle.kts#L25-L27):
```kotlin
storePassword = "8261830043"
keyAlias = "key0"
keyPassword = "8261830043"
```
* **Threat**: Committing passwords in plaintext into Git history compromises the cryptographic authenticity of the release channel. If the keystore is ever checked in or shared, any unauthorized third party can sign malicious APK binaries with the legitimate app identity.
* **Remediation**: Remove plaintext credentials immediately. Use environment variables read at build time:
  ```kotlin
  storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
  keyPassword = System.getenv("KEY_PASSWORD") ?: ""
  ```

---

### 2.2 SEC-02: Super-Admin Privilege Escalation in Firestore Rules
In [codecalendar-admin/firestore.rules:L6-L15](file:///d:/Projects2026/dsaapp/codecalendar-admin/firestore.rules#L6-L15):
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

#### Exploit Mechanism
1. The rule checks `request.auth.token.email in [...]` without verifying `request.auth.token.email_verified == true`.
2. By default, Firebase Email/Password Authentication issues a valid `request.auth` token immediately upon signup, **before** the user verifies their email address.
3. An attacker can register an email account using `admin@codecalendar.com` or `admin@mycodecalendar.app` in the mobile app or via standard Firebase Auth REST endpoints.
4. Because the email matches the string in the rules array, **Firestore grants the attacker full Super-Admin privileges**, permitting them to read and write all documents across `/broadcasts`, `/featured_materials`, `/custom_contests`, and `/users` (including all connected handles and personal data).

#### Required Remediation
Enforce email verification in Firestore rules:
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
Or preferably, assign custom user claims (`request.auth.token.admin == true`) via the Firebase Admin SDK.
