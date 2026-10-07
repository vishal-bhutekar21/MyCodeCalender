# Authentication & Authorization Audit

## 1. Mobile Client Authentication Flow

The Android authentication lifecycle is driven by [AuthScreen.kt](file:///d:/Projects2026/dsaapp/feature/onboarding/src/main/java/com/mycodecalendar/feature/onboarding/AuthScreen.kt) and [MainActivity.kt](file:///d:/Projects2026/dsaapp/app/src/main/java/com/vishal/mycodecalendar/MainActivity.kt).

### 1.1 The "Anonymous Google Sign-In" Anti-Pattern
```kotlin
// AuthScreen.kt lines 145-147
loadingMessage = "Welcome, $displayName…"
auth.signInAnonymously().addOnCompleteListener { authTask ->
    val user = auth.currentUser
    user?.updateProfile(...)
```

#### Flaw Mechanics
* The user interacts with Google's native account chooser and selects their real Google identity.
* The application discards the Google authentication token and issues an unlinked, anonymous Firebase session (`signInAnonymously()`).
* **Consequences**:
  1. If anonymous authentication is disabled on the Firebase Console project, Google Sign-In fails immediately with an authentication exception.
  2. The generated UID has no association with the user's permanent Google ID.
  3. Reinstalling the app generates a different anonymous UID, orphaning all previously synced contest bookmarks and streaks in Firestore.

#### Required Remediation
Exchange the Google ID token for a Firebase `AuthCredential`:
```kotlin
val credential = GoogleAuthProvider.getCredential(googleAccount.idToken, null)
auth.signInWithCredential(credential).addOnCompleteListener { ... }
```

---

## 2. Web Admin Authorization (`codecalendar-admin`)

In [AuthContext.tsx](file:///d:/Projects2026/dsaapp/codecalendar-admin/src/context/AuthContext.tsx) and [firebase.ts](file:///d:/Projects2026/dsaapp/codecalendar-admin/src/services/firebase.ts):

### 2.1 Client-Side Privilege Validation
```typescript
// AuthContext.tsx line 23
const email = user?.email?.toLowerCase().trim() || '';
const isAdmin = Boolean(email && ADMIN_WHITELIST.includes(email));
```
* **Security Finding**: `isAdmin` is computed exclusively in client memory inside the browser bundle.
* **Risk**: Any user can modify JavaScript variables in DevTools or override `useAuth().isAdmin` to render administrative CMS pages.
* **Mitigating Factor**: Firestore writes are checked on the server by `firestore.rules`. However, because `firestore.rules` suffers from **SEC-02** (checking email strings without `email_verified == true`), the server check is also vulnerable to spoofing.

---

## 3. Firestore Security Rules Deep-Dive

Auditing [codecalendar-admin/firestore.rules](file:///d:/Projects2026/dsaapp/codecalendar-admin/firestore.rules):

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

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

### Analysis by Rule Section

| Collection | Rule | Security Assessment | Vulnerability / Defect |
|---|---|---|---|
| `/broadcasts/{id}` | Read: `true`, Write: `isAdmin()` | **Vulnerable to SEC-02** | Unverified accounts matching email can publish broadcast banners to all mobile devices. |
| `/featured_materials/{id}`| Read: `true`, Write: `isAdmin()` | **Vulnerable to SEC-02** | Unverified admin can alter study sheets, insert phishing links. |
| `/custom_contests/{id}` | Read: `true`, Write: `isAdmin()` | **Vulnerable to SEC-02** | Unauthorized users can publish fraudulent hackathons. |
| `/users/{userId}` | Read/Write: `auth.uid == userId \|\| isAdmin()` | **Vulnerable to SEC-02 & Query Denial** | Collection queries by email throw permission denial; unverified admin can view all users. |
| `/deletion_requests/{id}` | Create: `auth != null`, Read/Write: `isAdmin()` | **Medium Risk** | Any authenticated user can create deletion requests without validation. |
| `/feedback/{id}` | **UNDEFINED (Default Deny)** | **Broken Feature** | Android app calls `.collection("feedback").add()`, which is permanently denied by Firestore. |

---

## 4. Firebase Storage Security Rules

In [codecalendar-admin/storage.rules](file:///d:/Projects2026/dsaapp/codecalendar-admin/storage.rules):
```javascript
match /cms_images/{allPaths=**} {
  allow read: if true;
  allow write: if request.auth != null && (
    request.auth.token.email in [
      'vishal.bhutekar1@gmail.com',
      'admin@mycodecalendar.app'
    ]
  ) && request.resource.size < 5 * 1024 * 1024;
}
```

### Discrepancy & Security Risk
1. **Email List Contradiction**: `storage.rules` permits only 2 emails, whereas `firestore.rules` permits 4 emails. Admins logged in with `vishalbhutekar33772@gmail.com` or `admin@codecalendar.com` can create CMS records in Firestore, but their image uploads to Firebase Storage fail with `Permission Denied`.
2. **Missing Verification Check**: Fails to check `email_verified == true`.
