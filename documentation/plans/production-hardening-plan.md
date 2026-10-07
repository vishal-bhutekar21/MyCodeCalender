# Production Hardening Plan

This document outlines the detailed security, reliability, and data-safety hardening procedures required to lock down the CodeCalendar infrastructure for commercial deployment.

---

## 1. Secrets Isolation & Key Rotation Runbook

### 1.1 Android Release Keystore Hardening
1. **Immediate Revocation**: Do not reuse the password `8261830043` committed in `app/build.gradle.kts`.
2. **Environment Variable Binding**:
   Update [app/build.gradle.kts](file:///d:/Projects2026/dsaapp/app/build.gradle.kts) to resolve signing credentials at build time:
   ```kotlin
   signingConfigs {
       create("release") {
           val keystorePath = System.getenv("KEYSTORE_FILE") ?: "release.jks"
           storeFile = file(keystorePath)
           storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
           keyAlias = System.getenv("KEY_ALIAS") ?: "key0"
           keyPassword = System.getenv("KEY_PASSWORD") ?: ""
           enableV1Signing = true
           enableV2Signing = true
       }
   }
   ```
3. **CI/CD Configuration**: Store the base64-encoded keystore file and passwords in GitHub Actions Repository Secrets:
   * `KEYSTORE_BASE64`
   * `KEYSTORE_PASSWORD`
   * `KEY_PASSWORD`
   * `KEY_ALIAS`

---

### 1.2 Razorpay Worker Secret Rotation
1. Log into the Razorpay Merchant Dashboard and generate a new API Key ID and Key Secret.
2. In the Cloudflare Dashboard / CLI for the account, execute:
   ```bash
   npx wrangler secret put RAZORPAY_KEY_SECRET --name razorpay-backend-worker
   ```
3. Deploy the updated worker script with all plaintext default secret strings stripped out.

---

## 2. Firebase Security Rules Hardening

Deploy the following hardened [firestore.rules](file:///d:/Projects2026/dsaapp/codecalendar-admin/firestore.rules) to prevent privilege escalation and enable feedback reporting:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Helper: strict super-admin verification requiring verified email
    function isAdmin() {
      return request.auth != null && (
        request.auth.token.admin == true || (
          request.auth.token.email_verified == true &&
          request.auth.token.email in [
            'vishal.bhutekar1@gmail.com',
            'vishalbhutekar33772@gmail.com'
          ]
        )
      );
    }

    // 1. Broadcasts: Public read, Admin write
    match /broadcasts/{broadcastId} {
      allow read: if true;
      allow write: if isAdmin();
    }

    // 2. Featured Materials: Public read, Admin write
    match /featured_materials/{materialId} {
      allow read: if true;
      allow write: if isAdmin();
    }

    // 3. Custom Contests: Public read, Admin write
    match /custom_contests/{contestId} {
      allow read: if true;
      allow write: if isAdmin();
    }

    // 4. Users Directory: Users read/write own doc; Admin reads all
    match /users/{userId} {
      allow read: if request.auth != null && (request.auth.uid == userId || isAdmin());
      allow write: if request.auth != null && (request.auth.uid == userId || isAdmin());

      match /connected_accounts/{platform} {
        allow read, write: if request.auth != null && (request.auth.uid == userId || isAdmin());
      }
    }

    // 5. Feedback & Bug Reports (Restored)
    match /feedback/{feedbackId} {
      allow create: if request.auth != null;
      allow read, update, delete: if isAdmin();
    }

    // 6. Deletion Requests (GDPR)
    match /deletion_requests/{requestId} {
      allow create: if request.auth != null;
      allow read, update, delete: if isAdmin();
    }

    // 7. Admin Settings: Public read, Admin write
    match /admin_settings/{settingId} {
      allow read: if true;
      allow write: if isAdmin();
    }
  }
}
```

---

## 3. Room Database Migration Stabilization

In [MyCodeCalendarDatabase.kt](file:///d:/Projects2026/dsaapp/core/database/src/main/java/com/mycodecalendar/core/database/MyCodeCalendarDatabase.kt):
1. Change `exportSchema = true`.
2. Configure schema export directory in `core/database/build.gradle.kts`:
   ```kotlin
   ksp {
       arg("room.schemaLocation", "$projectDir/schemas")
   }
   ```
3. Remove `.fallbackToDestructiveMigration()` from `MainActivity.kt`.
4. Define explicit migration paths for future releases to preserve user data.
