# API & Upstream Network Performance

## 1. Upstream Overfetching & Payload Analysis

Every network request issued by [RemoteDataSource.kt](file:///d:/Projects2026/dsaapp/core/network/src/main/java/com/mycodecalendar/core/network/RemoteDataSource.kt) was analyzed for payload size, overfetching ratio, and computational overhead.

| Target Endpoint | Network Payload | Data Actually Needed by App | Overfetching Ratio | Computational / Network Penalty |
|---|---|---|---|---|
| **Codeforces Contests** (`/api/contest.list?gym=false`) | ~380 KB (1,200+ contest objects) | ~5 upcoming contests (< 2 KB) | **> 98% Overfetched** | Parses 1,200 objects into Kotlin DTOs on the main/IO thread; filters down to top 10. |
| **LeetCode Contests** (`/graphql` - `allContests`) | ~120 KB (400+ contest objects) | ~4 upcoming contests (< 1.5 KB) | **> 96% Overfetched** | Overfetches historical contests since 2017; client filters in memory. |
| **AtCoder Contests** (`kenkoooo.com/.../contests.json`) | ~190 KB (800+ contest objects) | ~3 upcoming contests (< 1 KB) | **> 98% Overfetched** | Downloads full historical archive of AtCoder contests. |
| **Codeforces Submissions** (`/api/user.status?count=1000`) | ~1,200 KB – 1,800 KB (1,000 full submission objects) | Total solved count integer (< 10 bytes) | **> 99.9% Overfetched** | 1.5 MB JSON downloaded and deserialized solely to count distinct accepted problem IDs. |
| **GitHub Repositories** (`/users/{u}/repos?per_page=100`) | ~280 KB – 600 KB | 5 top repos, total stars, top languages (< 4 KB) | **> 95% Overfetched** | Downloads deep git metadata (clone URLs, hashes, topics, permissions). |
| **CodeChef User Profile** (`/users/{username}`) | ~450 KB – 850 KB (Raw HTML document) | Rating, stars, global rank (< 100 bytes) | **> 99.8% Overfetched** | Downloads complete HTML DOM including CSS, scripts, and navigation. |
| **GeeksforGeeks Profile** (`/user/{username}/`) | ~500 KB – 950 KB (Raw HTML document) | Score, solved count, institute rank (< 100 bytes) | **> 99.8% Overfetched** | Downloads complete Next.js SSR HTML page. |

---

## 2. Total Bandwidth Cost per User Session

When calculating the total data consumed by a typical user session with 3 connected platform handles:
* **Contest Radar Sync**: ~690 KB
* **Platform Handle Sync (CF + LC + GH)**: ~1,850 KB
* **Total Transferred per Full Sync**: **~2,540 KB (2.54 MB)**

### Cumulative Cellular Usage at 5-Minute Polling Interval
* In 1 hour of app foreground usage: **~30.4 MB**
* In 1 month (30 minutes daily usage): **~450 MB** of cellular data consumed per user.

This high data consumption directly impacts users on limited mobile data plans in developing markets (such as student competitive programmers in India).

---

## 3. Rate Limit Threat Matrix

| External Service | Official Rate Limit Policy | Impact on CodeCalendar Users | Current Failure Mitigation |
|---|---|---|---|
| **GitHub REST API** | 60 requests / hour per public IP address. | Users on shared NAT / campus Wi-Fi exhaust this limit within minutes. | None. Fails silently with `Result.failure` and displays cached data or empty screen. |
| **Codeforces API** | Max 5 requests / second per IP. Rapid bursts result in temporary 10-minute bans. | Multiple simultaneous calls from a single mobile device can trigger Cloudflare 429 throttling. | None. Returns HTTP 429 or HTML challenge page; Ktor JSON parsing throws deserialization error. |
| **LeetCode GraphQL** | Undocumented; throttles unauthenticated IP bursts with HTTP 429. | Heavy polling from many users on the same subnet risks IP blocks. | None. |
| **CodeChef Website** | WAF & Cloudflare Bot Management rules active on web server. | Sending `Mozilla/5.0 (Android; MyCodeCalendar)` user agent to HTML routes risks CAPTCHA / 403 challenge. | Fallback to third-party Vercel scraper (`codechef-api.vercel.app`), which frequently returns 402/429. |

---

## 4. Remediation: Edge Aggregation Model

All upstream overfetching can be eliminated by moving contest aggregation to a Cloudflare Worker:
1. **Edge Normalization**: The Cloudflare Worker queries upstream APIs on a 10-minute cron, strips historical contests, and creates a consolidated JSON response containing only active/upcoming contests across all platforms (< 25 KB total).
2. **Bandwidth Savings**: Mobile clients download **< 25 KB** instead of **~690 KB** (a **96% reduction in bandwidth**).
3. **Upstream Rate Limit Immunity**: Upstream platforms see only Cloudflare data center IPs making 6 queries/hour, completely shielding mobile users from rate limits.
