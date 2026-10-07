# System Architecture

## 1. Executive Architectural Overview

The **Code Calendar (MyCodeCalendar)** ecosystem is designed as a hybrid competitive programming companion application consisting of:
1. **An Android Mobile Client** (`com.vishal.mycodecalendar`): Built with Kotlin 2.0 and Jetpack Compose, targeting Android 8.0 (API 26) through Android 14 (API 34).
2. **A Web Admin Portal & CMS** (`codecalendar-admin`): Built with React 19, TypeScript, Vite, and Tailwind CSS, deployed or targeted for Vercel/Web hosting.
3. **Cloud & Edge Services**: Google Firebase (Firestore, Authentication, Cloud Storage) for administrative content management and dynamic updates, alongside references to a planned Cloudflare Worker edge API.
4. **Third-Party Upstream Data Providers**: Direct integration with LeetCode (GraphQL), Codeforces (REST), CodeChef (HTML Scraping & REST), AtCoder (Kenkoooo API & REST), GitHub (REST & community APIs), and GeeksforGeeks (HTML Scraping).

```
┌────────────────────────────────────────────────────────────────────────────┐
│                             SYSTEM TOPOLOGY                                │
└────────────────────────────────────────────────────────────────────────────┘

               ┌──────────────────────────────────────────────┐
               │         React Admin Portal (Vite)            │
               │   (codecalendar-admin / Super Admin CMS)     │
               └──────────────────────┬───────────────────────┘
                                      │ Firebase JS SDK
                                      │ (Firestore / Auth / Storage)
                                      ▼
               ┌──────────────────────────────────────────────┐
               │           Google Cloud / Firebase            │
               │  - Cloud Firestore (Live CMS & User Sync)    │
               │  - Firebase Authentication (Google / Email)  │
               │  - Firebase Storage (Banners & Thumbnails)   │
               └──────────────────────▲───────────────────────┘
                                      │
                                      │ Firebase Android SDK
                                      │ (Real-time Snapshots & Writes)
                                      │
┌─────────────────────────────────────┴──────────────────────────────────────┐
│                    Android Mobile Client (Compose / Room)                   │
│                                                                            │
│  ┌───────────────────────┐             ┌────────────────────────────────┐  │
│  │   UI & Presentation   │             │       Local Persistence        │  │
│  │   (Compose Screens)   │             │   - Room SQLite (9 Entities)   │  │
│  │   (Home, Contests,    │             │   - SharedPreferences          │  │
│  │    Platforms, Onboard)│             │     (Accounts, Streak, Watch)  │  │
│  └───────────┬───────────┘             └───────────────▲────────────────┘  │
│              │                                         │                   │
│              ▼                                         │                   │
│  ┌─────────────────────────────────────────────────────┴────────────────┐  │
│  │            Monolithic Data Layer (FakeRepository / 1980 LOC)         │  │
│  │            - In-Memory StateFlows (Contests, Stats, Streaks)         │  │
│  │            - Offline Room Seed & Writeback Engine                    │  │
│  │            - Direct Multi-Platform Network Dispatcher                │  │
│  └───────────────────────────────────┬──────────────────────────────────┘  │
│                                      │                                     │
│                                      ▼                                     │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │               RemoteDataSource (Ktor Client / Android Engine)        │  │
│  │               Base URL: https://api.mycodecalendar.com/v1 (Ghost)    │  │
│  └───────────────────────────────────┬──────────────────────────────────┘  │
└──────────────────────────────────────┼─────────────────────────────────────┘
                                       │
            Direct Outbound Requests   │ (No Edge Proxy / No Aggregation)
                                       ▼
  ┌───────────────┬───────────────┬───────────────┬───────────────┬───────────────┐
  │   LeetCode    │  Codeforces   │   CodeChef    │    AtCoder    │    GitHub     │
  │  (GraphQL)    │  (REST API)   │ (HTML Scrape) │ (Kenkoooo API)│  (REST / 60)  │
  └───────────────┴───────────────┴───────────────┴───────────────┴───────────────┘
```

---

## 2. Android Client Architecture: Claimed vs. Actual

### Documented Architecture
The repository [README.md](file:///d:/Projects2026/dsaapp/README.md#L106-L154) documents a 16-module Clean Architecture pattern with MVVM:
* `core/`: `designsystem`, `database`, `network`, `common`, `notifications`, `model`, `analytics`
* `domain/`: `model`, `repository`, `usecase`
* `data/`: `repository`, `local`, `remote`, `mapper`
* `feature/`: `home`, `contests`, `contestdetail`, `platformdetail`, `platforms`, `resources`, `settings`, `onboarding`
* Support modules: `sync`, `widget`, `backend`

### Actual Implementation Reality
Inspection of the Gradle modules and Kotlin source reveals major structural deviations:

| Module / Component | Documented Role | Actual Implementation Status | Divergence / Risk |
|---|---|---|---|
| `:data:repository` | Modular Clean Repositories | Implemented via [FakeRepository.kt](file:///d:/Projects2026/dsaapp/data/repository/src/main/java/com/mycodecalendar/data/repository/FakeRepository.kt) (1,980 lines) | Monolithic "God class" handling network, Room, SharedPreferences, mapping, and caching. |
| `:domain:repository` | Abstract Domain Contracts | Stub interface definitions ([RepositoryInterfaces.kt](file:///d:/Projects2026/dsaapp/domain/repository/src/main/java/com/mycodecalendar/domain/repository/RepositoryInterfaces.kt)) | Neither `FakeRepository` nor any class implements `PlatformRepository` or `ContestRepository`. |
| `:data:local`, `:data:remote`, `:data:mapper` | Segregated data sources | Empty module stubs | Only contain `build.gradle.kts`. No classes exist inside. |
| `:core:analytics` | Analytics abstraction | Empty module stub | Only contains `build.gradle.kts`. |
| `:widget` | Android Home Screen Widget | Empty module stub | Only contains `build.gradle.kts`. |
| `:backend` | Ktor microservice backend | Orphaned build artifacts | Contains compiled JAR/ZIP in `backend/build/`, but no source code, not in `settings.gradle.kts`. |
| `:sync` | Background synchronization | Dummy Worker stub | [ContestSyncWorker.kt](file:///d:/Projects2026/dsaapp/sync/src/main/java/com/mycodecalendar/sync/SyncManager.kt) contains only `println()` and is never scheduled. |

---

## 3. Web Admin CMS Architecture (`codecalendar-admin`)

Located in [codecalendar-admin/](file:///d:/Projects2026/dsaapp/codecalendar-admin):
* **Technology**: React 19.2.8, Vite 8.2.0, Tailwind CSS 3.4.19, TypeScript 6.0.2.
* **Authentication**: [AuthContext.tsx](file:///d:/Projects2026/dsaapp/codecalendar-admin/src/context/AuthContext.tsx) uses Firebase Google Sign-In (`signInWithPopup`). Authorization checks whether the signed-in user's email exists in the hardcoded list `ADMIN_WHITELIST` in [firebase.ts](file:///d:/Projects2026/dsaapp/codecalendar-admin/src/services/firebase.ts#L35).
* **Data Services**: [firestoreService.ts](file:///d:/Projects2026/dsaapp/codecalendar-admin/src/services/firestoreService.ts) performs direct CRUD operations against Firestore collections:
  * `broadcasts`: Live announcement banners for the Android Home Screen.
  * `featured_materials`: Curated DSA sheets, courses, and tools.
  * `custom_contests`: College hackathons and community contests.
  * `users`: Developer directory displaying synced handles, streaks, and device info.
  * `deletion_requests`: Account deletion tickets submitted via Google Play Data Safety compliance.

---

## 4. Cloud & Backend Infrastructure: Ghost Services & Reality

### The Disconnected Backend Endpoint
* In [RemoteDataSource.kt](file:///d:/Projects2026/dsaapp/core/network/src/main/java/com/mycodecalendar/core/network/RemoteDataSource.kt#L23), the base URL is configured as:
  ```kotlin
  private val baseUrl: String = "https://api.mycodecalendar.com/v1"
  ```
* **Reality**:
  1. `api.mycodecalendar.com` has **no active DNS record** and does not resolve.
  2. The Cloudflare zone under the authenticated account (`vishalbhutekar.me`) does not manage `mycodecalendar.com`.
  3. No Cloudflare Worker for `mycodecalendar` exists in the Cloudflare deployment environment.
  4. Compatibility methods like `getContests()` and `getPlatformStats()` that target `baseUrl` are bypassed in production flows; instead, the app falls back to direct client-side fetching.

### Upstream Service Direct Coupling
Because no central aggregation backend is functional, the Android client makes direct requests to 6 distinct platforms from end-user devices:
* **LeetCode**: Unauthenticated GraphQL queries to `https://leetcode.com/graphql`.
* **Codeforces**: REST queries to `https://codeforces.com/api/`.
* **CodeChef**: Direct HTML scraping of `https://www.codechef.com/users/{username}` via regex, with fallback to an unverified Vercel scraper (`https://codechef-api.vercel.app/`).
* **AtCoder**: Public JSON endpoint `https://atcoder.jp/users/{username}/history/json` and Kenkoooo API `https://kenkoooo.com/atcoder/resources/contests.json`.
* **GitHub**: Unauthenticated REST API calls to `https://api.github.com/users/{username}` and `https://api.github.com/users/{username}/repos`, plus a third-party contributions API (`https://github-contributions-api.jogruber.de/v4/`).
* **GeeksforGeeks**: Direct HTML scraping of `https://www.geeksforgeeks.org/user/{username}/` via regex.

---

## 5. Persistence Architecture

### Client-Side Database (Room SQLite)
Defined in [MyCodeCalendarDatabase.kt](file:///d:/Projects2026/dsaapp/core/database/src/main/java/com/mycodecalendar/core/database/MyCodeCalendarDatabase.kt):
* Database Version: `2`
* `exportSchema = false` (Schema migration history is not exported).
* **Entities**:
  1. `PlatformAccountEntity` — Stored user handle credentials.
  2. `PlatformStatsEntity` — Aggregated ratings and rankings.
  3. `RatingHistoryEntity` — Historical contest rating points for chart plotting.
  4. `ContestEntity` — Cached contest radar list.
  5. `SavedContestEntity` — User bookmarked contests.
  6. `ReminderEntity` — Scheduled contest alarm records.
  7. `ResourceEntity` — Curated study sheets and platforms.
  8. `SyncStateEntity` — Timestamp checkpoints for sync freshness.
  9. `GitHubStatsEntity` — GitHub user statistics, including serialized JSON blobs (`topLanguagesJson`, `reposJson`, `dailyContributionsJson`).

### Client-Side Key-Value Storage (SharedPreferences)
The mobile app relies heavily on private SharedPreferences files alongside Room:
* `"platform_accounts"`: Fast access to connected handles.
* `"app_streak_prefs"`: Daily app open calculation, active date sets, and milestone triggers.
* `"contest_watchlist_prefs"`: Set of favorited contest IDs.
* `"app_auth_prefs"`: Authentication session tokens, user profile metadata, and theme preferences.
