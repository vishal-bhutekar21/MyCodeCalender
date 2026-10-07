# Scalability & Growth Analysis

## 1. Scale Tier Breakdown: 10 to 100,000 Users

This section models system behavior as active daily users (DAU) scale from 10 to 100,000 without architectural modifications.

```text
TRAFFIC SCALE     PRIMARY BOTTLENECK                     SEVERITY    SYSTEM OUTCOME
─────────────     ──────────────────                     ────────    ──────────────
10 Users          None                                   Low         All components function normally.
100 Users         GitHub 60 req/hr rate limit            Medium      Users on campus/NAT Wi-Fi experience broken GitHub heatmaps.
1,000 Users       Upstream 429 throttling & scrapers    High        Codeforces & CodeChef block client user-agent; UI errors.
10,000 Users      Firestore collection reads             High        Admin portal dashboard freezes; Firestore bill escalates.
100,000 Users     Unmitigated upstream traffic           Critical    Widespread failure; upstream IP blacklists; cost spikes.
```

---

### Tier 1: 10 Users
* **Mobile Client**: Seamless performance. Room database caches effectively mask network latencies after initial boot.
* **Cloud Firestore**: Generates ~100 to 200 document reads/day. Operates safely within the Firebase Free Spark tier (50,000 reads/day).
* **Upstream APIs**: Upstream providers process fewer than 120 calls/hour across disparate IPs.

### Tier 2: 100 Users
* **Primary Bottleneck — Shared Network Rate Limits**:
  * Competitive programming users heavily cluster in educational institutions (engineering college campuses, hostel LANs).
  * 20 users on a single campus Wi-Fi network sharing one public IPv4 NAT gateway make up to 480 GitHub API calls per hour during peak hours.
  * Because GitHub permits only 60 calls/hour per unauthenticated IP, **all campus users receive HTTP 403 Rate Limit Exceeded errors** within 10 minutes of app usage.

### Tier 3: 1,000 Users
* **Primary Bottleneck — Upstream Anti-Scraping Defenses**:
  * 1,000 users with the app open in foreground generate **12,000 requests/hour** targeting Codeforces and LeetCode, and 12,000 HTML page fetches targeting CodeChef and GeeksforGeeks.
  * CodeChef and GeeksforGeeks deploy Cloudflare WAF and Bot Management. The repeated incoming requests with the custom user agent `Mozilla/5.0 (Android; MyCodeCalendar)` will trigger automated Turnstile challenges or 403 Forbidden responses, breaking user profile stats.

### Tier 4: 10,000 Users
* **Primary Bottleneck — Firestore Cost & Admin Portal Memory**:
  * In [firestoreService.ts](file:///d:/Projects2026/dsaapp/codecalendar-admin/src/services/firestoreService.ts#L239), `fetchDashboardMetrics()` downloads all 10,000 user documents on every admin page load.
  * In [UsersDirectory.tsx](file:///d:/Projects2026/dsaapp/codecalendar-admin/src/pages/UsersDirectory.tsx#L21), `subscribeToUsers()` loads all 10,000 user profiles into browser RAM via WebSockets.
  * Every time a user increments their streak on app launch, an update event is pushed to the admin browser, resulting in continuous re-renders and potential browser tab crashes.

### Tier 5: 100,000+ Users
* **System Breakdown**:
  * Direct client fetching becomes completely unsustainable. Upstream contest platforms will issue cease-and-desist notices or hard-block the application package name.
  * Firestore daily operations exceed hundreds of thousands of reads and writes per day, creating significant operational costs if data structure and caching remain unoptimized.

---

## 2. Cost Analysis & Risk Model

| Infrastructure Component | Current Architecture Cost Risk (at 10k DAU) | Optimized Edge Architecture Cost (at 10k DAU) |
|---|---|---|
| **Cloudflare Workers** | $0.00 (Unused / Ghost configuration) | $5.00 / month (Cloudflare Workers Paid for unlimited requests & Cron triggers) |
| **Cloud Firestore** | **High ($30 – $120 / month)**: Driven by unbounded reads in Admin CMS and frequent mobile client profile writes. | **Low ($0 – $5 / month)**: Driven by server-side aggregations (`getCountFromServer`) and paginated views. |
| **Mobile Cellular Bandwidth** | **High**: ~450 MB / month per user absorbed by user data plans. | **Minimal**: ~15 MB / month per user (96% bandwidth reduction). |
| **Upstream API Billing** | Risk of forced migration to paid third-party contest APIs (e.g. CLIST API costing $10 – $50/mo). | **$0.00**: Cloudflare Cron worker caches free public endpoints without exceeding quotas. |
