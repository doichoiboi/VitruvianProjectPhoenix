# R-006 - Forge the Clean Workout Base

## Goal

Make the project easier to debug and safer to evolve before visual redesign work.
The first cleanup target is the workout flow because it currently concentrates
machine control, timers, mode behavior, history, haptics, and UI-facing state in
large Android-facing classes.

## Architecture Direction

Use the Moyo pattern, adapted to this single-module Android repo:

- domain contracts first, Gradle module extraction later
- Android and BLE platform work at the edge
- ViewModels shape and bridge state instead of owning all behavior
- Compose screens render state and call callbacks
- focused tests around each extracted boundary

## First Boundary

Create a pure Kotlin workout engine under `domain/workout` that can grow into
the owner of workout orchestration:

- countdown and active-state transitions
- machine start/stop effects
- workout metric collection
- set summary state
- explicit action/effect/event models

Do not move the full `MainViewModel` flow in one pass. Replace behavior by
small tested slices after the boundary exists.

## Acceptance Criteria

- `domain/workout` has a documented state/action/effect contract.
- The first engine tests pass without Android, BLE, Hilt, Room, or Compose.
- `MainViewModel.startWorkout(...)` uses the engine for start/countdown state
  while hardware effects remain at the Android/BLE edge.
- `MainViewModel` routes active workout metric collection and set-summary
  calculation through the engine.
- Raw monitor packet decoding is isolated behind a pure parser test instead of
  reflection into the BLE manager.
- Existing app compile still passes.
- No broad Gradle module split is introduced in this repair.
- Existing full-suite failures are recorded as baseline debt, not hidden.

## Follow-Up Repairs

- Split `MainViewModel` into Android bridge plus workout engine integration.
- Extract protocol frame generation/parsing from BLE manager into testable pure
  Kotlin protocol classes.
- Split `WorkoutTab` into route, stateful screen, and dumb content components.
- Triage stale legacy tests before relying on the full unit suite as a release
  gate.

## Research Notes

- `repairs/R006_GodClassAudit.md` maps the current oversized classes, the
  responsibilities they own today, and the proposed clean seams for replacing
  them without preserving inherited naming or flow.
- The audit now includes every current `MainViewModel` caller and should guide
  presenter extraction. The key rule is to stop passing `MainViewModel` down the
  navigation tree; route entrypoints should collect feature `UiState` objects
  and call explicit actions/coordinators.
