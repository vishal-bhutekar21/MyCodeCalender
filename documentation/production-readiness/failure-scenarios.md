# Production Failure Scenarios & Resilience Matrix

This matrix evaluates how the system behaves under 12 critical production failure conditions.

---

## 1. Upstream Contest Platform Outage (e.g. Codeforces 503 / LeetCode 500)
1. **Current Behavior**: `remoteDataSource.fetchCodeforcesContests()` catches the HTTP 503 in `runCatching` and returns `Result.failure`.
2. **Is Handled?**: Yes, caught at repository layer.
3. **Data Inconsistent?**: No; existing Room DB contests remain untouched.
4. **Client Error Message?**: In `FakeRepository.refreshAndAwait`, if any critical call throws, `fetchError` displays: `"Refresh failed: Codeforces API returned non-OK status"`.
5. **Retrying Safe?**: Yes (read-only query).
6. **Duplicate Operations?**: No.
7. **Idempotency?**: Naturally idempotent.
8. **Timeout?**: Yes, 12 seconds (`HttpTimeout` in Ktor).
9. **Fallback?**: Shows previous cached contests from Room DB.
10. **Target Production Behavior**: Cloudflare edge worker should serve stale cached data (`stale-while-revalidate`) with zero user-facing error.

---

## 2. Upstream Scraper DOM / CSS Change (CodeChef / GeeksforGeeks)
1. **Current Behavior**: Regex matching fails to match (`ratingRegex.find(html)` returns null); throws `Exception("CodeChef user '$username' not found")`.
2. **Is Handled?**: Caught in `runCatching`.
3. **Data Inconsistent?**: No, but user profile stats show 0 or remain un-synced.
4. **Client Error Message?**: User sees generic "Refresh failed" error.
5. **Retrying Safe?**: Yes, but repeatedly fails until code is patched.
6. **Duplicate Operations?**: No.
7. **Idempotency?**: Yes.
8. **Timeout?**: 12 seconds.
9. **Fallback?**: CodeChef falls back to `https://codechef-api.vercel.app/` which often fails with 402/429.
10. **Target Production Behavior**: Scraper should run on Cloudflare Worker; edge updates can be deployed in minutes without releasing an Android APK update.

---

## 3. GitHub API 403 Rate Limit Exceeded
1. **Current Behavior**: Returns HTTP 403 with `{"message": "API rate limit exceeded for..."}`; Ktor deserialization fails or returns non-200.
2. **Is Handled?**: Caught in `runCatching`.
3. **Data Inconsistent?**: No.
4. **Client Error Message?**: "Refresh failed".
5. **Retrying Safe?**: Safe, but fails repeatedly until the 1-hour window resets.
6. **Duplicate Operations?**: No.
7. **Idempotency?**: Yes.
8. **Timeout?**: 12 seconds.
9. **Fallback?**: Shows cached `GitHubStatsEntity` from Room if previously fetched.
10. **Target Production Behavior**: Authenticated edge proxy with server token and 30-minute global caching.

---

## 4. Device Restarts / Power Off (Contest Alarms)
1. **Current Behavior**: Android OS wipes all `AlarmManager` timers. No boot receiver exists.
2. **Is Handled?**: **No.**
3. **Data Inconsistent?**: Yes. Room DB shows `ReminderEntity` exists, but Android system has no corresponding alarm scheduled.
4. **Client Error Message?**: None (silent failure).
5. **Retrying Safe?**: N/A.
6. **Duplicate Operations?**: N/A.
7. **Idempotency?**: N/A.
8. **Timeout?**: N/A.
9. **Fallback?**: None.
10. **Target Production Behavior**: Implement `BootCompletedReceiver` to read Room `reminders` table and reschedule all future alarms upon `ACTION_BOOT_COMPLETED`.

---

## 5. Calendar Export Attempt (Missing Android Permissions)
1. **Current Behavior**: `cr.insert(CalendarContract.Events.CONTENT_URI, values)` triggers `SecurityException`.
2. **Is Handled?**: Caught in `try-catch` inside `CalendarContractManager.kt`, returning `Result.failure(e)`.
3. **Data Inconsistent?**: Event is not added to calendar; UI button state does not update.
4. **Client Error Message?**: Button reverts without descriptive explanation.
5. **Retrying Safe?**: Safe, but fails permanently.
6. **Duplicate Operations?**: No.
7. **Idempotency?**: Has `findExistingEventId()` check.
8. **Timeout?**: Immediate.
9. **Fallback?**: None.
10. **Target Production Behavior**: Declare `READ_CALENDAR` and `WRITE_CALENDAR` in manifest; request runtime permission prompt in Compose before calling manager.

---

## 6. User Signs In with Existing Account (Data Loss Scenario)
1. **Current Behavior**: [MainActivity.kt:L440](file:///d:/Projects2026/dsaapp/app/src/main/java/com/vishal/mycodecalendar/MainActivity.kt#L440) calls `repository.clearAllUserData()`, then calls `syncUserProfileToCloud` with empty platforms list.
2. **Is Handled?**: **No. This is a critical logical bug.**
3. **Data Inconsistent?**: **Yes. Cloud document `/users/{uid}` has its platforms array overwritten with an empty list.**
4. **Client Error Message?**: None. User lands on home screen with wiped platform stats.
5. **Retrying Safe?**: Irreversible data overwrite in cloud.
6. **Duplicate Operations?**: No.
7. **Idempotency?**: Overwrite is destructive.
8. **Timeout?**: N/A.
9. **Fallback?**: None.
10. **Target Production Behavior**: Fetch cloud profile *before* clearing local state; merge cloud-stored platforms with local database.

---

## 7. User Submits Feedback / Bug Report (Missing Security Rule)
1. **Current Behavior**: `firestore.collection("feedback").add(feedbackData)` executes.
2. **Is Handled?**: [CloudAdminSyncService.kt:L205](file:///d:/Projects2026/dsaapp/app/src/main/java/com/vishal/mycodecalendar/CloudAdminSyncService.kt#L205) catches failure in `addOnFailureListener`.
3. **Data Inconsistent?**: Feedback ticket is dropped; never written to database.
4. **Client Error Message?**: "Error submitting feedback to cloud".
5. **Retrying Safe?**: Safe, but fails continuously.
6. **Duplicate Operations?**: No.
7. **Idempotency?**: Random document ID generated on add.
8. **Timeout?**: Default Firestore SDK timeout (60s).
9. **Fallback?**: None.
10. **Target Production Behavior**: Add `match /feedback/{id} { allow create: if true; allow read, update: if isAdmin(); }` to `firestore.rules`.

---

## 8. Network Drops Mid-Sync
1. **Current Behavior**: Active Ktor call throws `SocketException` / `UnknownHostException`. Caught in `refreshAndAwait`.
2. **Is Handled?**: Yes. `isOffline.value` set to `true`.
3. **Data Inconsistent?**: Partial sync: platforms queried before drop are cached in Room; subsequent platforms remain at previous state.
4. **Client Error Message?**: "No internet — showing cached data".
5. **Retrying Safe?**: Yes.
6. **Duplicate Operations?**: Room inserts use `OnConflictStrategy.REPLACE`.
7. **Idempotency?**: Yes.
8. **Timeout?**: 12s socket/connect timeout.
9. **Fallback?**: [NetworkMonitor.kt](file:///d:/Projects2026/dsaapp/core/common/src/main/java/com/mycodecalendar/core/common/NetworkMonitor.kt) listens to connectivity and automatically triggers `refreshAllData()` when connectivity is restored.
10. **Target Production Behavior**: Current offline banner and auto-reconnect behavior is well-designed.

---

## 9. Rapid Repeated Refresh Clicks (Throttling)
1. **Current Behavior**: `HomeViewModel.minManualRefreshIntervalMs = 30_000L` (30 seconds) and `FakeRepository.minRefreshIntervalMs = 30_000L`.
2. **Is Handled?**: Yes.
3. **Data Inconsistent?**: No.
4. **Client Error Message?**: Silent return (no-op).
5. **Retrying Safe?**: Yes.
6. **Duplicate Operations?**: Prevented by 30-second timestamp check.
7. **Idempotency?**: Yes.
8. **Timeout?**: N/A.
9. **Fallback?**: Current in-memory cache is maintained.
10. **Target Production Behavior**: Current 30-second throttle functions correctly.

---

## 10. Firebase Authentication Service Outage
1. **Current Behavior**: Firebase Auth methods throw `FirebaseNetworkException`.
2. **Is Handled?**: Caught in `AuthScreen.kt` and displayed as user error.
3. **Data Inconsistent?**: No.
4. **Client Error Message?**: "Network error. Please check your connection."
5. **Retrying Safe?**: Yes.
6. **Duplicate Operations?**: No.
7. **Idempotency?**: Yes.
8. **Timeout?**: Default Firebase timeout.
9. **Fallback?**: User can tap "Guest Access" button to bypass authentication.
10. **Target Production Behavior**: Guest mode provides an effective offline fallback.

---

## 11. Malformed JSON Response from Upstream Platform
1. **Current Behavior**: Ktor `ContentNegotiation` with `Json { ignoreUnknownKeys = true, isLenient = true, coerceInputValues = true }` attempts lenient parsing. If JSON structure changes drastically, throws `SerializationException`.
2. **Is Handled?**: Caught by `runCatching` in `RemoteDataSource`.
3. **Data Inconsistent?**: No; failing platform is omitted from results.
4. **Client Error Message?**: "Refresh failed: [Error details]".
5. **Retrying Safe?**: Yes.
6. **Duplicate Operations?**: No.
7. **Idempotency?**: Yes.
8. **Timeout?**: Immediate on parse.
9. **Fallback?**: Retains previously saved data in Room.
10. **Target Production Behavior**: Edge proxy should validate upstream schema and isolate malformed platform payloads.

---

## 12. Room SQLite Database Schema Change on App Update
1. **Current Behavior**: [MainActivity.kt:L93](file:///d:/Projects2026/dsaapp/app/src/main/java/com/vishal/mycodecalendar/MainActivity.kt#L93) uses `.fallbackToDestructiveMigration()`.
2. **Is Handled?**: Handled by Room, but **destructively**.
3. **Data Inconsistent?**: **All existing Room tables are dropped and recreated empty.**
4. **Client Error Message?**: None. User opens updated app to find all saved offline contests and reminder bookmarks gone.
5. **Retrying Safe?**: Irreversible data loss.
6. **Duplicate Operations?**: N/A.
7. **Idempotency?**: N/A.
8. **Timeout?**: N/A.
9. **Fallback?**: SharedPreferences data survives, but Room tables are wiped.
10. **Target Production Behavior**: Enable `exportSchema = true` and write explicit `Migration(1, 2)` / `AutoMigration` scripts.
