# R-006 God Class Audit

## Purpose

This document records what the current oversized classes do before the cleanup
continues. The goal is not to preserve the current naming or flow. The goal is
to understand the live responsibilities well enough to replace them with a
cleaner, easier-to-debug base.

The target architecture should be closer to:

- pure domain/session logic for workout state transitions
- protocol codec code that can be tested without Android or BLE
- BLE transport code at the edge
- repository/adapters that translate edge events into app events
- ViewModels that bridge UI state and app actions
- Compose screens that render state and call callbacks
- diagnostics isolated from the normal user path

## Audit Snapshot

Update: R-008 has since removed `di/AppModule.kt`, moved Room migrations to
`data/local/migration`, and split Hilt providers into focused modules. The
AppModule row below is preserved as historical audit context for the original
cleanup target.

The current project is a single Android module. Most of the app is workable, but
the largest classes are doing too many jobs at once:

| File | Approx lines | Current role | Main risk |
|---|---:|---|---|
| `presentation/viewmodel/MainViewModel.kt` | 2900+ | Whole-app bridge, workout coordinator, BLE orchestrator, history/settings/routine manager | Hard to test and debug because platform effects, state transitions, and persistence are mixed |
| `data/ble/VitruvianBleManager.kt` | 1800+ | Nordic BLE manager, GATT discovery, polling, parsing, handle detection, diagnostics | Hardware transport and protocol/domain interpretation are coupled |
| `presentation/screen/WorkoutTab.kt` | 1900+ | Main workout surface, setup dialog, active workout UI, live metrics, video loading | UI rendering, repository reads, and workflow branching are tangled |
| `presentation/screen/HistoryAndSettingsTabs.kt` | 1700+ | History list, grouped routine history cards, settings, backup/import UI, developer tools | Two unrelated tabs and several settings workflows share one file |
| `di/AppModule.kt` | 880+ | Room migrations plus all app bindings | Migrations and dependency graph changes are hard to review |
| `data/repository/BleRepositoryImpl.kt` | 850+ | Android scanner, connection lifecycle, command sequencing, reconnection, BLE manager adapter | Repository boundary still knows too much about protocol and manager details |
| `presentation/viewmodel/ProtocolTesterViewModel.kt` | 980+ | Protocol diagnostics workflow, scanning, direct BLE manager tests, reports | Diagnostic tooling duplicates app BLE flow and has release-lint errors |
| `presentation/screen/ProtocolTesterScreen.kt` | 940+ | Diagnostic UI and report sharing | Debug UI is large and mixed with production navigation/settings |

## MainViewModel

Path: `app/src/main/java/com/example/vitruvianredux/presentation/viewmodel/MainViewModel.kt`

### What It Owns Today

`MainViewModel` is currently the central app coordinator. It owns:

- connection state exposure from `BleRepository`
- device scanning, auto-connect, connection cancellation, disconnect
- workout state, current metrics, heuristic force display, rep count, rep ranges
- workout parameter edits and unit conversion helpers
- Just Lift setup, handle detection, auto-start, auto-stop, stall detection
- workout start/stop, countdown, bodyweight timers, foreground service calls
- rep notification handling via `RepCounterFromMachine`
- metric collection and set-summary calculation
- routine loading, routine progression, per-set progression, rest timers
- single-exercise temp routine behavior
- weekly program save/delete/activate/load
- history grouping and home dashboard stats
- personal record updates and celebration events
- settings writes to `PreferencesManager`
- backup/export/import state and Android share intents
- top-bar title/action/back state
- haptic event emission

### Why It Is Hard To Debug

- It extends `AndroidViewModel`, pulls `Application`, starts services, launches
  share intents, and also owns workout business rules.
- Private mutable state is spread across many fields: session id, timers,
  collected metrics, routine ids, atomic auto-stop flags, jobs, max force, and
  bodyweight timers.
- Time is read directly with `System.currentTimeMillis()` in several behavior
  paths, so tests need awkward setup or reflection.
- It writes `_workoutState` and `_workoutParameters` directly from many methods.
  That makes state transitions implicit instead of auditable.
- It has multiple versions of "advance to next set" logic across
  `startRestTimer`, `startNextSetOrExercise`, `skipRest`, and
  `advanceToNextExercise`.
- It depends directly on data-layer repositories and data preference models
  from the presentation layer.
- It knows BLE-specific handle states (`data.ble.HandleState`) and turns them
  into user workflow decisions.

### Important Current Flows

Workout start:

1. normalize workout parameters
2. determine bodyweight vs cable exercise
3. reset/configure `RepCounterFromMachine`
4. create session id and reset collected metrics
5. run countdown through the new `WorkoutEngine`
6. either run a bodyweight timer or send BLE workout command
7. set foreground service and haptic state

Workout stop:

1. cancel timers
2. optionally send BLE stop command
3. stop foreground service
4. save session and metrics
5. branch into Just Lift, AMRAP, or normal completion behavior

Set completion:

1. stop hardware
2. save session
3. build set summary
4. branch for Just Lift, AMRAP, or routine/manual continuation
5. restart polling or handle detection for modes that need quick restart

Routine progression:

1. load routine and first set parameters
2. track routine session id/name for history grouping
3. after set summary, decide whether there are more sets/exercises
4. apply per-set weight/reps/rest and user modifications during rest
5. start next set or mark completed

History/defaults:

1. build `WorkoutSession`
2. derive measured per-cable weight from heuristic or monitor metrics
3. save metrics
4. save Just Lift or single-exercise defaults
5. update personal records if eligible

### Clean Seams

Do not try to split this file by moving random methods. Split by ownership:

| New responsibility | Suggested home | Notes |
|---|---|---|
| Workout state reducer | `domain/workout/WorkoutEngine` | Expand existing engine. Own countdown, active/rest/summary/completed transitions. |
| Routine progression | `domain/workout/RoutineProgression` | Pure logic: next set/exercise, AMRAP set semantics, per-set values, user-modified rest behavior. |
| Auto-start/auto-stop policy | `domain/workout/AutoStopPolicy` or `MachineSessionPolicy` | Pure policy from metric/range/handle state to action. |
| Session recording | `domain/workout/WorkoutSessionRecorder` contract + data implementation | Build session/default/PR save commands without Android UI state. |
| Android effects | `presentation/workout/WorkoutAndroidEffects` | Foreground service, haptics, share intents. |
| Workout screen bridge | `presentation/workout/WorkoutViewModel` | Expose state and send actions, but avoid owning domain math. |
| Settings/defaults bridge | separate settings/default ViewModel | Keep preferences out of workout orchestration. |
| History/home stats | `HistoryViewModel` / `HomeStatsViewModel` | Move grouping/streak/progress away from active workout state. |

First extraction should finish moving behavior into `WorkoutEngine` while keeping
hardware effects in `MainViewModel` until there is a better edge adapter.

## MainViewModel Fan-Out Audit

Path scan:

- `app/src/main/java/com/example/vitruvianredux/MainActivity.kt`
- `app/src/main/java/com/example/vitruvianredux/presentation/navigation/NavGraph.kt`
- `app/src/main/java/com/example/vitruvianredux/presentation/screen/EnhancedMainScreen.kt`
- `app/src/main/java/com/example/vitruvianredux/presentation/screen/HomeScreen.kt`
- `app/src/main/java/com/example/vitruvianredux/presentation/screen/JustLiftScreen.kt`
- `app/src/main/java/com/example/vitruvianredux/presentation/screen/SingleExerciseScreen.kt`
- `app/src/main/java/com/example/vitruvianredux/presentation/screen/DailyRoutinesScreen.kt`
- `app/src/main/java/com/example/vitruvianredux/presentation/screen/ActiveWorkoutScreen.kt`
- `app/src/main/java/com/example/vitruvianredux/presentation/screen/WeeklyProgramsScreen.kt`
- `app/src/main/java/com/example/vitruvianredux/presentation/screen/ProgramBuilderScreen.kt`
- `app/src/main/java/com/example/vitruvianredux/presentation/screen/AnalyticsScreen.kt`
- `app/src/main/java/com/example/vitruvianredux/presentation/screen/ConnectionLogsScreen.kt`

The coupling is not just "too many screens use one ViewModel." The pattern is
that screens reach into the same global object for unrelated reasons:

- app shell state: title, top-bar actions, back action, connection icon
- connection side effects: scan, connect, disconnect, cancel auto-connect
- workout session state: active/rest/countdown/summary, metrics, reps, haptics
- workout setup/defaults: Just Lift defaults, single-exercise defaults, units
- routine/program persistence: routines, weekly programs, active program
- analytics/history: sessions, grouped history, personal records, deletion
- settings/backup: preferences, import/export state, destructive delete
- repositories: `exerciseRepository` and `personalRecordRepository`
- formatting/conversion helpers: kg/display conversion and formatted weights

That means every feature has accidental access to every other feature's state
and effects. It also means a bug in a setup screen can mutate the same state
object driving an active workout, settings, analytics, and the global shell.

### Call Site Map

| Caller | MainViewModel usage | Better owner |
|---|---|---|
| `MainActivity` | Collects `workoutState` only to set `FLAG_KEEP_SCREEN_ON`. | `WorkoutKeepScreenOnEffect` fed by an app-level `WorkoutSessionStatus` flow. |
| `EnhancedMainScreen` | Creates `MainViewModel`, collects connection and top-bar state, calls connect/disconnect, passes the VM to every route, and fetches `exerciseRepository` via `hiltViewModel<MainViewModel>().exerciseRepository`. | `AppShellRoute` + `AppShellPresenter` for navigation chrome, `ConnectionPresenter` for machine icon/dialogs, repository injected directly where needed. |
| `NavGraph` | Takes a global `MainViewModel`, passes it to feature screens, and directly collects settings/import/export state inside the settings route. | Navigation should only compose route entrypoints. Each route obtains its own presenter/ViewModel. |
| `HomeScreen` | Reads dashboard stats, active program, routines, unit, connection overlay; starts today's routine via connection + routine load + workout start. | `HomePresenter` for dashboard/program read model; `StartWorkoutCoordinator` for "load routine and start" workflow. |
| `JustLiftScreen` | Reads session state, metrics, auto-stop, unit, connection state; loads defaults; mutates workout parameters in `LaunchedEffect`; enables handle detection; prepares Just Lift; starts active route when workout becomes active. | `JustLiftPresenter` with a local draft config and explicit `StartClicked`/`HandleDetectionReady` actions; shared `WorkoutSessionController`. |
| `SingleExerciseScreen` | Reads unit/video/default eccentric load/connection overlay; queries exercise repository directly; loads defaults; uses `TEMP_SINGLE_EXERCISE_PREFIX`; passes `personalRecordRepository`; loads temp routine and starts workout. | `SingleExercisePresenter` + `SingleExerciseSessionFactory`; exercise search/defaults should be a feature state model, not raw repositories in Compose. |
| `DailyRoutinesScreen` | Reads routines/unit/video/connection overlay; passes `personalRecordRepository`; saves/updates/deletes routines; starts routine workouts. | `RoutinesPresenter` for CRUD/read state; `StartWorkoutCoordinator` for machine connection and launch. |
| `ActiveWorkoutScreen` | Reads the largest live state set: workout state, metric, heuristic force, parameters, reps, rep ranges, auto-stop, unit, video setting, loaded routine, current index, bodyweight timer, haptics, connection, preferences, PR events. Calls scan/disconnect/connect/start/stop/skip/proceed/reset/advance/update params/top-bar/back actions/formatting. | `ActiveWorkoutRoute` collecting `ActiveWorkoutUiState`; `ActiveWorkoutPresenter` for callbacks; domain `WorkoutSessionController` for transitions. |
| `WeeklyProgramsScreen` | Reads programs, active program, routines, connection overlay; activates/deletes programs; starts today's program routine. | `ProgramsPresenter`; reuse `StartWorkoutCoordinator`. |
| `ProgramBuilderScreen` | Reads routines/programs/connection overlay; sets top-bar title/actions; builds `WeeklyProgramEntity`/`ProgramDayEntity` directly; saves program. | `ProgramBuilderPresenter` with local draft state and `SaveProgram` action; UI should not construct database entities. |
| `AnalyticsScreen` | Reads history, grouped history, all sessions, PRs, unit, connection overlay; uses `exerciseRepository` directly for display/export; deletes workouts. | `AnalyticsPresenter` and `HistoryPresenter`; exercise names should be joined/mapped before Compose. Export should be an injected use case/effect. |
| `ConnectionLogsScreen` | Has its own `ConnectionLogsViewModel` but still takes `MainViewModel` for title and connection overlays. | `ConnectionLogsPresenter` plus shared `AppShellPresenter`/`ConnectionOverlayPresenter`. |

### Presenter Extraction Targets

The clean direction is to stop passing `MainViewModel` down the tree. Routes
should collect a feature state object and render a stateless screen. A useful
intermediate naming plan:

| Feature surface | Route/Presenter | State object |
|---|---|---|
| App chrome and permissions | `AppShellRoute` / `AppShellPresenter` | `AppShellUiState` |
| Machine connection overlays/icon | `ConnectionRoute` or `MachineConnectionPresenter` | `MachineConnectionUiState` |
| Home | `HomeRoute` / `HomePresenter` | `HomeUiState` |
| Just Lift setup | `JustLiftRoute` / `JustLiftPresenter` | `JustLiftUiState` |
| Single exercise setup | `SingleExerciseRoute` / `SingleExercisePresenter` | `SingleExerciseUiState` |
| Routine library | `RoutinesRoute` / `RoutinesPresenter` | `RoutinesUiState` |
| Active workout | `ActiveWorkoutRoute` / `ActiveWorkoutPresenter` | `ActiveWorkoutUiState` |
| Weekly programs list | `ProgramsRoute` / `ProgramsPresenter` | `ProgramsUiState` |
| Program builder | `ProgramBuilderRoute` / `ProgramBuilderPresenter` | `ProgramBuilderUiState` |
| Analytics/history | `AnalyticsRoute` / `AnalyticsPresenter` | `AnalyticsUiState` |
| Settings/import/export | `SettingsRoute` / `SettingsPresenter` | `SettingsUiState` |
| Connection logs | `ConnectionLogsRoute` / existing VM cleanup | `ConnectionLogsUiState` |

These presenters do not all need to appear at once. The important rule is that
new route code should move toward:

1. collect one feature `UiState`
2. pass callbacks as a small action interface or lambdas
3. keep repositories and data entities out of Compose where practical
4. call shared coordinators for cross-feature workflows instead of reaching
   sideways into another feature ViewModel

### Shared Workflows To Extract Before Or During Presenter Split

Some behavior legitimately crosses screens. It should become explicit shared
application services rather than implicit access to `MainViewModel`.

| Workflow | Current shape | Clean shape |
|---|---|---|
| Start a routine workout | Home, routines, weekly programs, and single exercise all call connection/load/start combinations. | `StartWorkoutCoordinator` with `startRoutine(id)`, `startRoutine(routine)`, and `startSingleExercise(config)`. |
| Connection overlays | Almost every route repeats `isAutoConnecting`, `connectionError`, cancel, dismiss. | Shared `ConnectionOverlayState` rendered once by app shell or provided by a small connection presenter. |
| Top-bar title/actions/back | Feature screens mutate global title/action state directly. | Route metadata plus scoped `AppBarController`, or navigation destination config owned by shell. |
| Weight conversion/formatting | Screens call `kgToDisplay`, `displayToKg`, and `formatWeight` on `MainViewModel`. | `WeightFormatter`/`WeightUnitConverter` injected where needed; pure and unit-testable. |
| Exercise lookup for display | Screens and analytics call `exerciseRepository` from Compose. | Read models that already contain display names/media/defaults. |
| Defaults loading | Just Lift and single exercise load defaults through `MainViewModel`. | `WorkoutDefaultsRepository` behind feature presenters. |
| Import/export/share | Settings and analytics trigger Android file/share effects from presentation. | `BackupRestoreUseCase`, `CsvExportUseCase`, and platform effect adapter. |

### Extraction Order For MainViewModel Callers

The safest order is by blast radius, not by file size:

1. Extract shell-only concerns first.
   - Move top-bar title/actions/back and connection overlays toward shell-owned
     state. This reduces the need for every screen to know `MainViewModel`.

2. Extract pure helpers.
   - Move weight conversion/formatting out of `MainViewModel`. This is low
     behavior risk and removes many small dependencies.

3. Extract read-only presenters.
   - Home, analytics/history, weekly programs, and connection logs can get
     feature state without changing workout mechanics.

4. Extract setup presenters.
   - Just Lift, single exercise, routines, and program builder should move to
     local drafts and explicit save/start actions.

5. Extract active workout last.
   - `ActiveWorkoutScreen` depends on the most volatile live state and hardware
     effects. It should be split only after `WorkoutEngine`,
     `StartWorkoutCoordinator`, and connection state are clearer.

### Immediate Guardrails

- Do not add new screen parameters of type `MainViewModel`.
- Do not call `hiltViewModel<MainViewModel>()` just to access a repository.
- Do not collect settings/history/program/workout state inside `NavGraph`.
- Do not place global shell mutation in leaf composables without a scoped shell
  API.
- New Compose content should be stateless where practical: `UiState` in,
  callbacks out.

## VitruvianBleManager

Path: `app/src/main/java/com/example/vitruvianredux/data/ble/VitruvianBleManager.kt`

### What It Owns Today

`VitruvianBleManager` subclasses Nordic `BleManager` and owns:

- GATT service/characteristic discovery
- firmware/model/version diagnostic reads
- notification setup and command response collection
- connection state exposed as `ConnectionStatus`
- monitor, diagnostic, heuristic, and heartbeat polling jobs
- BLE queue protection with suspend-based sequential reads
- Android 16 Pixel service invalidation workaround
- reconnection request emission
- raw command writes
- monitor packet interpretation into `WorkoutMetric`
- velocity smoothing and sample validation
- status flag interpretation, including deload event emission
- handle state analysis for Just Lift auto-start
- rep notification parsing for 24-byte official and legacy 6-byte formats
- protocol tester helpers

### Why It Is Hard To Debug

- Transport concerns and device-domain interpretation are coupled. A GATT read
  callback currently creates `WorkoutMetric`, interprets status flags, computes
  velocity, updates handle state, and emits app events.
- Several important behaviors are stored as mutable fields on the manager:
  characteristics, polling jobs, last positions, smoothed velocities,
  strict-validation mode, deload debounce, handle thresholds, and firmware info.
- It mixes normal app behavior with reverse-engineering diagnostics and
  protocol-test helpers.
- It has multiple polling loops with slightly different timing semantics.
- It emits data-layer types directly to the repository and presentation layer.
- Several comments document hard-won device behavior. Those should be preserved
  as protocol/transport decisions, not lost in a file split.

### Important Current Flows

Connection:

1. discover NUS and related characteristics
2. optionally read firmware/model/version diagnostics
3. enable notifications on known notify characteristics
4. request high connection priority and MTU
5. mark manager ready after setup operations complete
6. start diagnostic polling and heartbeat

Monitor polling:

1. read monitor characteristic sequentially
2. parse monitor packet via `MonitorPacketParser`
3. validate position values and substitute last good positions
4. calculate signed velocity and EMA smoothing
5. emit `WorkoutMetric`
6. analyze handle state
7. emit deload event if status flags require it

Rep notifications:

1. parse 24-byte official packet when available
2. fall back to legacy 6-byte packet
3. emit `RepNotification`

Stop/cleanup:

1. cancel polling jobs
2. preserve or reset handle state depending on caller
3. clean up Nordic manager resources

### Clean Seams

| New responsibility | Suggested home | Notes |
|---|---|---|
| BLE transport | `data/ble/VitruvianGattClient` | GATT discovery, characteristic reads/writes, notification callbacks only. |
| Polling scheduler | `data/ble/VitruvianPollingCoordinator` | Sequential read loops and heartbeat timing. |
| Protocol codec | `domain/protocol` or `data/protocol` | Monitor parser, rep parser, command frames, status flags. Pure tests. |
| Telemetry adapter | `data/ble/MachineTelemetryMapper` | Raw packets -> domain telemetry events. |
| Handle detector | `domain/workout/HandleStateDetector` | Pure policy from positions/velocity to handle state. |
| Diagnostic reader | `data/ble/VitruvianDiagnosticsClient` | Firmware/version/model reads and connection log payloads. |
| Protocol tester hooks | `debug/protocol` package | Keep experimental flows out of normal BLE manager. |

The safest next BLE extraction is continuing the parser move already started:
extract `RepNotificationParser`, `MachineStatusFlags`, and velocity/handle-state
logic into pure classes with packet fixtures.

## BleRepositoryImpl

Path: `app/src/main/java/com/example/vitruvianredux/data/repository/BleRepositoryImpl.kt`

### What It Owns Today

`BleRepositoryImpl` owns:

- Android Bluetooth adapter/scanner access
- ScanResult filtering by Vitruvian device name prefix
- connection lifecycle and manager replacement
- forwarding manager flows into repository flows
- auto-reconnect after manager reconnection requests
- workout command sequencing
- start/stop/color command logging
- manager polling start/stop decisions

### Why It Is Hard To Debug

- The interface exposes Android/data BLE types (`ScanResult`, `RepNotification`,
  `HandleState`) instead of app/domain types.
- Command semantics are mixed with transport. `startWorkout` decides whether to
  send echo vs program frames and starts monitor polling.
- Stop semantics are inconsistent by mode: stop workout, send stop command
  without polling stop, restart monitor polling, enable waiting mode.
- Connection logging is repeated in every command path.
- It creates and owns `VitruvianBleManager`, but `AppModule` also provides a
  singleton `VitruvianBleManager`, which is confusing because the repository
  generally creates managers per connection attempt.

### Clean Seams

| New responsibility | Suggested home | Notes |
|---|---|---|
| Device scanner | `data/ble/AndroidVitruvianScanner` | Emits app `MachineScanResult`, not Android `ScanResult`. |
| Machine connection | `data/ble/VitruvianMachineConnection` | Holds one active manager/client and lifecycle. |
| Machine command service | `data/machine/MachineCommandClient` | `configure`, `start`, `stop`, `setLights`, no Android UI types. |
| Machine repository | `data/repository/MachineRepository` | App-facing stream and commands. |
| Logging decorator | `data/ble/LoggingMachineClient` | Remove repeated logger calls from command methods. |

## WorkoutTab

Path: `app/src/main/java/com/example/vitruvianredux/presentation/screen/WorkoutTab.kt`

### What It Owns Today

`WorkoutTab` owns:

- the workout page layout
- connection card rendering
- idle/setup, active, completed, error, countdown, rest, and summary UI
- workout setup dialog
- mode sub-selector dialog
- compact number picker
- Just Lift auto-stop display
- video playback through Android `VideoView`
- current exercise data lookup through `ExerciseRepository`
- rep counter card, bodyweight timer card, live metrics card
- vertical cable position visualization

### Why It Is Hard To Debug

- The root composable takes a very large parameter list. It is hard to know what
  state is required for which sub-state.
- Compose UI directly depends on `ExerciseRepository` and data entities.
- The setup dialog mutates global workout parameters one field at a time instead
  of editing a local draft and submitting once.
- Screen state branches are spread through a single long composable.
- Visual styling comments from older Material 3 Expressive changes overwhelm
  the structure.
- Video loading is embedded inside the workout file instead of being a reusable
  media component.

### Clean Seams

| New responsibility | Suggested home | Notes |
|---|---|---|
| Route/collector | `WorkoutRoute.kt` | Collect ViewModel state and pass callbacks. |
| Screen state model | `WorkoutUiState.kt` | One stable object instead of many root params. |
| Stateless content | `WorkoutScreen.kt` | Render only `WorkoutUiState` and callbacks. |
| Setup flow | `WorkoutSetupSheet` + draft state | Local draft until submit. |
| Active workout content | `ActiveWorkoutContent.kt` | Rep/timer/exercise/metric cards. |
| Connection content | shared `MachineConnectionCard.kt` | Can be used outside workout. |
| Exercise media | `ExerciseVideoPreview.kt` | Isolate AndroidView/VideoView. |

This is a UI split, not a behavior rewrite. It should happen after workout state
is less chaotic or in very small moves with screenshot/manual smoke checks.

## HistoryAndSettingsTabs

Path: `app/src/main/java/com/example/vitruvianredux/presentation/screen/HistoryAndSettingsTabs.kt`

### What It Owns Today

This file owns two unrelated surfaces:

- history tab
- single-session history card
- grouped routine history card
- compact session card
- settings tab
- donation link
- weight unit preferences
- workout preferences
- LED color section
- backup/export/import UI
- destructive data management
- developer tools entry points
- app info
- import result dialog
- auto-connect overlays
- timestamp/duration/color helper functions

### Why It Is Hard To Debug

- History and settings change for unrelated reasons but live in one file.
- History cards use data/repository types from presentation.
- Settings contains platform effects such as URL intents and document picker
  launcher wiring.
- The file has many repeated card style blocks.
- Several components are reusable but are private to this large file.

### Clean Seams

| New responsibility | Suggested home | Notes |
|---|---|---|
| History route/content | `history/HistoryRoute.kt`, `HistoryScreen.kt` | Pass `HistoryUiState`. |
| History cards | `history/HistoryCards.kt` | Single session and routine grouping. |
| History formatting | `history/HistoryFormatters.kt` | Timestamp/duration formatting. |
| Settings route/content | `settings/SettingsRoute.kt`, `SettingsScreen.kt` | Separate from history entirely. |
| Backup/import section | `settings/BackupRestoreSection.kt` | Keep picker and result dialog isolated. |
| Developer tools section | `settings/DeveloperToolsSection.kt` | Hide or flavor-gate later. |
| App info/attribution section | `settings/AppInfoSection.kt` | Good place for rename/credit/disclaimer work. |

## AppModule

Path: `app/src/main/java/com/example/vitruvianredux/di/AppModule.kt`

Status: resolved by R-008. The file no longer exists; this section records the
pre-cleanup shape and why it was split.

### What It Owns Today

`AppModule` owns:

- Room migrations from early database versions through v26
- database construction
- DAO providers
- repository providers
- BLE repository and BLE manager providers
- preferences manager provider
- connection logger provider
- importer/provider wiring

### Why It Is Hard To Debug

- Migrations dominate the file. Provider changes are buried under SQL.
- Version history lives partly in `WorkoutDatabase` and partly in migration names.
- The migration chain contains an intentionally empty v18->v19 cleanup comment
  while the current database is v26, which needs better explanation.
- The module provides `VitruvianBleManager`, but repository code also creates
  managers dynamically. That provider may be misleading or unused.

### Clean Seams

| New responsibility | Suggested home | Notes |
|---|---|---|
| Database module | `di/DatabaseModule.kt` | Database and DAOs. |
| Migration list | `data/local/migration/DatabaseMigrations.kt` | Ordered list and named migration objects. |
| Repository module | `di/RepositoryModule.kt` | Workout/exercise/PR repositories. |
| Machine/BLE module | `di/BleModule.kt` | BLE repository, manager, and connection logger providers. |
| Preferences module | `di/PreferencesModule.kt` | DataStore/preferences provider. |

R-008 completed this split with production compile, Android-test compile, unit
tests, and emulator launch smoke.

## ProtocolTesterViewModel And ProtocolTesterScreen

Paths:

- `app/src/main/java/com/example/vitruvianredux/presentation/viewmodel/ProtocolTesterViewModel.kt`
- `app/src/main/java/com/example/vitruvianredux/presentation/screen/ProtocolTesterScreen.kt`

### What They Own Today

The protocol tester feature owns:

- scan for Vitruvian devices
- protocol configuration matrix tests
- direct `VitruvianBleManager` creation
- connection/service-discovery timing
- init command experiments
- workout simulation
- full exercise-cycle test phases
- report generation
- report sharing UI
- test result cards and phase cards

### Why It Is Hard To Debug

- It duplicates scan/connect/workout logic instead of reusing a stable machine
  diagnostic API.
- It is in presentation but directly creates low-level BLE managers.
- It has the current lint-blocking Bluetooth permission reads at lines where
  `scanResult.device.name` is accessed.
- It belongs more to a debug/diagnostics surface than the normal app runtime.

### Clean Seams

| New responsibility | Suggested home | Notes |
|---|---|---|
| Diagnostic runner | `debug/protocol/ProtocolDiagnosticRunner` | Pure-ish workflow object with injected scanner/client. |
| Diagnostic ViewModel | `debug/protocol/ProtocolTesterViewModel` | State bridge only. |
| Diagnostic UI | `debug/protocol/ProtocolTesterScreen` | Can stay large temporarily after runner extraction. |
| Report formatter | existing `util/ProtocolTester` or `debug/protocol` | Keep formatting out of ViewModel. |
| Release gating | build flavor or explicit debug setting | Decide if this ships to public users. |

## Cross-Cutting Boundary Leaks

These are not all blockers, but they explain why the app feels hard to reason
about:

- Presentation imports data entities/repositories in several places:
  `WorkoutTab`, `HistoryAndSettingsTabs`, `ExerciseLibraryViewModel`,
  `ConnectionLogsViewModel`, `ExercisePickerDialog`, `ProgramBuilderScreen`,
  `SingleExerciseScreen`, `RoutinesTab`, and others.
- Domain imports data in at least one model area (`PRType` from data local).
- Android intents/FileProvider appear inside ViewModel or screen code.
- Time is read directly from `System.currentTimeMillis()` across domain,
  presentation, BLE, and formatting code.
- Several current APIs expose Android BLE types instead of app-level types.
- Kable is half-removed: dependencies remain, while module/implementation code
  is commented out.
- Release metadata is still imported-project shaped:
  `com.example.vitruvianredux`, version drift, and debug-like release signing.

## Proposed New Names And Concepts

We are not bound to the current names. These names are only working vocabulary:

| Current concept | Better concept |
|---|---|
| `MainViewModel` | `AppShellViewModel` plus feature ViewModels |
| `BleRepository` | `MachineRepository` or `TrainerRepository` |
| `VitruvianBleManager` | `VitruvianGattClient` / `MachineBleTransport` |
| `WorkoutParameters` | `WorkoutPrescription` or `SetPrescription` |
| `WorkoutState` | `WorkoutSessionState` |
| `RepCounterFromMachine` | `MachineRepCounter` |
| `ProtocolBuilder` | `VitruvianCommandCodec` |
| monitor packet | `MachineTelemetryPacket` |
| rep notification | `MachineRepPacket` |
| Just Lift | `FreeLiftSession` or keep as product mode |
| Protocol Tester | `Machine Diagnostics` |

The final product naming can change later. For now the bigger improvement is to
name boundaries by behavior instead of imported project history.

## Recommended Refactor Order

1. Finish the test baseline decision.
   - Choose the canonical rep-counting behavior.
   - Update stale workout tests around that behavior.
   - Fix `MainViewModelEnhancedTest` coroutine setup.

2. Expand the pure workout engine.
   - Move rest/summary/routine progression out of `MainViewModel`.
   - Add a clock dependency.
   - Make state transitions explicit as actions and effects.

3. Extract protocol codecs.
   - `MonitorPacketParser` already exists.
   - Add rep notification parser, status flags parser, and command codec tests.
   - Keep BLE manager as transport after parsing exits.

4. Introduce app-facing machine types.
   - Replace `ScanResult`, BLE `HandleState`, and data-layer rep packets at the
     repository boundary with domain/app types.

5. Split the workout UI.
   - Add `WorkoutUiState`.
   - Split route/content/dialog/cards without changing behavior.

6. Split history and settings.
   - Separate files and state models.
   - Move backup/import and developer tools into their own sections.

7. Split DI and migration ownership.
   - Move migrations out of `AppModule`.
   - Remove or clarify unused BLE manager provider.

8. Clean dead/unclear dependencies.
   - Remove or formally park Kable.
   - Decide whether protocol diagnostics ship, are debug-only, or are hidden.

9. Release hygiene.
   - Rename package/application metadata.
   - Fix signing.
   - Decide backup policy.
   - Reconcile public version.
   - Add attribution/disclaimer surfaces.

## Debugging Payoff

After these splits, a bug report should have an obvious lane:

- "rep count is wrong" -> rep packet parser, rep counter, workout engine tests
- "machine disconnects" -> scanner/connection/transport logs
- "wrong set starts after rest" -> routine progression tests
- "history saved wrong" -> session recorder tests
- "button/UI wrong" -> Compose state/content tests or screenshot checks
- "diagnostics broke lint" -> debug protocol module, not the app surface

That is the standard this cleanup should aim for.
