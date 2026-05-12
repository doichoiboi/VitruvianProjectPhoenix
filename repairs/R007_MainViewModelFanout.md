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
