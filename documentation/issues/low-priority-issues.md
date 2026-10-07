# Low-Priority Issues (P3 — Improvements & Polish)

This document contains specifications for **P3 (Improvement)** issues representing architectural debt, obsolete code, or documentation discrepancies.

---

## ARCH-02: Empty and Disconnected Gradle Module Stubs

Severity:
Low

Category:
Architecture

Location:
- `widget/build.gradle.kts`
- `core/analytics/build.gradle.kts`
- `data/local/build.gradle.kts`
- `data/remote/build.gradle.kts`
- `data/mapper/build.gradle.kts`
- `domain/repository/build.gradle.kts`

Evidence:
6 Gradle modules declared in [settings.gradle.kts](file:///d:/Projects2026/dsaapp/settings.gradle.kts) contain only `build.gradle.kts` with zero Kotlin source files or logic.

Current Behavior:
Gradle configures and evaluates 6 empty projects on every build run, slowing down build configuration time and confusing new developers.

Why This Is A Problem:
Creates unnecessary build maintenance overhead and misrepresents project architecture.

Recommended Solution:
Either implement the intended module logic or remove unused subprojects from `settings.gradle.kts` and delete their directories.

Implementation Complexity:
Low

Priority:
P3

Verification:
Verify `./gradlew tasks` executes cleanly without empty project evaluation overhead.

Confidence:
Confirmed

---

## DOC-01: Project Documentation Contradicts Actual Codebase Implementation

Severity:
Low

Category:
Documentation

Location:
[README.md](file:///d:/Projects2026/dsaapp/README.md)

Evidence:
- README claims a Ktor microservice backend (`backend/`), but the module is not in `settings.gradle.kts` and has no source code.
- README claims Clean Architecture with separate local/remote data sources, but all logic resides in a single `FakeRepository.kt`.
- README claims widget support, but `:widget` is an empty stub.
- README lists defunct `kontests.net` service which the code explicitly bypasses.

Current Behavior:
Documentation paints an idealized architectural picture that does not match the actual codebase reality.

Why This Is A Problem:
Causes friction, confusion, and mistaken assumptions for incoming engineers, reviewers, and maintainers.

Recommended Solution:
Update `README.md` with an accurate "Documentation vs. Reality" section, link to the `/documentation` audit directory, and align architectural claims with the actual system state.

Implementation Complexity:
Low

Priority:
P3

Verification:
Review updated `README.md` and verify all claimed components, modules, and workflows exist and function as documented.

Confidence:
Confirmed
