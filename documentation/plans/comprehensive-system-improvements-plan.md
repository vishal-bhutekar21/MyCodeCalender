# Comprehensive System Audit & Improvements Plan

## Executive Summary
This plan delivers end-to-end performance optimizations, resilience hardening, compiler cleanups, bundle size reduction, and unit test suite expansion across the Android client, Cloudflare Edge Gateway, and Web Admin CMS.

---

## Targeted Improvement Areas

### 1. Cloudflare Gateway Primary Edge Routing in Android (`FakeRepository.kt`)
- **Problem**: `FakeRepository.fetchLiveContests()` calls 4 separate external platform APIs (LeetCode, Codeforces, AtCoder, CodeChef) sequentially from the Android device. This causes high battery consumption, latency (3–6 seconds), and sensitivity to mobile network jitter.
- **Solution**:
  - Add `GatewayContestDto.toDomain(): Contest?` mapper function.
  - Wire `remoteDataSource.fetchGatewayContests()` as the **Primary Fast Path** in `FakeRepository.fetchLiveContests()`.
  - If the Edge Gateway returns valid contests, immediately populate `liveContests`, persist to Room DB, and update `contestsFlow` (latency: ~100ms).
  - If the Edge Gateway fails (offline, edge failure, timeout), seamlessly fall back to the existing 4 client-side direct platform API calls.

### 2. Missing `ContestDetailSkeleton` & Detail Deep-Link Resilience
- **Problem**: Navigating to `contest_detail/{contestId}` during a cold start or before contests finish loading produces a blank screen if the contest is null.
- **Solution**:
  - Create `ContestDetailSkeleton` in `ShimmerEffect.kt` with realistic glassmorphism layout (header badge, title, timer card, details cards, action button shimmer).
  - Update `MainActivity.kt` to render `ContestDetailSkeleton` if `contest == null && isLoading`.

### 3. Clean Compiler Diagnostics (Deprecation Suppressions)
- **Problem**: Google Sign-In classes (`GoogleSignInOptions`, `GoogleSignInClient`, `GoogleSignIn`) in `MainActivity.kt` and `AuthScreen.kt` trigger compiler deprecation warnings.
- **Solution**:
  - Add `@Suppress("DEPRECATION")` to the specific legacy Google Sign-In helper functions and activity launchers.

### 4. Admin Web Portal Bundle Optimization (`codecalendar-admin/vite.config.ts`)
- **Problem**: The single Vite production bundle is 1,138 kB (> 500 kB limit warning), increasing initial load time on the web dashboard.
- **Solution**:
  - Configure Rollup `manualChunks` in `vite.config.ts` to cleanly separate:
    - `vendor-react`: `react`, `react-dom`
    - `vendor-firebase`: `firebase/app`, `firebase/auth`, `firebase/firestore`
    - `vendor-lucide`: `lucide-react`
  - Eliminates the chunk size warning and speeds up browser download via parallel HTTP/2 multiplexing.

### 5. Cloudflare Edge Gateway CDN Header Optimization (`workers/codecalendar-gateway`)
- **Problem**: Worker responses should explicitly instruct Cloudflare edge caches and HTTP clients with optimal `Cache-Control` directives (`s-maxage=600`, `stale-while-revalidate=1200`).
- **Solution**:
  - Add `Cache-Control` header to `/api/v1/contests` response in `workers/codecalendar-gateway/src/index.ts`.

### 6. Test Suite Expansion
- **Problem**: Unit tests were absent in `:core:network` and `:data:repository`.
- **Solution**:
  - Add unit tests for `GatewayContestDto.toDomain()` conversion, ISO parsing, status calculation, and duration handling.
  - Add unit tests for `GatewayContestsResponseDto` JSON deserialization with `kotlinx.serialization`.

---

## Verification Criteria
1. `npm run build` in `codecalendar-admin` finishes cleanly without large chunk warnings.
2. `npm run build` in `workers/codecalendar-gateway` succeeds with 0 TypeScript errors.
3. `./gradlew compileDebugKotlin` passes with 0 errors.
4. `./gradlew testDebugUnitTest` runs all unit tests successfully across all modules.
