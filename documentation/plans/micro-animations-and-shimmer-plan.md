# Micro-Animations, Shimmer Effects, and 120Hz Speed Optimization Plan

## Objective
Elevate the mobile application to feel ultra-fast, responsive, tactile, and visually stunning by implementing comprehensive diagonal sweeping shimmer effects in all unrendered loading states, fluid physical micro-spring press animations, live radar pulses, and optimized rendering pipelines.

---

## 1. Identified Gaps & Opportunities

### 1.1 Missing Shimmer Skeletons
1. **`PlatformDetailScreen`**:
   * `PlatformDetailSkeleton` was defined in `core/designsystem/components/ShimmerEffect.kt` but never rendered in `PlatformDetailScreen.kt`. When `stats == null` (network emission delay or handles sync), it flashed a jarring "Account Not Connected" empty state instead of a shimmer placeholder.
   * `GitHubDetailContent` had no loading skeleton for repository cards and commit heatmap when `gitHubStats == null` while querying GitHub API.
2. **`ContestsScreen` (Hackathons Tab)**:
   * When switching to or loading the "Hackathons" tab, no `HackathonCardSkeleton` existed.
   * Calendar Grid View displayed `EmptyState` rather than shimmering cards when contests list is empty during initial cold launch.
3. **`SettingsScreen`**:
   * `SettingsScreenSkeleton` existed in design tokens but was never integrated into `SettingsScreen.kt`.

### 1.2 Micro-Animations & Tactile Feedback
1. **Interactive Tap/Press Spring Scale**:
   * `GlassCard` and `GlassChip` had default ripples without physical mass or micro-compression.
   * Implementing a physics-based spring scale modifier (`scaleDown = 0.965f`) on `MutableInteractionSource` brings instant responsiveness and premium feedback.
2. **Reusable Shimmer Modifier**:
   * Introduce `Modifier.shimmerEffect(cornerRadius: Dp = 10.dp)` allowing arbitrary shapes, custom images, and text placeholders to shimmer with zero boilerplate.
3. **Smooth Staggered Item Introductions**:
   * Add animated transitions (`fadeIn` + subtle `slideInVertically`) so content resolves gracefully without abrupt layout snapping.

---

## 2. Step-by-Step Implementation Architecture

### Phase 1: Core Design System Micro-Animations & Skeletons (`core/designsystem`)
* **File**: `core/designsystem/src/main/java/com/mycodecalendar/core/designsystem/components/ShimmerEffect.kt`
  * Add `Modifier.shimmerEffect(cornerRadius: Dp, accentGlow: Color?)` extension.
  * Add `Modifier.bounceClick(scaleDown: Float, onClick: (() -> Unit)?)` spring physics modifier.
  * Add `HackathonCardSkeleton()` glassmorphic shimmering card placeholder.
  * Add `GitHubDetailSkeleton()` full profile, heatmap, and repo list shimmer.
* **File**: `core/designsystem/src/main/java/com/mycodecalendar/core/designsystem/components/GlassCard.kt`
  * Integrate spring bounce animation when `onClick != null` using `interactionSource.collectIsPressedAsState()`.
* **File**: `core/designsystem/src/main/java/com/mycodecalendar/core/designsystem/components/GlassChip.kt` (or within `GlassCard.kt`)
  * Add spring micro-scale animation when pressed.

### Phase 2: Feature-Level Shimmer & Animation Integration
* **`feature/platformdetail/PlatformDetailScreen.kt`**:
  * Check `if (stats == null)` -> render `PlatformDetailSkeleton()`.
  * In `GitHubDetailContent`, if `gitHubStats == null` -> render `GitHubDetailSkeleton()`.
* **`feature/contests/ContestsScreen.kt`**:
  * In Hackathons tab, display `repeat(3) { HackathonCardSkeleton() }` when loading.
  * In Calendar view, display `ContestCardSkeleton()` when contests are empty.
* **`feature/settings/SettingsScreen.kt`**:
  * Add `isLoading: Boolean = false` parameter and render `SettingsScreenSkeleton()` when true.
* **`app/MainActivity.kt`**:
  * Pass real loading states to `PlatformDetailScreen` and `SettingsScreen`.

### Phase 3: Validation & Verification
* Run `.\gradlew.bat compileDebugKotlin` across all 23 modules.
* Run `.\gradlew.bat testDebugUnitTest --continue` to guarantee zero regressions.
