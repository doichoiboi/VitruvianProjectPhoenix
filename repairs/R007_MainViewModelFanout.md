# R-007 - Reduce MainViewModel Fan-Out

## Goal

Start removing easy dependencies on `MainViewModel` before extracting feature
presenters. Keep public behavior stable while moving pure logic and repeated
cross-screen state toward explicit owners.

## First Slice

Extract weight conversion and formatting from `MainViewModel` into a pure domain
utility:

- `WeightFormatter.kgToDisplay`
- `WeightFormatter.displayToKg`
- `WeightFormatter.format`

`MainViewModel` keeps delegating wrapper methods for now so existing screens do
not need to change in the same commit.

## Validation

- Add focused JVM tests for kg/lb conversion and display formatting.
- Run the focused test class first.
- Run broader compile/unit checks after the first slice is green.

## Follow-Up Slices

- Replace direct screen calls to `MainViewModel` formatting methods with
  injected/passed formatter callbacks from feature presenters.
- Add route-local presenters for low-risk screens before touching active
  workout.

## Second Slice

Lift repeated connection overlay rendering out of leaf screens:

- `EnhancedMainScreen` renders `ConnectingOverlay` and `ConnectionErrorDialog`
  once at the shell level.
- Feature screens no longer collect `isAutoConnecting` or `connectionError`.
- Settings no longer receives connection overlay parameters through `NavGraph`.

This is still a transitional shape because `EnhancedMainScreen` is using
`MainViewModel` as the shell source. The important improvement is that the
overlay is now owned by one shell surface instead of being duplicated across
every route.

## Third Slice

Move static app-bar title ownership into the shell:

- `EnhancedMainScreen` derives static app-bar titles from the current route.
- `AppNavigationHub` now centralizes route metadata: static title, analytics
  name, workout-section grouping, bottom-bar visibility, top-level/back-button
  behavior, and dynamic-title eligibility.
- `AppDestination` is a sealed destination contract so route metadata remains
  a closed app model instead of loose config rows.
- Static screens no longer call `MainViewModel.updateTopBarTitle(...)` just to
  set route names.
- `SettingsTab` and `ConnectionLogsScreen` no longer receive title callbacks.
- Dynamic title routes are still explicitly allowed for now:
  `ActiveWorkoutScreen` and `ProgramBuilderScreen`.
- `EnhancedMainScreen` no longer calls `hiltViewModel<MainViewModel>()` a
  second time just to access `exerciseRepository`; it uses the already-created
  shell ViewModel as the temporary provider.

This avoids both callback plumbing and feature ViewModels mutating shell chrome
for static titles. It does not solve dynamic app-bar actions yet; those still
need a proper shell presenter/controller before `ProgramBuilderScreen` and
`ActiveWorkoutScreen` can stop reaching into `MainViewModel`.

## Fourth Slice

Move dynamic app chrome ownership out of `MainViewModel`:

- `AppChromeController` now owns transient dynamic title, top-bar actions, and
  route-specific back actions.
- `EnhancedMainScreen` provides the controller through `LocalAppChrome` and
  renders the global app bar from shell-owned chrome state.
- `ProgramBuilderScreen` registers its dynamic title and save action with the
  shell controller instead of mutating `MainViewModel`.
- `ActiveWorkoutScreen` registers its workout title and guarded back action
  with the shell controller instead of mutating `MainViewModel`.
- `MainViewModel` no longer contains app-bar title/action/back state.

This keeps dynamic app chrome at the navigation shell boundary while leaving
workout and program behavior unchanged. Remaining fan-out is now more clearly
business/data related, especially workout execution, routine/program editing,
device connection, and repository exposure.

## Fifth Slice

Extract the shell connection affordance:

- `MachineConnectionChromeState` maps `ConnectionState` into render-ready
  title, description, icon, and color data.
- `MachineConnectionButton` renders the app-bar Bluetooth action and keeps the
  connect/disconnect click policy outside `EnhancedMainScreen`.
- Focused JVM tests pin the user-facing labels and affordances for connected,
  disconnected, connecting, scanning, and error states.

This does not change BLE behavior. It makes the shell easier to debug because
connection display policy now has its own small owner instead of living inline
inside the root screen.

## Sixth Slice

Separate the settings route from the navigation table:

- `SettingsRoute` now collects settings/import/export state and wires settings
  callbacks to `MainViewModel`.
- `NavGraph` now only declares the settings destination and delegates route
  composition to `SettingsRoute`.
- The unused `onThemeModeChange` navigation parameter was removed.

This is still transitional because `SettingsRoute` depends on `MainViewModel`.
The improvement is that navigation no longer owns settings state collection,
which gives a natural place to introduce a dedicated settings presenter later.

## Test Stabilization Slice

Restore the full production-debug unit lane:

- Updated `WorkoutModeTest` to model the current rep-counter contract:
  modern packets use ROM count for warmup reps and set count for working reps.
- Kept wrap-around coverage on the legacy directional-counter path, where
  counter deltas are still part of the implementation contract.
- Updated AMRAP and integration tests to avoid mixing warmup and working
  counters in the same packet stream.
- Added explicit `BleRepository` and `PersonalRecordRepository` mock streams to
  ViewModel tests so background init collectors do not fail from relaxed MockK
  `Nothing` values.

Validation: `:app:testProductionDebugUnitTest` is green after this slice.

## Review-Fix Slice

Address follow-up review findings before adding more shell work:

- `AppChromeController` now requires an owner key for all dynamic chrome
  updates and only clears chrome for the current owner. This prevents outgoing
  animated routes from clearing chrome already claimed by the incoming route.
- `AppNavigationHub` now returns `AppDestination.Unknown` for unknown concrete
  routes instead of inheriting Home metadata. Null route still maps to Home for
  startup behavior.
- R-006, the board, and README were synced with the current fork branch and
  validation state.

Deferred workout follow-ups:

- `B-002`: decide and repair the `stopAtTop` contract. This is a discovered
  pre-existing issue: current modern rep-counter logic stores `stopAtTop` but
  does not use it when confirming working reps.
- `B-003`: add AMRAP manual-save coverage. Current tests verify AMRAP parameter
  loading and auto-stop behavior, but do not prove manual stop saves actual
  completed reps.

## Seventh Slice

Extract the settings route owner:

- `SettingsViewModel` now owns settings UI state, preference mutations, LED
  color selection, delete-all-workouts, and data import/export actions.
- `SettingsRoute` collects a single `SettingsUiState`, uses an Activity-scoped
  `SettingsViewModel`, and handles pending export-share URIs from state.
- `NavGraph` no longer passes `MainViewModel` into the settings destination.
- `MainViewModel` no longer depends on `DataBackupManager` and no longer owns
  settings-only import/export state or preference setter methods.
- Focused `SettingsViewModelTest` coverage pins preference state mapping,
  settings actions, durable pending export-share state, import result dialog
  state, and failure fallback behavior.

This gives the first feature-owner presenter pattern to repeat for other
low-risk screens before touching active workout behavior.

Review follow-up:

- Settings import/export work stays in the settings owner but is scoped to the
  Activity instead of the settings destination, matching the previous
  `MainViewModel` lifetime for long-running import/export jobs.
- Export share delivery is state-backed through `pendingExportUri` so route
  recreation cannot drop the chooser request after the cache file is written.
- `B-004` was repaired after review: backup import now runs in a single Room
  transaction and child-row imports can restore missing metrics/exercises/days
  for existing parent records on retry.

## Eighth Slice

Split the home destination into route orchestration and a dumb screen:

- `HomeRoute` now collects active-program state from `MainViewModel` and owns
  the navigation/workout-start callbacks for the home destination.
- `HomeScreen` no longer imports `NavController` or `MainViewModel`; it receives
  render state and callbacks.
- Unused home stat collections were removed from the landing screen until the
  UI actually renders that data.

This is still transitional because the home route starts active-program
workouts through `MainViewModel`. It gives the home UI the same route/screen
shape we can later migrate to a dedicated presenter without rewriting the
visual composable again.

## Ninth Slice

Split the weekly-programs destination into route orchestration and a dumb
screen:

- `WeeklyProgramsRoute` now collects programs, active program, and routines
  from `MainViewModel`.
- `WeeklyProgramsRoute` owns Program Builder navigation plus active-program
  workout-start orchestration.
- `WeeklyProgramsScreen` no longer imports `NavController` or `MainViewModel`;
  it receives render state and callbacks.

This keeps program list rendering local while moving state collection and app
navigation toward the same route boundary established for Home and Settings.

## Tenth Slice

Split the analytics destination into route collection and screen rendering:

- `AnalyticsRoute` now collects workout history, grouped history, all workout
  sessions, personal records, and weight unit from `MainViewModel`.
- `AnalyticsRoute` passes the exercise repository, weight formatter, and delete
  callback into the analytics UI.
- `AnalyticsScreen` no longer imports `MainViewModel`; export and tab rendering
  use explicit inputs.
- The currently unused `DashboardTab` signature was also decoupled from
  `MainViewModel` so the file no longer depends on the god ViewModel.

This leaves CSV export behavior inside the screen for now. A later analytics
owner can move export side effects into a presenter once route boundaries are
stable.

## Eleventh Slice

Split the daily-routines destination into route collection and screen rendering:

- `DailyRoutinesRoute` now collects routines, weight unit, and video playback
  settings from `MainViewModel`.
- `DailyRoutinesRoute` owns the routine-start connection flow, active-workout
  navigation, and routine save/delete/update callbacks.
- `DailyRoutinesScreen` no longer imports `NavController` or `MainViewModel`;
  it delegates to `RoutinesTab` using explicit inputs.

This moves another pre-active-workout feature behind the route boundary while
leaving the routine builder/tab internals unchanged.
