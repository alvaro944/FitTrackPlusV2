# FitTrackPlus

> A fully offline, native Android workout tracker built with Kotlin and Jetpack Compose.

[![Build](https://github.com/alvaro944/FitTrackPlusV2/actions/workflows/ci.yml/badge.svg)](https://github.com/alvaro944/FitTrackPlusV2/actions/workflows/ci.yml)
![Android](https://img.shields.io/badge/Android-API%2023%2B-green?logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-blue?logo=kotlin)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.04-4285F4?logo=jetpackcompose)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

---

## Screenshots

> _Screenshots taken on a Pixel 8 emulator running Android 14 (API 34)._

| Home | Active Workout | History |
|------|---------------|---------|
| ![Home](docs/screenshots/home.png) | ![Workout](docs/screenshots/workout.png) | ![History](docs/screenshots/history.png) |

| Stats & Heatmap | Widget + Shortcuts | Dark Mode |
|----------------|-------------------|-----------|
| ![Stats](docs/screenshots/stats.png) | ![Widget](docs/screenshots/widget.png) | ![Dark](docs/screenshots/dark.png) |

---

## What is FitTrackPlus?

FitTrackPlus lets you build gym routines, log every workout session, and review your progress over time — all without an internet connection or a cloud account.

The key design decision is the **snapshot invariant**: when you start a session, the app records an immutable copy of your routine, day, and exercise targets. That means you can freely edit or delete routines later and your historical data stays exactly as it was on the day you trained.

---

## Features

### Routine Management
- Create routines with multiple training days and exercises
- Set target reps per exercise using flexible formats (`8`, `8-12`, `AMRAP`, `RPE 8`)
- Reorder and duplicate days or exercises with a tap
- Archive routines without touching historical data
- Three built-in starter templates: **Push/Pull/Legs**, **Upper/Lower**, **Full Body**

### Workout Logging
- Start a session from the active routine with one tap
- Log sets with weight (kg or lb) and reps
- Built-in **rest timer** with 60 / 90 / 120 s presets, pause and resume
- **Live PR detection**: the moment a set beats your all-time best weight or volume, a badge appears on the row and you get a double haptic pulse
- **Confetti celebration overlay** when you finish a session where you broke at least one PR

### History
- Full chronological session list with volume, duration and set count
- Filter by period (All / Last 4 weeks / Last 12 weeks)
- Sort by most recent, oldest or highest volume
- **Session comparison**: every detail view shows deltas (weight, reps, volume) against the previous comparable session from the same routine day

### Statistics
- **Heatmap calendar** — last 365 days coloured by total daily volume (5-level scale using percentiles)
- Volume-per-session line chart
- Best weight and best set volume per exercise
- Personal records table
- All stats filterable by period

### Android Integration
- **Home screen widget** (2×1): shows current workout streak and sessions this week
- **App shortcuts**: long-press the launcher icon to jump straight to Workout or Stats
- **Persistent silent notification** while a session is open — tap to return instantly

### UX & Polish
- **Shimmer skeleton loaders** on every screen while data loads
- Three-page **onboarding** for first-time users
- **Demo data** loader in debug builds (Settings → Load demo data)
- System / Light / Dark **theme selector** persisted across restarts
- Dynamic home screen: shows active routine info or a prompt to create one

---

## Tech Stack

| Layer | Technology | Why |
|-------|-----------|-----|
| Language | Kotlin 2.1.0 | Concise, null-safe, coroutine-native |
| UI | Jetpack Compose + Material 3 | Declarative, testable, design-system ready |
| Architecture | Feature-first MVVM (StateFlow + UiState) | Scalable, single source of truth per screen |
| DI | Hilt 2.57 | Standard Android DI, compile-time verified |
| Database | Room 2.7 + KSP | Type-safe SQL, Flow integration, schema versioning |
| Preferences | DataStore | Async, structured, coroutine-friendly |
| Navigation | Navigation Compose | Single-activity, type-safe routes |
| Widget | Glance 1.1 | Compose-style widget UI with Hilt EntryPoint |
| Async | Kotlin Coroutines + Flow | Cold streams, lifecycle-aware collection |
| Static analysis | Detekt | Enforces style, complexity and formatting rules |
| CI | GitHub Actions | Runs lint + Detekt + tests + build on every push |

---

## Architecture

```
com.alvarocervantes.fittrackplus
├── core/
│   ├── design/          # Design system: theme, colors, typography, components
│   ├── navigation/      # NavHost, routes, deep-link helpers
│   ├── notification/    # Active-session notification manager + observer
│   └── util/            # Extensions and helpers
├── data/
│   ├── local/           # Room database, DAOs, entities, relations
│   ├── preferences/     # DataStore repository
│   └── repository/      # Repository implementations
├── di/                  # Hilt modules
├── domain/
│   ├── model/           # Domain models (RoutineSnapshot, HeatmapDay, PrType…)
│   └── usecase/         # Business logic (GetWorkoutStreakUseCase, DetectPersonalRecordUseCase…)
└── feature/
    ├── home/            # Home screen + ViewModel
    ├── routines/        # Routines list + editor
    ├── workout/         # Active session screen
    ├── history/         # Session history list + detail
    ├── stats/           # Statistics + heatmap
    ├── settings/        # Theme, units, demo data
    ├── onboarding/      # First-run onboarding pager
    ├── launch/          # Splash / intro animation
    └── widget/          # Glance widget + receiver + Hilt entry point
```

### Key Design Decisions

**Snapshot invariant** — The most important architectural rule. When a workout session starts, the app writes immutable copies of the routine name, day name, exercise names and target reps into the session record. History and statistics always read from those snapshots, never from the live routine tables. Editing or deleting a routine never corrupts past data.

**Unidirectional data flow** — Every screen subscribes to a single `UiState` data class emitted by its ViewModel as a `StateFlow`. User actions are plain function calls; side effects (navigation, snackbars, haptics) flow through a separate `events: SharedFlow<Event>` channel.

**Repository abstraction** — Room DAOs and DataStore are hidden behind repository interfaces. Use cases and ViewModels depend only on those interfaces, making unit tests straightforward with fake implementations.

**Hilt entry point for widgets** — `GlanceAppWidget` does not support standard `@AndroidEntryPoint` injection. The widget uses `@EntryPoint` + `EntryPointAccessors.fromApplication()` to retrieve the repository from the Hilt component graph without breaking the DI boundary.

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK with API 35 installed

### Clone and build

```bash
git clone https://github.com/alvaro944/FitTrackPlusV2.git
cd FitTrackPlusV2
```

On Windows:
```powershell
.\gradlew.bat build
```

On macOS / Linux:
```bash
./gradlew build
```

### Run tests

```powershell
# Unit tests
.\gradlew.bat test

# Instrumented tests (requires connected device or emulator)
.\gradlew.bat connectedAndroidTest
```

### Install on device

```powershell
.\gradlew.bat installDebug
```

### Try demo data

Open the app in a debug build, go to **Settings → Load demo data**. This seeds a full Push/Pull/Legs routine with several weeks of finished sessions so every screen has realistic content to show.

---

## Testing

| Layer | Approach |
|-------|----------|
| Use cases | Pure unit tests with fake repository implementations |
| ViewModels | Unit tests with `kotlinx-coroutines-test` and `Turbine` for Flow assertions |
| Room DAOs | Instrumented tests with an in-memory database |
| DataStore | Instrumented tests with `ApplicationProvider` |

Run the full suite:

```powershell
.\gradlew.bat test --no-daemon
.\gradlew.bat connectedAndroidTest --no-daemon
```

---

## CI

GitHub Actions runs on every push to `main` and `codex/**` branches and on every pull request:

1. **Detekt** — static analysis
2. **Lint** — Android lint checks
3. **Unit tests** — `./gradlew test`
4. **Build** — `./gradlew build`

See [`.github/workflows/ci.yml`](.github/workflows/ci.yml).

---

## Project History

FitTrackPlus v2 was built phase by phase following a strict methodology: one branch per phase, small scope, automatic verification (tests + build), manual verification on device, and updated docs before the next phase starts.

| Phase | Scope |
|-------|-------|
| 0 | Android foundation: Compose, Hilt, Room, DataStore, Navigation |
| 1 | Routine management: create, edit, archive, activate |
| 2 | Workout logging: sessions, sets, finish flow |
| 3 | History: list and detail with snapshot data |
| 4 | Statistics MVP: progress charts, volume, personal records |
| 5 | UX polish: states, errors, accessibility, new-user flow |
| 6 | Visual redesign: design system, animations, dark mode |
| 2.1A | Stability: error feedback, input guards, inline validation |
| 2.1B | Value features: templates, duplicate/reorder, rest timer, filters, periods, session comparison, theme selector |
| 2.1C | Portfolio WOW: heatmap, live PR detection, celebration, skeleton loaders, onboarding, demo data, app shortcuts, widget, active-session notification |

---

## License

MIT — see [LICENSE](LICENSE) for details.

---

## Author

**Alvaro Cervantes**
[GitHub](https://github.com/alvaro944) · [LinkedIn](https://linkedin.com/in/alvarocervantes)
