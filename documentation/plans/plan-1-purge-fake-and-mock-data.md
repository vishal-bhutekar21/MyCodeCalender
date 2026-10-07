# Plan 1: Purge Fake & Mock Data Across All Systems

## 1. Objective
Identify, isolate, and remove all fabricated, mock, and pseudo-generated data across the Android client, Cloudflare edge services, and the Admin CMS. Replace mock fallbacks with authentic offline-first cached states, clean zero-state indicators, or genuine upstream data.

---

## 2. Identified Fake Data Locations & Remediation

### 2.1 Fake GitHub Contributions & Stats Generator
* **File**: `data/repository/src/main/java/com/mycodecalendar/data/repository/FakeRepository.kt`
* **Evidence**:
  * Lines 1162–1176: `fetchFallbackGitHubStats` hardcodes `publicRepos = 14`, `totalStars = 6`, `followers = 12`, and `topLanguages = listOf("Kotlin", "Python", "TypeScript", "Java")`.
  * Lines 1255–1275: `generateFallbackDailyContributions(username)` generates fake commit counts `((seed + daysAgo) % 12).toInt()` and fabricates a 365-day commit heatmap using modulo hashing on the username string.
* **Remediation**:
  1. Eliminate `generateFallbackDailyContributions`. If network is unavailable and no Room database cache exists, return an empty `dailyContributions` list.
  2. In `fetchFallbackGitHubStats`, remove hardcoded integers (14 repos, 6 stars, 12 followers). If offline without cached data, emit a clean `null` or 0-count authentic state.
  3. Ensure the UI cleanly renders an authentic "Sync Pending / Offline" card rather than misleading fake commit charts.

### 2.2 Hardcoded Fallback Platform Stats
* **File**: `data/repository/src/main/java/com/mycodecalendar/data/repository/FakeRepository.kt`
* **Evidence**:
  * Line 1135: `generateFallbackStats(platform, username)` seeds the in-memory map with placeholder metrics (`rank = "Connecting..."`).
* **Remediation**:
  Ensure stats remain `null` until live network data or Room database entities are loaded.

### 2.3 Verification Strategy
1. Test offline mode on a freshly connected GitHub account. Verify the app displays "No offline data available. Connect to network to sync" instead of fabricated 14-repo statistics and fake heatmaps.
2. Verify Room database caching preserves authentic network responses.
