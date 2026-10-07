# Production Readiness Audit & Scorecard

## 1. Production Readiness Verdict

> ### 🛑 FINAL VERDICT: NOT PRODUCTION READY (`NO`)
>
> The application cannot safely be deployed to production in its current state.
> It contains **P0 Production Blockers** including:
> 1. **Immediate Data Loss on Login**: Signing in or creating an account wipes local user data and immediately overwrites the user's cloud document with an empty platform list.
> 2. **Critical Security Vulnerabilities**: Production keystore passwords committed in plaintext to Gradle build files; unverified email addresses can gain super-admin privileges in Firestore security rules.
> 3. **Broken CI/CD Pipeline**: GitHub Actions pull-request workflow fails because it targets a non-existent Gradle module (`:backend:build`), and local builds fail on non-matching environments due to a hardcoded machine-specific JDK path in `gradle.properties`.
> 4. **Runtime Security Exceptions**: Exporting contests to the Android calendar crashes or fails with a runtime `SecurityException` due to undeclared calendar permissions.
> 5. **Silent Alarm Erasure**: Scheduled contest reminders are permanently wiped on device reboot because no `RECEIVE_BOOT_COMPLETED` receiver exists.
> 6. **Missing Backend Infrastructure**: The edge API (`api.mycodecalendar.com`) is not deployed, forcing clients to perform fragile client-side web scraping and unauthenticated API calls that quickly hit rate limits.

---

## 2. Production Readiness Scorecard

| Category | Status | Summary Evidence | Major Production Risks | Required Remediations |
|---|---|---|---|---|
| **Architecture** | **NEEDS WORK** | 16 modules declared; 6 are empty stubs (`widget`, `analytics`, `data:local`, etc.). [FakeRepository.kt](file:///d:/Projects2026/dsaapp/data/repository/src/main/java/com/mycodecalendar/data/repository/FakeRepository.kt) is a 1980-line God class. | Architectural bloat, difficult maintainability, lack of clean boundaries. | Consolidate data layer into real repositories; prune dead module stubs. |
| **Performance** | **NEEDS WORK** | Direct multi-platform waterfall requests taking 4s–15s; 2.5 MB payload per refresh; regex HTML scraping. | Severe UI refresh latency, battery drain, high mobile cellular bandwidth cost. | Deploy Cloudflare Worker edge aggregation API; cache normalized JSON. |
| **Cloudflare** | **BLOCKED** | Ghost domain `api.mycodecalendar.com` does not resolve. No Worker deployed in live account. | Feature regression; app bypasses edge layer entirely. | Deploy dedicated Worker script with KV/Cache and scheduled cron triggers. |
| **Database** | **NEEDS WORK** | Room DB has `fallbackToDestructiveMigration()` enabled and `exportSchema = false`. Storing 150 KB JSON blobs. | Schema upgrades wipe local data; Firestore aggregation reads entire user database. | Implement explicit Room migrations; use `getCountFromServer()` in Firestore. |
| **Security** | **BLOCKED** | Keystore passwords in plaintext in [app/build.gradle.kts](file:///d:/Projects2026/dsaapp/app/build.gradle.kts#L25); `firestore.rules` checks email without `email_verified == true`. | Production keystore compromise; unauthorized admin privilege escalation. | Move passwords to environment variables/secrets; enforce `email_verified` in rules. |
| **Reliability** | **BLOCKED** | Missing `BOOT_COMPLETED` receiver wipes alarms on reboot; missing calendar permissions throw `SecurityException`. | Silent feature failure; users miss contests; app crashes during calendar export. | Add `RECEIVE_BOOT_COMPLETED` and calendar permissions; handle runtime grants. |
| **Scalability** | **BLOCKED** | Direct unauthenticated GitHub calls (60 req/hr); unbounded `onSnapshot` on `/users` in Admin portal. | Outages on shared Wi-Fi networks; admin browser crashes; high Firestore billing. | Edge caching for GitHub; paginated queries with `limit()` in Admin portal. |
| **Observability** | **BLOCKED** | No crash reporting (Firebase Crashlytics or Sentry missing); no correlation IDs; no structured logs. | 2 AM production failures cannot be diagnosed or alerted. | Integrate Firebase Crashlytics; add Cloudflare Worker structured logging. |
| **CI/CD** | **BLOCKED** | [pr.yml:L37](file:///d:/Projects2026/dsaapp/.github/workflows/pr.yml#L37) runs `./gradlew :backend:build` which fails; `gradle.properties` hardcodes `C:\Program Files\Java\jdk-21`. | Pull request validation fails; automated testing is impossible across machines. | Remove `:backend:build` from CI; remove hardcoded `org.gradle.java.home`. |
| **Configuration** | **NEEDS WORK** | Hardcoded Firebase fallback credentials for another project (`shetkari-mitra-7721`) in [firebase.ts](file:///d:/Projects2026/dsaapp/codecalendar-admin/src/services/firebase.ts#L7-L13). | Data bleeding into wrong Firebase project if environment variables are missing. | Enforce mandatory environment variables; throw error on startup if missing. |
| **Documentation** | **NEEDS WORK** | Claims clean architecture, Ktor backend, and widgets that do not exist. | Misleads maintainers and contributors. | Update README to reflect real implementation and reference audit documentation. |
| **Disaster Recovery** | **BLOCKED** | No backup procedures for Firestore; no rollback strategy for Android APK releases. | Irreversible data loss if Firestore collection is corrupted. | Configure automated Firestore daily export backups to Google Cloud Storage. |
| **Testing** | **BLOCKED** | Only 2 sample unit tests in the entire repository (< 1% test coverage). | High risk of regression on any code change. | Add unit tests for repositories, ViewModels, and mappers. |
