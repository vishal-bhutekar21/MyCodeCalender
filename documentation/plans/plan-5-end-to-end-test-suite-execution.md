# Plan 5: End-to-End Test Suite Execution

## 1. Objective
Execute test suites across the Android application, Cloudflare edge services, and the Admin web portal to guarantee system stability and test pass rates.

---

## 2. Test Execution Roadmap

### 2.1 Android Unit Tests
* Execute `./gradlew testDebugUnitTest` across all 23 active modules.
* Verify domain use cases, mappers, repositories, and ViewModels.

### 2.2 Admin Portal Build & Lint Verification
* Execute `npm run build` in `codecalendar-admin`.
* Execute `npm run lint` in `codecalendar-admin` with Oxlint.

### 2.3 Cloudflare Worker Typecheck
* Execute `npm run build` (`tsc --noEmit`) in `workers/codecalendar-gateway`.

### 2.4 Verification Strategy
Ensure all test and lint commands return exit code 0 with zero fatal errors.
