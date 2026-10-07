# Phase 3 Execution Plan: Cloudflare Edge API Gateway (P1)

## 1. Executive Summary
Phase 3 builds and configures a production-grade Cloudflare Worker (`codecalendar-gateway`) that solves the biggest performance and scalability issues discovered in the audit:
- Replaces the 15-second sequential client-side fetching across 6 external platforms (`PERF-01`).
- Shields clients from GitHub/Codeforces IP rate limits (`PERF-02`).
- Provides cached, normalized contest listings with edge latency (< 150ms).
- Gracefully falls back if any individual platform API is experiencing downtime.

---

## 2. Issues Addressed & Technical Specifications

### 2.1 CF-01 & PERF-01: Multi-Platform Aggregation Worker
* **Location**: `workers/codecalendar-gateway/`
* **Worker Responsibilities**:
  1. **Contest Aggregator**:
     - Fetches LeetCode (GraphQL contest query), Codeforces (`/api/contest.list`), CodeChef, and AtCoder.
     - Merges and normalizes them into unified schema (`ContestItem`).
     - Caches normalized JSON in Cloudflare KV / Cache API under key `contests:all:v1`.
  2. **Cron Trigger**:
     - Pre-fetches every 10 minutes (`*/10 * * * *`).
     - Background cache warming guarantees 0ms cold origin latency for incoming client requests.
  3. **Edge Caching**:
     - Serves `GET /v1/contests` with `Cache-Control: public, max-age=300, stale-while-revalidate=3600`.
     - When 1,000 Android clients open the app, 99.9% of requests hit the nearest Cloudflare PoP in < 50ms without touching upstream servers.

### 2.2 PERF-02 & PERF-03: User Profile Proxy & Shielding
* **Worker Endpoint**: `GET /v1/user/stats/:platform/:handle`
* **Implementation Plan**:
  1. When requested, checks KV cache `user:stats:${platform}:${handle}` (TTL 30 minutes).
  2. If cache miss:
     - For `github`: Calls GitHub public API with gateway User-Agent and optional server GitHub PAT to bypass the 60 req/hr IP limit.
     - For `codeforces`: Calls `codeforces.com/api/user.info`.
     - For `leetcode`: Executes GraphQL user public profile query.
  3. Stores response in KV for 30 minutes.
  4. Returns sanitized stats JSON to client.

### 2.3 Android Client Integration
* **Affected Files**:
  * [core/network/src/main/java/com/mycodecalendar/core/network/RemoteDataSource.kt](file:///d:/Projects2026/dsaapp/core/network/src/main/java/com/mycodecalendar/core/network/RemoteDataSource.kt)
  * [core/network/src/main/java/com/mycodecalendar/core/network/api/ContestApi.kt](file:///d:/Projects2026/dsaapp/core/network/src/main/java/com/mycodecalendar/core/network/api/ContestApi.kt)
* **Implementation Plan**:
  1. Add gateway configuration URL to `RemoteDataSource.kt` pointing to `https://codecalendar-gateway.vishalbhutekar.me/v1` (with fallback).
  2. When refreshing contests, first query the gateway endpoint.
  3. If gateway returns HTTP 200, return parsed contests instantly.
  4. If gateway fails (network error / HTTP 5xx), gracefully fall back to the existing platform-by-platform network calls.

---

## 3. Worker Project Structure
```text
workers/codecalendar-gateway/
├── wrangler.toml
├── package.json
├── tsconfig.json
└── src/
    ├── index.ts
    ├── aggregators/
    │   ├── leetcode.ts
    │   ├── codeforces.ts
    │   ├── codechef.ts
    │   └── atcoder.ts
    ├── services/
    │   ├── cache.ts
    │   └── statsProxy.ts
    └── types/
        └── index.ts
```

---

## 4. Verification Strategy
1. Run local/staging wrangler worker check.
2. Verify `GET /v1/contests` returns normalized JSON with proper cache headers.
3. Verify `GET /v1/user/stats/github/torvalds` returns user metrics.
4. Verify Android `RemoteDataSource` integrates gateway response and falls back gracefully when offline.
