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

## Risk-Reduction Slice

Pin the workout-start behavior moved into routes:

- `WorkoutLaunchCoordinator` centralizes the connection-gated launch flow used
  by Home, Weekly Programs, and Daily Routines.
- Home still loads a routine by id, starts the workout, and navigates to Daily
  Routines after connection.
- Weekly Programs still loads a routine by id and starts the workout without a
  route change.
- Daily Routines still loads the selected routine, starts the workout, and
  navigates to Active Workout after connection.
- Focused JVM tests verify ordering and confirm connection failure does not
  load, start, or navigate.

## Twelfth Slice

Split the single-exercise destination into route orchestration and screen
rendering:

- `SingleExerciseRoute` now collects weight unit, video playback, and session
  eccentric-load state from `MainViewModel`.
- `SingleExerciseRoute` owns temp-routine creation, connection gating,
  workout start, and active-workout navigation for single-exercise launch.
- `SingleExerciseScreen` no longer imports `NavController` or `MainViewModel`;
  it receives repositories, render state, formatter callbacks, defaults lookup,
  and start callback explicitly.

The route intentionally preserves the previous ordering for single-exercise
launch: create and load the temp routine first, then ensure connection, then
start the workout and navigate. That keeps behavior stable while removing
another pre-active-workout feature surface from direct god-ViewModel ownership.

## Thirteenth Slice

Split the Just Lift destination into route orchestration and screen rendering:

- `JustLiftRoute` now collects workout state, current metrics, rep count,
  auto-start/auto-stop state, weight unit, and connection state from
  `MainViewModel`.
- `JustLiftRoute` owns active-workout navigation, connected-handle detection,
  and the reset-to-Just-Lift-idle effect for non-idle/non-active states.
- `JustLiftScreen` no longer imports `NavController` or `MainViewModel`; it
  receives render state, defaults lookup, formatter callbacks, workout
  parameter mutation, and stop callback explicitly.

This keeps Just Lift behavior in the current `MainViewModel` controller for
now, but moves another leaf screen away from direct god-ViewModel and navigation
ownership. The next low-risk cleanup should continue around route/presenter
surfaces before changing active workout execution.

## Fourteenth Slice

Split the Program Builder destination into route orchestration and screen
rendering:

- `ProgramBuilderRoute` now collects routines and weekly programs from
  `MainViewModel`.
- `ProgramBuilderRoute` owns existing-program loading, dynamic app chrome title,
  save top-bar action, program persistence, and navigate-up behavior.
- `ProgramBuilderScreen` no longer imports `NavController`, `MainViewModel`,
  `LocalAppChrome`, or `TopBarAction`; it receives program draft state, routine
  lists, mutation callbacks, and theme mode explicitly.

This keeps program-builder persistence behavior unchanged while moving another
setup/editing flow behind the route boundary. The remaining high-risk area is
still active workout execution and should stay behind focused tests before any
semantic changes.

## Fifteenth Slice

Split the Active Workout destination into route orchestration and screen
rendering without changing workout execution:

- `ActiveWorkoutRoute` now collects active workout state, metrics, rep state,
  loaded-routine state, user preferences, connection state, haptic events, and
  PR celebration events from `MainViewModel`.
- `ActiveWorkoutRoute` owns dynamic app chrome title, guarded top/system back
  behavior, completion/error navigate-up timing, exit-confirmation actions, and
  `MainViewModel` workout callbacks.
- `ActiveWorkoutScreen` no longer imports `NavController`, `MainViewModel`, or
  `LocalAppChrome`; it renders `WorkoutTab`, exit confirmation, and PR
  celebration from explicit inputs and callbacks.

This is only a boundary cleanup. `WorkoutTab`, `WorkoutEngine`, BLE start/stop,
rep counting, rest transitions, AMRAP behavior, and persistence semantics were
not intentionally changed. Any semantic active-workout cleanup still needs
focused tests first.

## Active Route Protection Slice

Pin the route-level active workout navigation policy before semantic changes:

- `ActiveWorkoutRoutePolicy` now owns the small pure rules for guarded back
  confirmation, auto navigate-up timing, exit-confirmation state, and one-shot
  navigation guard state for delayed and manual exits.
- Focused JVM tests cover active/resting/countdown back confirmation,
  non-active immediate back behavior, completed-delay navigation, Just Lift
  idle auto-reset navigation, normal idle no-op, error-delay navigation,
  confirmation dialog state, confirmed exit action, and duplicate-navigation
  suppression for both auto and manual route exits.
- `ActiveWorkoutRoute` delegates those decisions to the policy while keeping
  the same route behavior.

Validation: `:app:testProductionDebugUnitTest --tests
com.example.vitruvianredux.presentation.workout.ActiveWorkoutRoutePolicyTest`
passes.

Manual smoke: Daniel tested the route cleanup on device/hardware after the
broader unit lane passed and reported the flow seemed fine.

## Just Lift Parameter Policy Follow-Up

Review found that Just Lift Echo eccentric-load changes were local UI state but
were not part of the parameter-sync trigger. That meant the setup UI could show
one Echo eccentric-load value while `WorkoutParameters` still held the previous
value until another keyed setting changed.

The follow-up keeps the safety boundary conservative:

- Just Lift controls still update app-side next-start parameters.
- BLE command emission still happens only through the existing workout start
  path, where `BleRepositoryImpl.startWorkout` builds the Echo frame.
- `JustLiftParameterPolicy` now owns the mapping from the screen draft to
  `WorkoutParameters`.
- Focused JVM tests cover Echo level/eccentric-load mapping, lb-to-kg
  progression conversion, and program-mode behavior ignoring Echo-only draft
  values.

Validation: `:app:testProductionDebugUnitTest --tests
com.example.vitruvianredux.presentation.workout.JustLiftParameterPolicyTest`
passes.

## Just Lift Rest Elapsed Feature

Add a small informational rest timer for Daniel's Just Lift smoke/testing flow:

- `MainViewModel` records elapsed time after a Just Lift set ends through
  manual stop or auto-stop.
- The elapsed timer is cleared when a new workout starts.
- `JustLiftRoute` passes the timer into `JustLiftScreen`.
- `AutoStartStopCard` shows a star-marked `Resting M:SS` row while Just Lift is
  idle and waiting for the next set.
- The star marker identifies this as an explicitly added feature, not inherited
  app behavior.
- `JustLiftRestElapsedStatePolicy` owns the tested state rules for mark, clear,
  elapsed calculation, and display eligibility.
- `JustLiftRestElapsedFormatter` owns the tested `Resting M:SS` label.

This does not add a programmed rest duration, and it does not affect BLE start,
stop, auto-start, or resistance commands.

Validation: `:app:testProductionDebugUnitTest --tests
com.example.vitruvianredux.presentation.workout.JustLiftRestElapsedFormatterTest`
and `:app:testProductionDebugUnitTest --tests
com.example.vitruvianredux.presentation.workout.JustLiftRestElapsedStatePolicyTest`
pass.

## AMRAP Manual-Save Protection

Close B-003 before deeper active-workout execution changes:

- Added focused workout-flow coverage for manual AMRAP stop.
- The test loads an AMRAP routine where the configured target reps placeholder
  is `0`, injects measured working reps, manually stops the workout, and
  captures the saved `WorkoutSession`.
- The captured session must persist actual `workingReps` and `totalReps`
  instead of the `0` AMRAP target placeholder.
- The manual-stop path must also show `WorkoutState.SetSummary` with the same
  measured rep count.

Validation: `:app:testProductionDebugUnitTest --tests
com.example.vitruvianredux.presentation.viewmodel.MainViewModelWorkoutFlowTest`
passes.
