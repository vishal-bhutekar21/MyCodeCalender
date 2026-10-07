# Phase 5 Execution Plan: Architecture & Build Optimization (P2/P3)

## 1. Executive Summary
Phase 5 optimizes build configurations, cleans up unused module stubs, clarifies architecture documentation, and refines repository abstractions:
- Removes empty stub modules from `settings.gradle.kts` (`ARCH-02`).
- De-duplicates repository patterns and prevents memory leaks (`ARCH-01`).
- Aligns `README.md` and project documentation with the actual implementation (`DOC-01`).
- Ensures long-term maintainability and fast Gradle build times.

---

## 2. Issues Addressed & Technical Specifications

### 2.1 ARCH-02: Prune Empty Gradle Module Stubs
* **Affected Files**:
  * [settings.gradle.kts](file:///d:/Projects2026/dsaapp/settings.gradle.kts)
* **Root Cause**:
  `settings.gradle.kts` included `:widget`, `:core:analytics`, `:data:local`, `:data:remote`, `:data:mapper`. These folders contain no source code—only empty stub build files. This increases Gradle configuration time and project complexity.
* **Implementation Plan**:
  1. Remove `:widget`, `:core:analytics`, `:data:local`, `:data:remote`, and `:data:mapper` from `settings.gradle.kts`.
  2. Verify that none of the active modules depend on these stubs.
  3. Keep the active modules clean:
     - `:app`
     - `:core:common`, `:core:designsystem`, `:core:model`, `:core:network`, `:core:database`, `:core:datastore`, `:core:notifications`, `:core:calendar`, `:core:navigation`
     - `:data:repository`
     - `:domain:model`, `:domain:repository`, `:domain:usecase`
     - `:feature:onboarding`, `:feature:home`, `:feature:contests`, `:feature:contestdetail`, `:feature:platforms`, `:feature:platformdetail`, `:feature:resources`, `:feature:settings`
     - `:sync`

---

### 2.2 ARCH-01: Clean Up Repository Patterns & Context References
* **Affected Files**:
  * [data/repository/src/main/java/com/mycodecalendar/data/repository/FakeRepository.kt](file:///d:/Projects2026/dsaapp/data/repository/src/main/java/com/mycodecalendar/data/repository/FakeRepository.kt)
  * [MainActivity.kt](file:///d:/Projects2026/dsaapp/app/src/main/java/com/vishal/mycodecalendar/MainActivity.kt)
* **Root Cause**:
  `FakeRepository.kt` holds state flows and performs heavy operations, occasionally holding Android context.
* **Implementation Plan**:
  1. Ensure all context passed to repositories uses `context.applicationContext` to prevent Activity retention leaks.
  2. Ensure streak and account state updates flow through clear coroutine scopes.

---

### 2.3 DOC-01: Synchronize System Documentation with Implementation Reality
* **Affected Files**:
  * [README.md](file:///d:/Projects2026/dsaapp/README.md)
  * [documentation/issues/master-issue-matrix.md](file:///d:/Projects2026/dsaapp/documentation/issues/master-issue-matrix.md)
* **Implementation Plan**:
  1. Update `README.md` to document the Cloudflare Edge Worker gateway, the updated Gradle build structure, and the production security configurations.
  2. Update `master-issue-matrix.md` with resolved status and implementation references for all addressed issues.

---

## 3. Verification Strategy
1. Run `./gradlew projects` and verify Gradle project evaluation is clean and faster.
2. Verify all active modules compile with zero missing dependency errors.
3. Verify documentation is fully aligned with codebase reality.
