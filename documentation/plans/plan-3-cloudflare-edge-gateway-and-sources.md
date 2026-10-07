# Plan 3: Cloudflare Edge Gateway & Multi-Platform Sources

## 1. Objective
Validate, test, and integrate the Cloudflare Worker Gateway (`workers/codecalendar-gateway`) with upstream competitive programming platforms, ensuring high availability, edge caching, and seamless Android client routing.

---

## 2. Issues Addressed & Technical Specifications

### 2.1 Multi-Platform Source Integrity
* **Sources Tested**:
  * LeetCode GraphQL (`https://leetcode.com/graphql`)
  * Codeforces Official API (`https://codeforces.com/api/contest.list`)
  * CodeChef Contests API (`https://www.codechef.com/api/list/contests/all`)
  * AtCoder Contests JSON (`https://kenkoooo.com/atcoder/resources/contests.json`)
* **Remediation**:
  Ensure all 4 aggregators handle upstream response errors with `Promise.allSettled` and produce normalized `ContestItem` structures.

### 2.2 Cloudflare KV & Edge Cache API
* **Configuration**:
  * KV namespace: `CONTEST_CACHE` (key: `contests:all:v1`)
  * Cache TTL: 1 hour in KV, 300s edge `Cache-Control` header with `stale-while-revalidate=3600`.
  * Scheduled Cron: `*/10 * * * *` (10-minute warming).

### 2.3 User Stats Proxy Service
* **Platforms**: GitHub, Codeforces, LeetCode
* **Functionality**:
  Caches user profile lookups for 30 minutes in KV, shielding Android clients from IP-based rate limiting (especially GitHub 60 req/hr limits).

### 2.4 Verification Strategy
1. Build and validate TypeScript compilation for `workers/codecalendar-gateway`.
2. Inspect Cloudflare Worker endpoints and confirm JSON format matches Android client expectations.
