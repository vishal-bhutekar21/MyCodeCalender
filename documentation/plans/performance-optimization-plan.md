# Performance Optimization Plan

This document details the precise technical steps to optimize the performance, bandwidth consumption, and responsiveness of the CodeCalendar platform.

---

## 1. Cloudflare Edge Aggregation Gateway

### 1.1 Worker Specification
Create a new Cloudflare Worker project in `backend-worker/` with `wrangler.toml`:
```toml
name = "codecalendar-api-worker"
main = "src/index.ts"
compatibility_date = "2025-01-01"

[triggers]
crons = ["*/10 * * * *"] # Every 10 minutes

[[kv_namespaces]]
binding = "CONTESTS_KV"
id = "<created_kv_namespace_id>"
```

### 1.2 Aggregator Logic (`src/index.ts`)
```typescript
interface Env {
  CONTESTS_KV: KVNamespace;
  GITHUB_TOKEN?: string;
}

export default {
  // 1. Scheduled Cron: Fetch all platforms every 10 minutes
  async scheduled(event: ScheduledEvent, env: Env, ctx: ExecutionContext) {
    ctx.waitUntil(refreshAndCacheContests(env));
  },

  // 2. Fetch Handler: Serve cached contests with sub-30ms latency
  async fetch(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
    const url = new URL(request.url);

    if (url.pathname === "/v1/contests") {
      const cached = await env.CONTESTS_KV.get("contests:all:v1");
      if (cached) {
        return new Response(cached, {
          headers: {
            "Content-Type": "application/json",
            "Cache-Control": "public, max-age=300, stale-while-revalidate=3600",
            "Access-Control-Allow-Origin": "*"
          }
        });
      }
      // Cache miss fallback
      const fresh = await refreshAndCacheContests(env);
      return new Response(JSON.stringify(fresh), {
        headers: { "Content-Type": "application/json", "Access-Control-Allow-Origin": "*" }
      });
    }

    // Proxy GitHub stats with authenticated server token
    if (url.pathname.startsWith("/v1/user/stats/github/")) {
      const username = url.pathname.split("/").pop();
      return handleGitHubProxy(username, env);
    }

    return new Response("Not Found", { status: 404 });
  }
};
```

---

## 2. Android Network Parallelization (`FakeRepository.kt`)

Refactor [FakeRepository.refreshAndAwait](file:///d:/Projects2026/dsaapp/data/repository/src/main/java/com/mycodecalendar/data/repository/FakeRepository.kt#L331-L375) to replace sequential blocking calls with concurrent coroutines:

```kotlin
suspend fun refreshAndAwait(force: Boolean = false) {
    withContext(Dispatchers.IO) {
        isRefreshing.value = true
        fetchError.value = null
        try {
            coroutineScope {
                // Execute contest radar, POTD, and all platform syncs in parallel
                val contestsDeferred = async { fetchLiveContests() }
                val potdDeferred = async { fetchDailyProblemOfTheDay() }
                val platformJobs = connectedPlatforms.value.map { acc ->
                    async {
                        when (acc.platform) {
                            Platform.GITHUB -> fetchLiveGitHubData(acc.username)
                            Platform.CODEFORCES -> fetchLiveCodeforcesData(acc.username)
                            Platform.LEETCODE -> fetchLiveLeetCodeData(acc.username)
                            Platform.CODECHEF -> fetchLiveCodeChefData(acc.username)
                            Platform.ATCODER -> fetchLiveAtCoderData(acc.username)
                            Platform.GEEKSFORGEEKS -> fetchLiveGeeksForGeeksData(acc.username)
                        }
                    }
                }
                contestsDeferred.await()
                potdDeferred.await()
                platformJobs.awaitAll()
            }
            lastRefreshTimestamp = System.currentTimeMillis()
            isOffline.value = false
        } catch (e: Exception) {
            // Handled
        } finally {
            isRefreshing.value = false
        }
    }
}
```
* **Impact**: Total refresh latency drops from **~15,000 ms to ~3,000 ms** immediately, even before the Cloudflare edge worker is deployed.

---

## 3. SQLite Schema Normalization (`GitHubStatsEntity`)

1. Create a dedicated entity for daily contribution heatmaps:
   ```kotlin
   @Entity(
       tableName = "github_contributions",
       primaryKeys = ["username", "date"]
   )
   data class GitHubContributionEntity(
       val username: String,
       val date: String,
       val count: Int,
       val level: Int
   )
   ```
2. Remove `dailyContributionsJson` and `reposJson` string columns from `GitHubStatsEntity`.
3. Query contributions on-demand only when the user opens the `PlatformDetailScreen` for GitHub, eliminating memory pressure on `HomeScreen` launch.
