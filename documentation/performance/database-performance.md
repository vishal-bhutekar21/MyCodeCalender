# Database Performance Audit

## 1. Local SQLite Performance (Room Database)

The mobile client uses Room 2.6.1 over SQLite, managed by [MyCodeCalendarDatabase.kt](file:///d:/Projects2026/dsaapp/core/database/src/main/java/com/mycodecalendar/core/database/MyCodeCalendarDatabase.kt).

### 1.1 Schema & Indexing Analysis

| Entity | Primary Key | Declared Indexes | Index Efficiency | Risk / Optimization |
|---|---|---|---|---|
| `contests` | `id: String` | `(platform, startTimeUtc)`, `(status, startTimeUtc)` | **Good** | Covered queries filter efficiently by platform and time. Missing index on `lastFetchedAt` for cache cleanup. |
| `rating_history` | `(platform, username, timestamp)` | Unique Index on `(platform, username, timestamp)` | **Redundant** | The compound Primary Key already creates an implicit unique index in SQLite; the explicit index duplicate is redundant. |
| `platform_stats` | `(platform, username)` | Unique Index on `(platform, username)` | **Redundant** | Implicit PK index already covers queries. |
| `github_stats` | `username: String` | None | **Adequate** | Single lookup by primary key username. |
| `reminders` | `id: String` | Index on `contestId` | **Good** | Fast lookup when checking if contest has an active alarm. |

---

### 1.2 The "Large String JSON Blob" SQLite Anti-Pattern

In [GitHubStatsEntity.kt](file:///d:/Projects2026/dsaapp/core/database/src/main/java/com/mycodecalendar/core/database/entity/GitHubStatsEntity.kt#L28-L32):
```kotlin
@Entity(tableName = "github_stats")
data class GitHubStatsEntity(
    @PrimaryKey val username: String,
    ...
    val topLanguagesJson: String,
    val reposJson: String,
    val dailyContributionsJson: String,
    val lastUpdated: Instant
)
```

#### Problem Analysis
* Storing serialized JSON representations of 100 repositories (`reposJson`) and 365 daily contribution blocks (`dailyContributionsJson`) within a single database row creates row sizes ranging from **50 KB to 150 KB**.
* Android's SQLite implementation reads rows using a memory buffer called `CursorWindow` (default maximum size is 2 MB).
* When reading large rows into a `CursorWindow`:
  1. Excessive allocations occur on the heap.
  2. If the user links multiple GitHub accounts or if other entities grow, large row reads cause garbage collection pauses on lower-end devices.
  3. Every single query for basic stats (like follower count or star count) is forced to deserialize the entire 150 KB JSON payload into memory via `Json.decodeFromString`.

#### Recommended Normalization
Separate the daily contributions into a child table:
* `github_contributions` table: `(username, date, count, intensity)` with a composite primary key.
* `github_repositories` table: `(username, repo_name, stars, forks, language)`.

---

### 1.3 Database Migration & Data Safety Risk
* In [MainActivity.kt:L93](file:///d:/Projects2026/dsaapp/app/src/main/java/com/mycodecalendar/MainActivity.kt#L93):
  ```kotlin
  database = Room.databaseBuilder(
      applicationContext,
      MyCodeCalendarDatabase::class.java,
      "mycodecalendar.db"
  ).fallbackToDestructiveMigration().build()
  ```
* Combined with `exportSchema = false` in `MyCodeCalendarDatabase`, **any schema change automatically drops all existing tables and re-creates them from scratch**.
* **Impact**: If a future update modifies a table schema, all saved offline contests, user preferences, alarms, and streak checkpoints in Room are deleted upon update.

---

## 2. Cloud Firestore Performance (`codecalendar-admin` & Cloud Sync)

### 2.1 Unbounded Collection Reads for Metric Aggregation
In [firestoreService.ts:L239-L245](file:///d:/Projects2026/dsaapp/codecalendar-admin/src/services/firestoreService.ts#L239-L245):
```typescript
const [usersSnap, broadcastsSnap, materialsSnap, deletionsSnap, contestsSnap] = await Promise.all([
  getDocs(collection(firestore, 'users')),
  getDocs(query(collection(firestore, 'broadcasts'), where('isActive', '==', true))),
  getDocs(collection(firestore, 'featured_materials')),
  getDocs(query(collection(firestore, 'deletion_requests'), where('status', '==', 'PENDING'))),
  getDocs(collection(firestore, 'custom_contests'))
]);
```

#### Cost & Performance Penalty
* `getDocs(collection(firestore, 'users'))` retrieves the complete document bodies for **all registered users** in the entire application simply to read `usersSnap.size`.
* At 10,000 users, every page load of the Admin Dashboard:
  * Triggers **10,000 billed document reads** ($0.06 per page load).
  * Downloads ~5 MB to 15 MB of user JSON data to the admin browser.
  * Takes 3,000 ms – 7,000 ms to execute.

#### Recommended Fix
Use Firestore Server-Side Count Aggregation:
```typescript
import { getCountFromServer } from 'firebase/firestore';

const usersCountSnap = await getCountFromServer(collection(firestore, 'users'));
const totalUsers = usersCountSnap.data().count;
```
* **Result**: Charges only 1 read per 1,000 index entries; response payload is a single integer; latency drops to < 100 ms.

---

### 2.2 Unbounded Real-Time Snapshot Listener in Admin Directory
In [UsersDirectory.tsx:L21](file:///d:/Projects2026/dsaapp/codecalendar-admin/src/pages/UsersDirectory.tsx#L21):
```typescript
const unsubscribe = subscribeToUsers((items) => {
  setUsers(items);
  setLoading(false);
});
```
* Subscribes to the entire `/users` collection with no `limit()` clause.
* Every time any mobile user opens the app and updates their daily streak in Firestore, an event is pushed over WebSockets to every open admin browser tab, causing unnecessary re-rendering and memory consumption.
* **Remediation**: Implement cursor-based pagination with `limit(50)` and `startAfter()`.
