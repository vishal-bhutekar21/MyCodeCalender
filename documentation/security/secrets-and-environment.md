# Secrets & Environment Configuration Audit

## 1. Secrets Inventory & Exposure Assessment

| Tier / Location | Secret / Credential | Type | Exposure Mechanism | Severity |
|---|---|---|---|---|
| **Android Gradle** | `8261830043` | Release Keystore Password | Committed in plaintext in [app/build.gradle.kts:L25](file:///d:/Projects2026/dsaapp/app/build.gradle.kts#L25) | **Critical** |
| **Android Gradle** | `8261830043` | Release Key Password | Committed in plaintext in [app/build.gradle.kts:L27](file:///d:/Projects2026/dsaapp/app/build.gradle.kts#L27) | **Critical** |
| **Cloudflare Worker** | `HEPFlUd1FozZEDNXnDa1FITL` | Razorpay Key Secret | Committed in plaintext in `razorpay-backend-worker` JavaScript source | **Critical** |
| **Web Admin CMS** | `AIzaSyAj1PnhtZM1hq5zhnS8ujwGBZ3MT_6QFPg` | Firebase Web API Key | Hardcoded fallback in [codecalendar-admin/src/services/firebase.ts:L7](file:///d:/Projects2026/dsaapp/codecalendar-admin/src/services/firebase.ts#L7) | **Medium** |
| **Web Admin CMS** | 4 Super Admin Emails | Administrative Identifiers | Hardcoded in client bundle in [firebase.ts:L36](file:///d:/Projects2026/dsaapp/codecalendar-admin/src/services/firebase.ts#L36) | **Low** |

---

## 2. Environment Configuration Analysis

### 2.1 Android Environment Configuration
* **No `BuildConfig` Fields**: The Android application does not define environment-specific `buildConfigField` parameters in `app/build.gradle.kts`.
* **Hardcoded Base URL**: `RemoteDataSource(baseUrl = "https://api.mycodecalendar.com/v1")` is hardcoded as a default parameter in Kotlin, preventing build variants (e.g. `debug` vs `release` vs `staging`) from pointing to local emulators or staging servers.
* **Missing `google-services.json`**: The actual production Google services file is omitted from Git (only `google-services.json.example` is committed). Developers cloning the repo cannot run debug builds without manually providing this configuration file.

---

### 2.2 Web Admin CMS Environment Configuration (`codecalendar-admin`)
* **Environment File**: [codecalendar-admin/.env.example](file:///d:/Projects2026/dsaapp/codecalendar-admin/.env.example) outlines:
  ```env
  VITE_FIREBASE_API_KEY=
  VITE_FIREBASE_AUTH_DOMAIN=
  VITE_FIREBASE_PROJECT_ID=
  VITE_FIREBASE_STORAGE_BUCKET=
  VITE_FIREBASE_MESSAGING_SENDER_ID=
  VITE_FIREBASE_APP_ID=
  VITE_ADMIN_WHITELIST=
  ```
* **Dangerous Fallback Anti-Pattern**: In [firebase.ts](file:///d:/Projects2026/dsaapp/codecalendar-admin/src/services/firebase.ts#L6-L13):
  ```typescript
  const firebaseConfig = {
    apiKey: import.meta.env.VITE_FIREBASE_API_KEY || "AIzaSyAj1PnhtZM1hq5zhnS8ujwGBZ3MT_6QFPg",
    authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN || "shetkari-mitra-7721.firebaseapp.com",
    projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID || "shetkari-mitra-7721",
    storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET || "shetkari-mitra-7721.appspot.com",
    messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID || "333822226193",
    appId: import.meta.env.VITE_FIREBASE_APP_ID || "1:333822226193:web:3e6104ced5d469ac4aa6b0"
  };
  ```
* **Production Danger**: If an admin deployment on Vercel or Netlify is launched without configuring environment variables, the admin portal connects to the **`shetkari-mitra-7721`** database rather than CodeCalendar's production project. All CMS edits would be saved to the wrong Firebase tenant.

---

## 3. Remediation Roadmap for Secrets & Environment

1. **Purge Keystore Credentials from Git**:
   * Rotate the release keystore password.
   * Add `KEYSTORE_PASSWORD` and `KEY_PASSWORD` to GitHub Actions Secrets.
   * Update `app/build.gradle.kts` to read from environment variables or a local `local.properties` file that is ignored by Git.
2. **Remove Hardcoded Fallbacks in `firebase.ts`**:
   * Change initialization to fail loudly if variables are missing:
     ```typescript
     if (!import.meta.env.VITE_FIREBASE_PROJECT_ID) {
       throw new Error("Missing required VITE_FIREBASE_PROJECT_ID environment variable.");
     }
     ```
3. **Migrate Cloudflare Worker Secrets**:
   * Rotate the Razorpay key secret.
   * Strip default secret strings from all Cloudflare Worker code and configure them exclusively through `wrangler secret put`.
