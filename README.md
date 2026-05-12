# Project: Vitruvian Project Phoenix `#3`

> *Created: 2026-04-11 | Grid Board: GUILD_BOARD.md*

---

## Identity

| | |
|---|---|
| **Project Name** | Vitruvian Project Phoenix |
| **Type** | Mobile App |
| **Platform** | Android |
| **Stack** | Kotlin, Jetpack Compose, Material 3, Hilt, Room, DataStore, Coroutines/Flow, Nordic BLE, Kable, Timber |
| **Repo / Location** | `campaigns/VitruvianProjectPhoenix`; active fork `https://github.com/doichoiboi/VitruvianProjectPhoenix`; upstream source `https://github.com/9thLevelSoftware/VitruvianProjectPhoenix` |
| **Naming Status** | Temporary import name; Daniel plans a rework and rename |

---

## Mission

**What does this project do?**
Vitruvian Project Phoenix is a native Android control app for Vitruvian Trainer workout machines over Bluetooth Low Energy. It is a community rescue project: keep useful strength-training hardware operational after company instability, preserve local control, and give owners a maintained alternative to the official app path.

**Success looks like:**
- A Vitruvian owner can install a working APK and connect to a compatible machine over BLE.
- Core workout modes remain usable: Old School, Pump, TUT, TUT Beast, Eccentric, Echo, Just Lift, and AMRAP.
- Workout data stays useful locally: history, personal records, routines, programs, and analytics are reliable.
- Contributors can build and test the project with clear setup, version, and release instructions.

---

## Constraints & Prior Decisions

> Grid: respect these. Do not re-litigate without flagging to Rex first.

- Android native app, not cross-platform.
- Android API 26+ support is required.
- BLE behavior should be tested against real Vitruvian hardware where possible.
- The app is not affiliated with or endorsed by Vitruvian.
- Daniel plans to rework and rename the app. Do not treat "Vitruvian Project Phoenix" as the final product name.
- Preserve clear credit that the original work came from another developer/project. Rebrand work must not imply Daniel authored the original codebase from scratch.
- Local control and owner rescue value are the core purpose. Avoid changes that make the app dependent on fragile external services unless Daniel explicitly chooses that path.
- Current metadata needs review: README status says `0.6.0-beta`, Gradle production version says `1.1.0`, and beta flavor says `0.6.2-beta`.

---

## Key Files by Role

### Product Vision
> Product purpose, community value, owner trust, scope guardrails
- `README.md` - this file; project overview, owner-facing purpose, and build context.
- `BLUETOOTH_DEBUGGING_GUIDE.md` - hardware troubleshooting and BLE testing context.

### Technical
> Core logic, BLE protocol, data flow, architecture, build system
- `app/src/main/java/com/example/vitruvianredux/data/ble/` - BLE communication layer.
- `app/src/main/java/com/example/vitruvianredux/domain/` - domain models and business logic.
- `app/src/main/java/com/example/vitruvianredux/data/local/` - Room database and DAOs.
- `app/src/main/java/com/example/vitruvianredux/data/preferences/` - DataStore preferences.
- `app/src/main/java/com/example/vitruvianredux/service/` - foreground service behavior.
- `app/build.gradle.kts` - Android app config, flavors, signing, dependencies.

### UX Strategy
> Workout flows, safety-critical feedback, first-run clarity, permission paths
- `app/src/main/java/com/example/vitruvianredux/presentation/screen/` - Compose screens.
- `app/src/main/java/com/example/vitruvianredux/presentation/viewmodel/` - screen state and user actions.
- `app/src/main/AndroidManifest.xml` - permissions and platform-facing behavior.

### UX Build
> Compose UI, components, theming, assets
- `app/src/main/java/com/example/vitruvianredux/presentation/components/` - reusable UI components.
- `app/src/main/java/com/example/vitruvianredux/ui/theme/` - theme configuration.
- `app/src/main/res/` - Android resources.
- `app/src/main/assets/` - bundled app assets.

### Learning
> Existing documentation, decision logs, contributor clarity
- `README.md` - this file; project identity, constraints, and current architecture.
- `DECISIONS.md` - Forge session log and durable notes.

### Project
> Grid Board and planning artifacts
- `GUILD_BOARD.md` - active repairs, proposed work, bugs, and completed work.

### Legal
> Disclaimers, permissions, data handling, release risk
- `README.md` - license, affiliation disclaimer, and support language.
- `app/src/main/AndroidManifest.xml` - Bluetooth, location, foreground service, and notification permissions.

---

## Architecture Overview

The app is a single-module native Android project. Compose screens and ViewModels drive the user workflow, Hilt wires dependencies, repositories coordinate data access, Room stores workout history and related records, and DataStore holds app preferences. BLE communication lives in the data layer and talks to Vitruvian hardware using Nordic BLE and Kable libraries, while a foreground service supports workout tracking that must continue reliably during active use.

---

## Glossary

| Term | Meaning |
|---|---|
| Vitruvian Trainer | Strength-training hardware controlled over Bluetooth Low Energy |
| BLE | Bluetooth Low Energy, the wireless protocol used to discover and control the machine |
| V-Form Trainer / VIT-200 | Original Vitruvian model listed as fully supported in the imported docs |
| Trainer+ | Later Vitruvian model listed as community verified |
| Just Lift | Quick single-exercise workout mode |
| AMRAP | As Many Reps As Possible, a supported workout mode |
| Room | Android local database used for workout and history data |
| DataStore | Android preferences storage used for app settings |
