<div align="center">

# MyCodeCalendar

### A modern competitive programming companion for Android

*Track contests · Monitor ratings · Never miss a deadline*

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26-orange?style=flat-square)](https://developer.android.com/about/versions/oreo)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)

</div>

---

## Overview

**MyCodeCalendar** is a fully offline-capable Android application built for competitive programmers. It aggregates contests from the top 5 competitive programming platforms, tracks your personal ratings and statistics, and lets you sync upcoming contests directly to your Android calendar — all from one clean, minimal interface.

Built entirely with **Jetpack Compose**, **Material Design 3**, and a clean **multi-module architecture**.

---

## Features

### Contest Tracking
- Live countdown timers for upcoming contests
- Real-time contest status (Live · Upcoming · Ended)
- Filter by platform, status, or search by name
- Time-until-start shown on every contest row

### Platform Stats & Ratings
- Connect your handles for **Codeforces, LeetCode, CodeChef, AtCoder, GeeksforGeeks, and GitHub**
- View current rating, highest rating, global rank, and current streak
- Rating progression line chart with gradient fill
- Problem difficulty breakdown (Easy / Medium / Hard) with stacked progress bar

### Developer Activity (GitHub)
- Total contributions for the year
- Current contribution streak
- Total stars across repos
- Public repository count
- Top programming languages

### Smart Refresh
- Pull-to-refresh with animated spinning icon
- Last-updated timestamp in the dashboard header
- Progressive loading indicator

### Study Resources
- Curated hand-picked tutorials and guides
- Category-based filtering with accent-colored chips
- Direct link to resources via system browser

### Calendar & Reminders
- One-tap contest add to Android calendar
- Set 15-minute pre-contest notifications
- No duplicate events

### Onboarding
- Three-step animated onboarding with smooth slide transitions
- Skip anytime

### Settings
- Connect / disconnect platform accounts
- Contest notification toggle
- Calendar sync toggle
- App theme: Light · Dark · System Default

---

## Screenshots

> *Coming soon — install the debug APK and experience it live*

---

## Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Kotlin 2.0 |
| **UI Framework** | Jetpack Compose + Material Design 3 |
| **Architecture** | Multi-Module Clean Architecture (MVVM) |
| **Navigation** | Jetpack Navigation Compose |
| **Local Database** | Room (with KSP) |
| **Async** | Kotlin Coroutines + StateFlow |
| **DI** | Manual DI (no Hilt, lightweight) |
| **Build System** | Gradle with Kotlin DSL |
| **Min SDK** | 26 (Android 8.0 Oreo) |
| **Target SDK** | 34 (Android 14) |
| **Compile SDK** | 34 |

---

## Architecture

MyCodeCalendar follows a strict **multi-module Clean Architecture** pattern:

```
MyCodeCalendar/
├── app/                        # Application entry point, MainActivity, NavHost
│
├── core/
│   ├── designsystem/           # Color palette, typography, shared components
│   │   └── components/         # FloatingBottomNavigation, PlatformBadge, StatusChip, ...
│   ├── database/               # Room database, DAOs, entities
│   ├── datastore/              # Preferences DataStore
│   ├── network/                # Retrofit API definitions (future)
│   ├── common/                 # Shared utilities
│   ├── calendar/               # Android Calendar integration
│   └── notifications/          # Notification scheduling
│
├── domain/
│   ├── model/                  # Pure Kotlin data models (Contest, Platform, PlatformStats, ...)
│   ├── repository/             # Repository interfaces
│   └── usecase/                # Business logic use cases
│
├── data/
│   ├── repository/             # FakeRepository (offline demo data)
│   ├── local/                  # Room data source implementations
│   ├── remote/                 # Network data source implementations
│   └── mapper/                 # Entity ↔ Domain model mappers
│
├── feature/
│   ├── home/                   # Dashboard, hero countdown, platform stats
│   ├── contests/               # Contest list with filtering and search
│   ├── contestdetail/          # Single contest detail + calendar / reminder actions
│   ├── platformdetail/         # Rating chart, stat cards, problem breakdown
│   ├── platforms/              # Add / connect platform account
│   ├── resources/              # Curated study resource list
│   ├── settings/               # Account management, preferences, appearance
│   └── onboarding/             # First-launch walkthrough
│
├── sync/                       # Background sync worker (future live data)
└── widget/                     # Home screen widget (future)
```

---

## Design System

The app uses a custom **Material Design 3** design system defined in `core:designsystem`:

### Color Palette

| Token | Dark Mode | Light Mode |
|---|---|---|
| Background | `#0B0F19` (Obsidian) | `#F8FAFC` (Porcelain) |
| Surface | `#131B2E` (Deep Slate) | `#FFFFFF` |
| Primary | `#6366F1` (Indigo) | `#6366F1` |
| Secondary | `#10B981` (Emerald) | `#10B981` |
| Tertiary | `#06B6D4` (Cyan) | `#06B6D4` |

### Platform Brand Colors

| Platform | Color |
|---|---|
| Codeforces | `#3B82F6` Blue |
| LeetCode | `#F59E0B` Amber |
| CodeChef | `#8B5CF6` Violet |
| AtCoder | `#64748B` Slate |
| GeeksforGeeks | `#2F8D46` Green |
| GitHub | `#10B981` Emerald |

### Navigation

A custom **FloatingBottomNavigation** pill replaces the standard `NavigationBar`. The selected tab expands with an animated label, and all tabs use smooth spring animations.

---

## Supported Platforms

| Platform | Contests | Rating | Streak | Rank |
|---|---|---|---|---|
| Codeforces | ✅ | ✅ | ✅ | ✅ |
| LeetCode | ✅ | ✅ | ✅ | ✅ |
| CodeChef | ✅ | ✅ | — | ✅ |
| AtCoder | ✅ | ✅ | — | ✅ |
| GeeksforGeeks | ✅ | ✅ | — | ✅ |
| GitHub | — | — | ✅ | — |

---

## Getting Started

### Prerequisites

- **Android Studio Hedgehog** or newer
- **JDK 21** (required for KSP with Kotlin 2.0)
- Android SDK Platform 34
- Git

### Clone & Build

```bash
git clone https://github.com/vishal-bhutekar21/MyCodeCalender.git
cd MyCodeCalender
```

Open in Android Studio. Let Gradle sync complete, then:

```bash
# Debug build
./gradlew assembleDebug

# Install directly to connected device
./gradlew installDebug
```

The output APK is at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### JDK Configuration

This project requires **JDK 21** for KSP symbol processing. In Android Studio:

> **File → Project Structure → SDK Location → Gradle JDK → JDK 21**

Or set in `gradle.properties`:
```properties
org.gradle.java.home=C:/Program Files/Java/jdk-21
```

---

## Project Status

| Module | Status |
|---|---|
| Core design system | ✅ Complete |
| Onboarding flow | ✅ Complete |
| Home dashboard | ✅ Complete |
| Contest listing & detail | ✅ Complete |
| Platform detail & rating chart | ✅ Complete |
| Connect platform (Add handle) | ✅ Complete |
| Resources screen | ✅ Complete |
| Settings screen | ✅ Complete |
| Floating bottom navigation | ✅ Complete |
| Room database schema | ✅ Complete |
| Live contest API integration | 🔄 Planned |
| Background sync worker | 🔄 Planned |
| Home screen widget | 🔄 Planned |
| Rating history chart (live data) | 🔄 Planned |

---

## Roadmap

- [ ] **Live API integration** — Codeforces, LeetCode, and Kontests public APIs
- [ ] **Background sync** — periodic WorkManager job to refresh contest data
- [ ] **Home screen widget** — next contest countdown on the device home screen
- [ ] **Deep links** — open contest detail from notification tap
- [ ] **Export to iCal** — share contest schedule
- [ ] **Dark / Light theme switching** — respect in-app setting immediately

---

## Contributing

Contributions are welcome. Please open an issue first to discuss what you'd like to change.

1. Fork the repo
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -m 'Add your feature'`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Open a Pull Request

---

## License

```
MIT License

Copyright (c) 2026 Vishal Bhutekar

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```

---

## Author

**Vishal Bhutekar**

- GitHub: [@vishal-bhutekar21](https://github.com/vishal-bhutekar21)
- Project: [MyCodeCalendar](https://github.com/vishal-bhutekar21/MyCodeCalender)

---

<div align="center">

*Built with Kotlin · Jetpack Compose · Material Design 3*

</div>
