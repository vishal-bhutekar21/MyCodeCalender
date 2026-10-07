# Master Issue Matrix

This table aggregates all identified findings across the application, categorized by severity, priority, component, and complexity, along with their remediation status.

| ID | Severity | Priority | Category | Component | Issue Name | Production Impact | Complexity | Status |
|---|---|---|---|---|---|---|---|---|
| **DATA-01** | Critical | P0 | Database / Data Integrity | `MainActivity.kt` / `CloudAdminSyncService.kt` | Account Login Clears Local Data & Overwrites Cloud Profile | Permanent loss of connected handles and user profile data upon login. | Medium | `FIXED` |
| **SEC-01** | Critical | P0 | Security | `app/build.gradle.kts` | Plaintext Keystore Passwords Committed in Version Control | Potential unauthorized signing of application binaries. | Low | `FIXED` |
| **SEC-02** | Critical | P0 | Security / Auth | `codecalendar-admin/firestore.rules` | Super-Admin Privilege Escalation via Unverified Email | Attacker can gain full read/write admin access to all users and CMS. | Low | `FIXED` |
| **BUILD-01** | Critical | P0 | Deployment / Build | `gradle.properties` | Machine-Specific Absolute JDK Path Breaks CI & Local Builds | Builds fail immediately on any non-matching system or CI runner. | Low | `FIXED` |
| **BUILD-02** | Critical | P0 | Deployment / CI | `.github/workflows/pr.yml` | CI Workflow Fails on Non-Existent `:backend:build` Module | All GitHub Actions pull request checks fail automatically. | Low | `FIXED` |
| **REL-01** | Critical | P0 | Reliability | `core:notifications` | Contest Alarms Silently Wiped on Device Reboot | Users miss scheduled contests due to lost `AlarmManager` timers. | Medium | `FIXED` |
| **REL-02** | Critical | P0 | Reliability | `core:calendar` / `AndroidManifest.xml` | Calendar Export Fails Due to Missing Manifest Permissions | Runtime `SecurityException` causes calendar sync to permanently fail. | Low | `FIXED` |
| **CF-01** | High | P1 | Cloudflare / Architecture | `workers/codecalendar-gateway` | Ghost Backend Domain `api.mycodecalendar.com` Does Not Resolve | Feature regression; mobile client completely bypasses edge layer. | High | `FIXED` |
| **PERF-01** | High | P1 | Performance / Network | `workers/codecalendar-gateway` | Direct Multi-Platform Network Waterfall Causes 15s Latency | High UI refresh latency, battery drain, high mobile bandwidth usage. | High | `FIXED` |
| **PERF-02** | High | P1 | Performance | `workers/codecalendar-gateway` | Unauthenticated GitHub Requests Exhaust 60 req/hr Rate Limit | Broken GitHub heatmaps for users on campus/shared Wi-Fi. | Medium | `FIXED` |
| **PERF-03** | High | P1 | Reliability / Performance | `workers/codecalendar-gateway` | Fragile Client-Side HTML Web Scraping (CodeChef / GFG) | Profile data breaks whenever external websites update HTML/WAF. | Medium | `FIXED` |
| **DATA-02** | High | P1 | Security / Database | `codecalendar-admin/firestore.rules` | Missing Rule for `/feedback` Collection Rejects User Reports | User bug reports and feature requests are permanently rejected. | Low | `FIXED` |
| **SEC-03** | High | P1 | Security | Cloudflare Worker (`razorpay-backend-worker`) | Plaintext Razorpay Secret Key Fallback in Worker Bundle | Unauthorized verification of payment signatures. | Low | `PLANNED` |
| **PERF-04** | High | P1 | Performance / Database | `codecalendar-admin` (`UsersDirectory.tsx`) | Unbounded Snapshot Listener on `/users` in Admin CMS | Browser memory crash and high Firestore read charges at scale. | Medium | `FIXED` |
| **PERF-05** | High | P1 | Performance / Database | `codecalendar-admin` (`firestoreService.ts`) | Metric Aggregations Download Full User Document Bodies | Massive Firestore read costs on every dashboard page view. | Low | `FIXED` |
| **REL-03** | High | P1 | Reliability / Auth | `feature:onboarding` (`AuthScreen.kt`) | Google Sign-In Issues Anonymous Firebase Sessions | Accounts cannot be restored across devices or reinstalls. | Medium | `FIXED` |
| **REL-04** | High | P1 | Reliability | `sync` / `MainActivity.kt` | Background `ContestSyncWorker` is an Inactive Placeholder | Zero background contest synchronization. | Medium | `FIXED` |
| **OBS-01** | High | P1 | Observability | `core:common` (`AppLogger.kt`) | Total Absence of Crash Reporting & Remote Diagnostics | Production crashes and failures cannot be diagnosed or alerted. | Medium | `FIXED` |
| **DATA-03** | Medium | P2 | Database | `core:database` | Missing ReminderDao and Safe Database Migration | App updates risk silently dropping offline user reminder data. | Medium | `FIXED` |
| **DATA-04** | Medium | P2 | Privacy / Database | `codecalendar-admin` (`firestoreService.ts`) | Incomplete GDPR Deletion Leaves Orphaned Subcollections & Auth | Fails to fully purge user data as required by Google Play policies. | Medium | `PLANNED` |
| **SEC-04** | Medium | P2 | Configuration | `codecalendar-admin` (`firebase.ts`) | Hardcoded Fallback Firebase Credentials to Wrong Project | Data saved to incorrect project (`shetkari-mitra-7721`) if env missing. | Low | `FIXED` |
| **REL-05** | Medium | P2 | Notifications | `core:notifications` (`NotificationHelper.kt`) | Cloud Push Channel Inoperable (No FCM Service Implemented) | Push notification channel cannot receive remote broadcasts. | High | `PLANNED` |
| **BUILD-03**| Medium | P2 | Deployment | `app/build.gradle.kts` | Release Build References Missing `release.jks` & Suppresses Lint | `assembleRelease` fails; release lint verification is disabled. | Low | `FIXED` |
| **ARCH-01** | Medium | P2 | Architecture | `data:repository` (`FakeRepository.kt`) | Monolithic Repository Leaks Activity Context | Architectural coupling; potential memory leaks on config changes. | High | `FIXED` |
| **PERF-06** | Medium | P2 | Database / Performance | `core:database` (`GitHubStatsEntity.kt`) | Room Row Bloat from Storing 150 KB Serialized JSON Blobs | SQLite CursorWindow thrashing and garbage collection pauses. | Medium | `PLANNED` |
| **ARCH-02** | Low | P3 | Architecture | Root Gradle Build (`settings.gradle.kts`) | 6 Empty and Disconnected Gradle Module Stubs | Build overhead and confusion for developers. | Low | `FIXED` |
| **DOC-01** | Low | P3 | Documentation | `README.md` | Documentation Claims Clean Architecture & Features Not Present | Misleads maintainers and contributors regarding real system status. | Low | `FIXED` |
