<div align="center">

# ⚡ Code Calendar (MyCodeCalendar)

### *The Ultimate Competitive Programming Companion & Developer Hub for Android*

*Track Live Contests · Unified Rating Analytics · GitHub Heatmaps · Daily Habit Streaks · AI & Learning Hub*

<br/>

[![Android Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://android.com)
[![Kotlin Version](https://img.shields.io/badge/Kotlin-2.0.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Material Design 3](https://img.shields.io/badge/Design-Material%203%20Glassmorphism-FF7043?style=for-the-badge&logo=materialdesign&logoColor=white)](https://m3.material.io)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26%20(Oreo)-orange?style=for-the-badge)](https://developer.android.com/about/versions/oreo)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-34%20(Android%2014)-blue?style=for-the-badge)](https://developer.android.com/about/versions/14)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](LICENSE)

</div>

---

## 🌟 Overview

**Code Calendar** (`com.vishal.codecalendar`) is a modern, high-performance Android application engineered for software engineers, competitive programmers, and tech enthusiasts. It aggregates live and upcoming contests from top platforms (**LeetCode, Codeforces, CodeChef, and AtCoder**), tracks your personal rating progression curves, visualizes GitHub contributions, builds daily problem-solving habits with streak trackers, and provides an all-in-one AI & education developer hub.

Designed with **Jetpack Compose**, **Material 3 OLED Glassmorphism**, and a modular **Clean Architecture** powered by Room, Ktor, and Coroutines.

---

## 🚀 Key Features

### 🛰️ 1. Multi-Platform Live Contest Radar
- **Real-Time Feed**: Aggregates live and upcoming contests across **LeetCode, Codeforces, CodeChef, and AtCoder**.
- **Live Countdown Timers**: Precision millisecond and second tickers showing exact time until contest starts.
- **Dynamic Status Chips**: `LIVE NOW` with pulsing neon radar indicators, `UPCOMING`, and `COMPLETED`.
- **Smart Filtering & Search**: Instant filtering by platform, contest category, duration, and keywords.

---

### 📈 2. Real-Time Rating Curves & Performance Analytics
- **Live Rating History Engine**: Interactive rating progression curves with gradient fills for Codeforces and LeetCode.
- **Statistics Overview**: View Current Rating, Highest Rating, Global Rank, and Total Contests Participated.
- **Problem Difficulty Matrix**: Easy, Medium, and Hard problem breakdowns with stacked progress indicators.
- **Zero Fake Data**: Authentic dynamic data calculations based on connected handles.

---

### 🐙 3. GitHub Matrix & Developer Showcase
- **Day-Wise Activity Grid**: Authentic 52-week contribution heatmap matrix.
- **Public Repositories Browser**: Real-time repository showcase with primary languages, stars ⭐, forks 🍴, and descriptions.
- **Direct GitHub Web Deep Links**: 1-tap navigation directly to repository pages on GitHub.

---

### 🔥 4. Daily Coding Streak Habit System
- **Daily App Open Habit Tracker**: Automatic +1 streak calculation on genuine daily app opens.
- **Full-Screen Activity Calendar (`StreakScreen`)**: Monthly heatmap calendar highlighting active days and streaks.
- **Milestone Celebration Modal**: Animated celebratory dialog upon unlocking streak milestones.
- **1-Tap Share Streak**: Share your coding streak directly to WhatsApp, Twitter/X, and LinkedIn.

---

### 📇 5. Interactive QR Code Generator & Tri-Mode Share Modal
- **Deterministic 25×25 Canvas QR Generator**: High-performance, razor-sharp QR matrix rendering with custom neon colors.
- **Tab 1: Profile & Coding Streak Share Card**: Story/Social graphic card featuring avatar, streak badge, feature checklist, mini QR, and 1-tap social share text.
- **Tab 2: App Download QR Code**: Large scannable QR Code linking directly to the Google Play Store.
- **Tab 3: Creator Digital vCard**: Direct contact chips for portfolio, LinkedIn, Instagram, GitHub, and email.

---

### 🤖 6. Curated Developer Hub & Education Resources
- **AI & Machine Learning Platforms**: Direct links to **Hugging Face Hub**, **Google Colab (Free GPU)**, **Kaggle**, **OpenAI Platform & Docs**, **PyTorch Tutorials**, **Ollama (Local LLMs)**, and **v0 by Vercel**.
- **Masterclass YouTube Playlists**: Full courses from **Striver (takeUforward - A2Z DSA & DP)**, **NeetCode (NeetCode 150)**, **Andrej Karpathy (Neural Networks: Zero to Hero)**, **3Blue1Brown (Visual Backpropagation & Linear Algebra)**, **StatQuest (Josh Starmer)**, **Kunal Kushwaha (Java + DSA)**, and **Love Babbar (450 DSA Cracker)**.
- **Curated Problem Sheets & Roadmaps**: **Striver SDE 180 Sheet**, **CSES 300 Problem Set**, **USACO Guide**, **CP-Algorithms (E-Maxx)**, and **System Design Primer**.
- **Instant Search**: Real-time glassmorphic search bar with category filter tabs.

---

### 🎨 7. 5-Slide Infographic Onboarding & Majestic Auth
- **Cinematic Animated Splash Screen**: Sweeping neon cyber ring, spring scaling physics, and staggered typography reveal.
- **5-Page Animated Infographics**: Live Contest Radar, Unified Ratings, Heatmap & Streak, Smart Calendar Sync, and Curated Roadmaps with dynamic spring indicator pills.
- **Majestic Glassmorphic Auth (`AuthScreen`)**:
  - Sign In & Create Account tab switcher.
  - Fast GitHub Handle verification.
  - Email & Password with inline validation.
  - **Instant Guest Bypass Card** for 1-tap offline access.

---

### 💫 8. State-of-the-Art Shimmer Skeletons & Cyber Loading
- **Diagonal 45° Sweeping Shimmer Brush**: Smooth multi-color gradient luminance sweep across cards and charts.
- **CyberLoadingSpinner**: Futuristic dual counter-rotating orbital neon arcs (Electric Indigo & Sky Cyan) with glowing center node.
- **Screen Skeletons**: `HomeScreenSkeleton`, `ContestCardSkeleton`, `PlatformDetailSkeleton`, and `ResourcesListSkeleton`.

---

### ⏰ 9. Smart Alarms & Calendar Auto-Sync
- **1-Tap Android Calendar Sync**: Export registered contests directly to the device calendar via Android `CalendarContract`.
- **Pre-Contest Alarms**: Configurable 15-minute reminders before tracked contests start.

---

## 🛠️ Tech Stack & Architecture

| Category | Technology |
|---|---|
| **Language** | Kotlin 2.0.0 |
| **UI Framework** | Jetpack Compose (BOM 2024.02.00) + Material Design 3 |
| **Architecture** | Clean Architecture (MVVM) + Multi-Module Pattern |
| **Navigation** | Jetpack Navigation Compose |
| **Local Database** | Room Database 2.6.1 + KSP Symbol Processing |
| **Networking** | Ktor Client 2.3.8 + Kotlinx Serialization (JSON) |
| **Async & State** | Kotlin Coroutines + StateFlow / SharedFlow |
| **QR Code Engine** | Pure Kotlin Deterministic 25×25 Matrix + Compose Canvas |
| **Min SDK** | Android 8.0 (API 26) |
| **Target / Compile SDK**| Android 14 (API 34) |

---

## 📂 Project Architecture

```text
CodeCalendar/
├── app/                        # Application entry point, MainActivity, Themes, ProGuard
│
├── core/
│   ├── designsystem/           # Color tokens, Typography, GlassCard, ShimmerEffect, FloatingBottomNav
│   ├── database/               # Room DB, DAOs (PlatformStatsDao, RatingHistoryDao, SyncStateDao)
│   ├── network/                # Ktor client, RemoteDataSource, Contest API models
│   ├── common/                 # NetworkMonitor, DateFormatters, DispatcherProviders
│   ├── notifications/          # ReminderScheduler, AlarmManager integration
│   ├── model/                  # Domain-shared cross-module models
│   └── analytics/              # Analytics tracker abstraction
│
├── domain/
│   └── model/                  # Pure Kotlin entities (Contest, PlatformStats, StreakInfo, Resource)
│
├── data/
│   ├── repository/             # FakeRepository, Offline Cache, DomainEntityMappers
│   ├── local/                  # Local Room data source delegates
│   ├── remote/                 # Network data source delegates
│   └── mapper/                 # Entity ↔ Domain model mappers
│
└── feature/
    ├── home/                   # Dashboard, Hero next contest card, StreakScreen, HomeViewModel
    ├── contests/               # Contest list, Platform filters, ContestCard, Past contest records
    ├── contestdetail/          # Single contest details, Google Calendar export, Alarm trigger
    ├── platformdetail/         # Rating progression charts, Solved breakdown, GitHub repo list
    ├── platforms/              # Add/connect platform handle with verification
    ├── resources/              # AI/ML Tools, YouTube Masterclasses, DSA Sheets, Search Bar
    ├── settings/               # Developer Showcase Card, QR Generator, ShareAppContactCardModal
    └── onboarding/             # SplashScreen, 5-Slide Infographic Onboarding, AuthScreen
```

---

## 🎨 Supported Platforms

| Platform | Contests Radar | Rating Curves | Solved Breakdown | Heatmap & Streak |
|---|:---:|:---:|:---:|:---:|
| **LeetCode** | ✅ | ✅ | ✅ | ✅ |
| **Codeforces** | ✅ | ✅ | ✅ | ✅ |
| **CodeChef** | ✅ | ✅ | ✅ | — |
| **AtCoder** | ✅ | ✅ | — | — |
| **GitHub** | — | — | — | ✅ (52-Week Grid) |

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio Hedgehog** (2023.1.1) or newer
- **JDK 21** (Required for Kotlin 2.0 KSP symbol processing)
- **Android SDK Platform 34**
- **Git**

### Clone & Build

```bash
# Clone the repository
git clone https://github.com/vishal-bhutekar21/MyCodeCalender.git
cd MyCodeCalender

# Compile debug sources
./gradlew compileDebugSources --no-daemon

# Assemble Debug APK
./gradlew assembleDebug

# Install on connected device or emulator
./gradlew installDebug
```

Output APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 👨‍💻 Meet the Creator

<div align="center">

### **Vishal Bhutekar**
*Android & Full-Stack Developer*

[![Portfolio](https://img.shields.io/badge/Portfolio-vishalbhutekar.netlify.app-06B6D4?style=for-the-badge&logo=google-chrome&logoColor=white)](https://vishalbhutekar.netlify.app/)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-vishal--bhutekar21-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/vishal-bhutekar21/)
[![GitHub](https://img.shields.io/badge/GitHub-vishal--bhutekar21-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/vishal-bhutekar21)
[![Instagram](https://img.shields.io/badge/Instagram-unexplored__vish__2.0-E4405F?style=for-the-badge&logo=instagram&logoColor=white)](https://www.instagram.com/unexplored_vish_2.0/)
[![Google Play Dev](https://img.shields.io/badge/Google%20Play-Developer%20Page-00E676?style=for-the-badge&logo=googleplay&logoColor=white)](https://play.google.com/store/apps/dev?id=8656025420118431472)
[![Email](https://img.shields.io/badge/Email-vishal.bhutekar1%40gmail.com-EA4335?style=for-the-badge&logo=gmail&logoColor=white)](mailto:vishal.bhutekar1@gmail.com)

</div>

---

## 📜 License

```text
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

<div align="center">

*Crafted with ❤️ using Kotlin · Jetpack Compose · Material 3 Glassmorphism*

</div>
