# High-Priority Issues (P1 — Critical Before Scale)

This document contains deep technical specifications for all **P1 (Critical Before Scale)** issues that cause severe performance degradation, reliability failures, or scalability bottlenecks.

---

## CF-01: Ghost Cloudflare Backend Domain api.mycodecalendar.com Does Not Resolve

Severity:
High

Category:
Cloudflare

Location:
- [core/network/src/main/java/com/mycodecalendar/core/network/RemoteDataSource.kt:L23](file:///d:/Projects2026/dsaapp/core/network/src/main/java/com/mycodecalendar/core/network/RemoteDataSource.kt#L23)
- Cloudflare Zone Management API

Evidence:
`RemoteDataSource.baseUrl = "https://api.mycodecalendar.com/v1"`.
DNS resolution test for `api.mycodecalendar.com` returns NXDOMAIN. The Cloudflare account holds no zone or worker for `mycodecalendar`.

Current Behavior:
Methods in `RemoteDataSource` that attempt to call `baseUrl/contests` fail immediately. The app falls back to direct client-side scraping.

Why This Is A Problem:
The mobile client bypasses the edge layer completely, losing all benefits of caching, rate-limit shielding, and centralized normalization.

Potential Impact:
Application is vulnerable to upstream rate limiting and cannot centrally control contest data.

Trigger:
Any attempt to call `RemoteDataSource.getContests()`.

Recommended Solution:
Deploy a Cloudflare Worker on an active zone (e.g. `api.vishalbhutekar.me` or purchase/configure `mycodecalendar.com`), implementing edge contest aggregation with KV caching.

Implementation Complexity:
High

Priority:
P1

Verification:
Verify DNS resolution of the API endpoint and confirm HTTP 200 JSON responses from edge cache.

Confidence:
Confirmed

---

## PERF-01: Direct Multi-Platform Network Waterfall Causes 15s Latency

Severity:
High

Category:
Performance

Location:
[data/repository/src/main/java/com/mycodecalendar/data/repository/FakeRepository.kt:L331-L375](file:///d:/Projects2026/dsaapp/data/repository/src/main/java/com/mycodecalendar/data/repository/FakeRepository.kt#L331-L375)

Evidence:
`refreshAndAwait()` executes 4 contest requests sequentially, followed by sequential loops over each connected platform handle.

Current Behavior:
On mobile networks, a full refresh takes 6 to 15 seconds to finish, during which the pull-to-refresh spinner is held open and multiple radio transitions occur.

Why This Is A Problem:
Severely impacts user experience, drains battery, and increases risk of network socket timeouts.

Potential Impact:
High user drop-off due to sluggish responsiveness; frequent "No internet" error banners on slow connections.

Trigger:
Every pull-to-refresh action or 5-minute background polling loop.

Recommended Solution:
1. Parallelize client requests using `kotlinx.coroutines.async` and `awaitAll()`.
2. Move contest aggregation to a Cloudflare Worker edge endpoint so the client requires only a single request.

Implementation Complexity:
Medium

Priority:
P1

Verification:
Benchmark network refresh duration with Android Profiler; verify latency drops below 2 seconds.

Confidence:
Confirmed

---

## PERF-02: Unauthenticated Client-Side GitHub API Requests Exhaust 60 req/hr Rate Limit

Severity:
High

Category:
Performance

Location:
[core/network/src/main/java/com/mycodecalendar/core/network/RemoteDataSource.kt:L239-L264](file:///d:/Projects2026/dsaapp/core/network/src/main/java/com/mycodecalendar/core/network/RemoteDataSource.kt#L239-L264)

Evidence:
Calls `https://api.github.com/users/{username}` and `.../repos?per_page=100` without an `Authorization` header.

Current Behavior:
Subject to GitHub's strict 60 requests/hour unauthenticated rate limit per public IP address.

Why This Is A Problem:
Multiple users connecting from the same Wi-Fi network (college hostels, labs, offices) share a single public IP, exhausting the quota in minutes.

Potential Impact:
GitHub heatmap and repositories fail to load with HTTP 403 Forbidden for all users on shared networks.

Trigger:
More than 2–3 active users on the same Wi-Fi network within an hour.

Recommended Solution:
Proxy GitHub API requests through a Cloudflare Worker that attaches an authenticated server token and caches user responses in Cloudflare KV for 1 hour.

Implementation Complexity:
Medium

Priority:
P1

Verification:
Execute 70 consecutive requests through the proxy and confirm no 403 rate-limit errors occur.

Confidence:
Confirmed

---

## PERF-03: Fragile Client-Side HTML Web Scraping for CodeChef and GeeksforGeeks Profiles

Severity:
High

Category:
Reliability

Location:
- [core/network/src/main/java/com/mycodecalendar/core/network/RemoteDataSource.kt:L177-L206](file:///d:/Projects2026/dsaapp/core/network/src/main/java/com/mycodecalendar/core/network/RemoteDataSource.kt#L177-L206)
- [core/network/src/main/java/com/mycodecalendar/core/network/RemoteDataSource.kt:L363-L398](file:///d:/Projects2026/dsaapp/core/network/src/main/java/com/mycodecalendar/core/network/RemoteDataSource.kt#L363-L398)

Evidence:
```kotlin
val html: String = client.get("https://www.codechef.com/users/$username").body()
val ratingRegex = Regex("""class="rating-number">(\s*\d+)""")
val solvedRegex = Regex("""Total Problems Solved:\s*(\d+)""")
```

Current Behavior:
Downloads full HTML pages (500 KB to 1 MB) directly to the phone and parses fields using regular expressions.

Why This Is A Problem:
Any minor update to CodeChef or GeeksforGeeks CSS classes, DOM structure, or Cloudflare bot challenges completely breaks profile stat fetching. Fixing it requires rolling out a full Android update through the Google Play Store.

Potential Impact:
Persistent 0-rating display and sync crashes for CodeChef and GeeksforGeeks profiles.

Trigger:
Any layout or class name update on CodeChef or GeeksforGeeks.

Recommended Solution:
Move all scraping to a Cloudflare Worker edge function. Edge functions can be updated and redeployed in seconds via `wrangler deploy` without requiring any client APK updates.

Implementation Complexity:
Medium

Priority:
P1

Verification:
Verify user profile metrics are extracted accurately and resiliently at the edge.

Confidence:
Confirmed

---

## DATA-02: Missing Security Rule for /feedback Collection Rejects User Reports

Severity:
High

Category:
Security

Location:
- [codecalendar-admin/firestore.rules](file:///d:/Projects2026/dsaapp/codecalendar-admin/firestore.rules)
- [app/src/main/java/com/vishal/mycodecalendar/CloudAdminSyncService.kt:L177-L209](file:///d:/Projects2026/dsaapp/app/src/main/java/com/vishal/mycodecalendar/CloudAdminSyncService.kt#L177-L209)

Evidence:
In `CloudAdminSyncService.kt`:
`firestore.collection("feedback").add(feedbackData)`
In `firestore.rules`:
No rule exists for `match /feedback/{feedbackId}`.

Current Behavior:
Because Firestore defaults to "deny all" for undeclared collections, every feedback and bug report submission from the mobile app is rejected with `PERMISSION_DENIED`.

Why This Is A Problem:
Users attempting to report issues or bugs cannot submit them; tickets are lost silently.

Potential Impact:
100% failure rate for user feedback and bug reporting features.

Trigger:
User submits a bug report or feedback from app settings.

Recommended Solution:
Add security rule in `firestore.rules`:
```javascript
match /feedback/{feedbackId} {
  allow create: if request.auth != null;
  allow read, update, delete: if isAdmin();
}
```

Implementation Complexity:
Low

Priority:
P1

Verification:
Submit feedback from the Android app and verify document creation succeeds in Firestore.

Confidence:
Confirmed

---

## SEC-03: Plaintext Razorpay Secret Key Fallback in Cloudflare Worker

Severity:
High

Category:
Security

Location:
Deployed Cloudflare Worker script: `razorpay-backend-worker`

Evidence:
`const keySecret = env.RAZORPAY_KEY_SECRET || "HEPFlUd1FozZEDNXnDa1FITL";`

Current Behavior:
The production payment verification worker embeds a plaintext API secret key fallback directly in its JavaScript source code.

Why This Is A Problem:
Exposes secret keys to anyone with script access and risks unauthorized payment spoofing.

Potential Impact:
Fraudulent payment verification and financial exposure.

Trigger:
Whenever `RAZORPAY_KEY_SECRET` is unset or inspectable.

Recommended Solution:
Rotate the Razorpay secret key in the Razorpay Dashboard; remove the fallback string from the script and bind exclusively via `wrangler secret put RAZORPAY_KEY_SECRET`.

Implementation Complexity:
Low

Priority:
P1

Verification:
Verify worker verifies payments using encrypted secret binding only.

Confidence:
Confirmed

---

## PERF-04: Unbounded Snapshot Listener on /users in Admin CMS

Severity:
High

Category:
Performance

Location:
[codecalendar-admin/src/pages/UsersDirectory.tsx:L21](file:///d:/Projects2026/dsaapp/codecalendar-admin/src/pages/UsersDirectory.tsx#L21)

Evidence:
```typescript
const unsubscribe = subscribeToUsers((items) => {
  setUsers(items);
  setLoading(false);
});
```

Current Behavior:
Attaches a real-time WebSocket snapshot listener to the entire `/users` collection without pagination limits.

Why This Is A Problem:
At 5,000+ users, loads all documents into browser RAM; every user streak update triggers a re-render of the entire directory table in the admin browser.

Potential Impact:
Admin browser crashes; massive Firestore billing from continuous snapshot document transfers.

Trigger:
Navigating to the "Users Directory" page in the Admin CMS.

Recommended Solution:
Replace real-time collection subscription with paginated `getDocs(query(collection(firestore, 'users'), limit(50)))` with search and cursor navigation.

Implementation Complexity:
Medium

Priority:
P1

Verification:
Verify directory loads only 50 records per page and supports next/previous cursor pagination.

Confidence:
Confirmed

---

## PERF-05: Full Collection Document Downloads in Admin Dashboard Metric Aggregations

Severity:
High

Category:
Performance

Location:
[codecalendar-admin/src/services/firestoreService.ts:L237-L245](file:///d:/Projects2026/dsaapp/codecalendar-admin/src/services/firestoreService.ts#L237-L245)

Evidence:
`getDocs(collection(firestore, 'users'))` called solely to read `usersSnap.size`.

Current Behavior:
Downloads all document bodies over the network to compute user count.

Why This Is A Problem:
High latency and high Firestore read costs ($0.06 per 100k reads). At 10,000 users, each dashboard visit costs 10,000 reads.

Potential Impact:
Slow dashboard loading; unexpected monthly Firebase billing charges.

Trigger:
Every time an admin loads the Admin CMS Overview page.

Recommended Solution:
Use `getCountFromServer(collection(firestore, 'users'))` which performs server-side aggregation and costs only 1 read per 1,000 index entries.

Implementation Complexity:
Low

Priority:
P1

Verification:
Inspect network panel in DevTools and confirm response payload is a single integer count object.

Confidence:
Confirmed

---

## REL-03: Google Sign-In Issues Anonymous Firebase Sessions

Severity:
High

Category:
Reliability

Location:
[feature/onboarding/src/main/java/com/mycodecalendar/feature/onboarding/AuthScreen.kt:L145-L161](file:///d:/Projects2026/dsaapp/feature/onboarding/src/main/java/com/mycodecalendar/feature/onboarding/AuthScreen.kt#L145-L161)

Evidence:
`auth.signInAnonymously()` executed after Google account selection rather than linking the Google AuthCredential.

Current Behavior:
Creates an ephemeral anonymous Firebase session.

Why This Is A Problem:
User identities cannot be restored across devices or upon reinstallation, defeating the purpose of Google Sign-In.

Potential Impact:
User accounts and streaks are permanently lost when clearing data or switching phones.

Trigger:
Any user signing in via Google.

Recommended Solution:
Retrieve the Google ID token and call `auth.signInWithCredential(GoogleAuthProvider.getCredential(account.idToken, null))`.

Implementation Complexity:
Medium

Priority:
P1

Verification:
Sign in on Device A, note UID in Firebase console, sign in on Device B with the same Google email, and verify the identical UID is returned.

Confidence:
Confirmed

---

## REL-04: Background ContestSyncWorker is an Inactive Placeholder

Severity:
High

Category:
Reliability

Location:
- [sync/src/main/java/com/mycodecalendar/sync/SyncManager.kt](file:///d:/Projects2026/dsaapp/sync/src/main/java/com/mycodecalendar/sync/SyncManager.kt)

Evidence:
`ContestSyncWorker.doWork()` contains only `println("Background ContestSyncWorker executing successfully.")` and is never scheduled anywhere in the project.

Current Behavior:
No background synchronization takes place.

Why This Is A Problem:
Contest radar data and widget information become stale unless the user manually opens the app in the foreground.

Potential Impact:
Users miss contest notifications because background sync never refreshes new contest additions.

Trigger:
Continuous app operation while in background.

Recommended Solution:
1. Implement real sync logic in `ContestSyncWorker`: call repository refresh and update Room DB.
2. Schedule periodic work via `SyncManager` inside `MainActivity.onCreate()` or `Application.onCreate()`.

Implementation Complexity:
Medium

Priority:
P1

Verification:
Simulate background WorkManager execution via `adb shell cmd jobscheduler run` and verify Room database receives fresh contests.

Confidence:
Confirmed

---

## OBS-01: Total Absence of Production Crash Reporting and Telemetry

Severity:
High

Category:
Observability

Location:
[app/build.gradle.kts](file:///d:/Projects2026/dsaapp/app/build.gradle.kts)

Evidence:
Firebase Crashlytics, Sentry, and Bugsnag dependencies are completely absent.

Current Behavior:
Unhandled exceptions terminate the app with zero remote telemetry.

Why This Is A Problem:
The engineering team cannot diagnose production issues, track crash-free user rates, or respond to regressions.

Potential Impact:
Silent bugs remain undetected in production until negative Play Store reviews are posted.

Trigger:
Any production crash or unexpected runtime exception.

Recommended Solution:
Add `firebase-crashlytics` plugin and dependency to `app/build.gradle.kts`; initialize Crashlytics in the application entry point.

Implementation Complexity:
Medium

Priority:
P1

Verification:
Trigger a test crash via `FirebaseCrashlytics.getInstance().recordException(...)` and verify stack trace appears in the Firebase Console within 5 minutes.

Confidence:
Confirmed
