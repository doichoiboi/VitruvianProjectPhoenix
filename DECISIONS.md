# Vitruvian Project Phoenix - Decision Log

> Maintained by Ledger (RECORDER). Append-only. One entry per session.
> New threads: read this alongside CAMPAIGN.md to pick up full context.

---

## Session 001 - 2026-04-11 - Forge Campaign Bootstrap

### Decisions Made

**Campaign created from existing repo context**
- This was not a greenfield kickoff. The Android app repo already existed under `campaigns/VitruvianProjectPhoenix`.
- Forge campaign files were created from README/build context instead of stopping Daniel for a full intake.
- Theme selected for this Build: The Salvage Bay.

**Core project identity captured**
- Native Android app for controlling Vitruvian Trainer machines over BLE.
- Community rescue purpose: keep owner hardware useful and avoid e-waste.
- Stack captured as Kotlin, Jetpack Compose, Material 3, Hilt, Room, DataStore, Coroutines/Flow, Nordic BLE, Kable, and Timber.

**Rework and attribution direction**
- Daniel plans to rework and rename the app; "Vitruvian Project Phoenix" is not the final product name.
- Attribution must stay explicit: the original work came from another developer/project, and future rebrand work should not blur that history.
- Added R-005, "Rebadge the Build," to plan rename/rebrand and credit surfaces together.

### Open Threads

- First technical orientation repair should inspect architecture, BLE boundaries, build flavors, and test commands.
- Version metadata decision: current public/review lane is beta `0.6.2-beta`; Gradle production `1.1.0` remains unreleased metadata until Daniel explicitly promotes a production release.
- Legal/release wording should preserve the non-affiliation disclaimer and owner-rescue framing.

---

## Session 002 - 2026-05-12 - Clean Architecture Base Cleanup

### Decisions Made

**Prefer focused composition modules over a catch-all AppModule**
- The Hilt `AppModule` was deleted after splitting its remaining providers into
  smaller modules by responsibility: BLE, repositories, preferences, exercise
  import, domain use cases, and database.
- The team chose not to remove existing constructor/class-level injection in
  this pass. Constructor injection can remain the dependency contract while
  modules define app composition boundaries.

**Treat migrations as data-local persistence code, not DI code**
- Room migrations now live under `data/local/migration`.
- `DatabaseModule` only builds Room and provides DAOs.

### Open Threads

- Review whether explicit providers for already `@Inject` constructible classes
  should be removed in a later focused pass.
- Consider `@Binds` for stable interface-to-implementation bindings after the
  local injection convention is reviewed.
- Continue R-007 MainViewModel fan-out reduction after this DI cleanup slice.

---

## Session 003 - 2026-05-12 - Single Exercise Route Cleanup

### Decisions Made

**Keep pre-active-workout cleanup moving through route boundaries**
- `SingleExerciseScreen` was split behind `SingleExerciseRoute`, matching the
  route/screen pattern already used for Home, Weekly Programs, Analytics, and
  Daily Routines.
- The screen now receives explicit state and callbacks instead of importing
  `NavController` or `MainViewModel`.

**Preserve single-exercise launch ordering**
- Single Exercise still creates and loads the temp routine before connection
  gating, then starts the workout and navigates to Active Workout after
  connection succeeds.
- This ordering was kept intentionally because the prior screen owned it
  directly and this slice is a boundary cleanup, not a behavior change.

### Open Threads

- Continue R-007 with the remaining low-risk route/presenter surfaces before
  changing active workout execution.

---

## Session 004 - 2026-05-12 - Just Lift Route Cleanup

### Decisions Made

**Keep Just Lift as route-owned orchestration**
- `JustLiftRoute` now owns the ViewModel state collection, active-workout
  navigation, connected-handle detection effect, and Just Lift reset effect.
- `JustLiftScreen` is now a render/local-state surface that receives explicit
  inputs and callbacks instead of importing `MainViewModel` or `NavController`.

**Do not change workout parameter behavior in this slice**
- The existing Just Lift parameter update trigger shape was preserved so this
  remains a cleanup boundary change, not a change to active workout semantics.

Later review refined this in Session 008: the route split stayed behavior-only,
but the Echo eccentric-load sync bug was repaired as a focused next-start
parameter mapping fix.

### Open Threads

- Continue reducing route/presenter fan-out for the remaining low-risk screens
  before changing active workout execution.

## Session 005 - 2026-05-12 - Program Builder Route Cleanup

### Decisions Made

**Move Program Builder save/chrome ownership to the route**
- `ProgramBuilderRoute` now owns program loading, dynamic app chrome title, save
  top-bar action, persistence, and navigate-up behavior.
- `ProgramBuilderScreen` receives explicit draft state and callbacks instead of
  importing `MainViewModel`, `NavController`, or app chrome.

**Keep persistence semantics unchanged**
- The save action still builds the same `WeeklyProgramEntity` and
  `ProgramDayEntity` set from the current draft before calling
  `MainViewModel.saveProgram`.

### Open Threads

- Continue route/presenter cleanup around remaining non-active surfaces, then
  add focused protection before active workout changes.

---

## Session 006 - 2026-05-12 - Active Workout Route Boundary

### Decisions Made

**Split active workout rendering from route orchestration**
- `ActiveWorkoutRoute` now owns state collection, app chrome, guarded back
  behavior, completion/error navigate-up timing, PR celebration collection, and
  `MainViewModel` callback wiring.
- `ActiveWorkoutScreen` now receives explicit state and callbacks and no longer
  imports `MainViewModel`, `NavController`, or app chrome.

**Do not change active workout execution semantics**
- This slice deliberately did not change `WorkoutTab`, `WorkoutEngine`, BLE
  start/stop, rep counting, AMRAP behavior, rest transitions, or persistence.
- Future active-workout semantic cleanup needs focused tests first.

### Open Threads

- Add focused protection around active workout completion/back/AMRAP behavior
  before changing execution logic.

---

## Session 007 - 2026-05-12 - Active Route Protection

### Decisions Made

**Extract route navigation policy before changing execution**
- `ActiveWorkoutRoutePolicy` now owns the pure route decisions for guarded
  back confirmation and auto navigate-up timing.
- `ActiveWorkoutRoute` delegates to this policy instead of keeping those rules
  inline.

**Cover route behavior with fast JVM tests**
- Focused tests pin active/resting/countdown back confirmation, non-active back
  behavior, completion delay, Just Lift idle auto-reset navigation, normal idle
  no-op, and error delay.

### Open Threads

- AMRAP manual-save behavior still needs focused coverage before execution
  cleanup.

---

## Session 008 - 2026-05-12 - BLE Learning Map

### Decisions Made

**Document the communication layer for learning**
- Added `BLE_COMMUNICATION.md` as a learning-oriented map of the active BLE
  stack, scan/connect flow, service/characteristic roles, command frames,
  telemetry paths, and code-reading order.
- Linked the new note from `README.md`.

**Record manual smoke signal**
- Daniel tested the route cleanup on device/hardware after the focused and
  broad unit lanes passed and reported the flow seemed fine.

### Open Threads

- Expand the BLE note into a fuller protocol guide as we verify more command
  frames against hardware behavior.

---

## Session 009 - 2026-05-12 - Just Lift Next-Start Parameter Policy

### Decisions Made

**Treat Just Lift setup edits as next-start parameters, not live BLE writes**
- `updateWorkoutParameters` mutates app state and the idle workout engine, while
  the actual machine command is still built only in the start path.
- Echo eccentric-load changes should therefore sync into `WorkoutParameters`
  before auto-start/start builds the Echo command frame.
- This is not a mid-rep live update and does not introduce a new direct BLE
  write path.

**Move Just Lift draft mapping out of the Compose effect**
- `JustLiftParameterPolicy` now maps selected mode, Echo level, eccentric load,
  weight, progression, and unit into `WorkoutParameters`.
- Focused tests pin Echo eccentric-load/level mapping and lb progression
  conversion.

### Open Threads

- A future Just Lift owner should make the draft/applied boundary explicit
  instead of keeping it in route/screen state.

---

## Session 010 - 2026-05-12 - Just Lift Rest Elapsed Marker

### Decisions Made

**Add informational rest-elapsed display for Just Lift**
- Just Lift now records when a set ends through manual stop or auto-stop.
- While Just Lift is idle and waiting for the next set, the idle card can show
  elapsed time since the previous set ended.
- The timer is cleared when a new workout starts, so it remains informational
  and does not affect BLE commands, auto-start, or resistance behavior.
- `JustLiftRestElapsedStatePolicy` owns the mark/clear/show/elapsed rules, and
  `JustLiftRestElapsedFormatter` owns the display label formatting.

**Mark explicit additions in the UI**
- The new rest-elapsed row uses a star icon marker so it is visible as an
  explicitly added feature.

### Open Threads

- Compose route/display wiring still needs targeted tests before we claim the
  entire Just Lift rest-elapsed UI path is covered.
- Future Just Lift owner work should decide whether more explicit markers are
  needed for other newly-added app behavior.

---

## Session 011 - 2026-05-13 - Active Route Navigation State

### Decisions Made

**Keep active route navigation state pure and testable**
- `ActiveWorkoutRoutePolicy` now owns the exit-confirmation state transitions,
  route-level navigate-up action decisions, and the one-shot guard that prevents
  delayed completion/error navigation or repeated manual actions from firing
  after another route exit.
- `ActiveWorkoutRoute` still owns Compose collection and `NavController`
  calls, but delegates the fragile back/exit/auto-navigation decisions to the
  tested policy.

### Open Threads

- A full Compose route harness is still deferred; current coverage pins the
  route decision contract without standing up a `NavController` UI test.

---

## Session 012 - 2026-05-13 - AMRAP Manual-Save Coverage

### Decisions Made

**Pin AMRAP manual stop persistence before execution cleanup**
- `MainViewModelWorkoutFlowTest` now covers the AMRAP manual-stop path where
  the target reps placeholder is `0`, but the measured working reps are nonzero.
- The test captures the saved `WorkoutSession` and verifies `workingReps` and
  `totalReps` use the actual completed rep count, while `reps` remains the
  AMRAP target placeholder.
- The same test verifies manual AMRAP stop shows a `SetSummary` with the
  measured rep count.

### Open Threads

- Closed by Session 015: multi-set AMRAP progression now has focused coverage
  before deeper rest/advance cleanup.

---

## Session 013 - 2026-05-13 - Stop At Top Modern Counter Repair

### Decisions Made

**Honor `stopAtTop` in modern rep counting**
- Modern rep notifications use machine `repsSetCount` as the source of truth
  for normal bottom-confirmed reps.
- When `stopAtTop=true`, the final top-counter movement now completes the
  target rep immediately, sets `shouldStop`, and emits workout completion
  without waiting for bottom confirmation.
- If a later bottom/set-count confirmation arrives for that same final rep, it
  must not emit duplicate workout completion.
- Just Lift and AMRAP remain excluded from target-based auto-stop behavior.

### Open Threads

- Hardware smoke should specifically try a short set with `stopAtTop=true` and
  confirm the machine releases at the contracted/top position.

---

## Session 014 - 2026-05-13 - Beta Version Lane

### Decisions Made

**Keep the public/review lane on beta**
- Daniel chose to keep the current public/review app lane as beta.
- The documented current lane is now the beta flavor `0.6.2-beta`.
- Gradle production `1.1.0` remains unreleased metadata until Daniel explicitly
  promotes a production release.

### Open Threads

- Before production distribution, choose a production version/name intentionally
  and update Gradle, README, release notes, and APK naming together.

---

## Session 015 - 2026-05-13 - AMRAP Multi-Set Progression Coverage

### Decisions Made

**Pin AMRAP rest/advance behavior before semantic cleanup**
- `MainViewModelWorkoutFlowTest` now covers a two-set AMRAP routine from first
  manual stop through set summary, rest, skip-rest next-set start, second manual
  stop, and routine completion.
- The next AMRAP set must preserve `isAMRAP=true` and the `0` target reps
  placeholder while applying the configured per-set weight.
- Each completed set must save actual `workingReps` while keeping `reps` as the
  AMRAP target placeholder.

### Open Threads

- A full Compose route harness is still deferred before claiming end-to-end
  display/navigation coverage for active workout flows.

---

## Session 016 - 2026-05-13 - AMRAP Empty-Start Guard

### Decisions Made

**Do not auto-complete AMRAP before warmup completes**
- Daniel's hardware smoke found a last-set AMRAP path where the UI showed
  warmup `0/3`, resistance did not appear to load, and the app moved to
  Continue/summary anyway.
- `MainViewModel` now treats AMRAP auto-stop as ineligible until warmup has
  completed, or until actual working reps exist when warmup is disabled.
- `MainViewModelWorkoutFlowTest` now covers stalled AMRAP telemetry before
  warmup completion and verifies it stays Active without saving or stopping.

### Open Threads

- Closed by Daniel's 2026-05-14 hardware retest: the AMRAP multi-set flow is
  good after the empty-start guard.

---

## Session 017 - 2026-05-13 - Active Completion Reset Exit

### Decisions Made

**Do not let completed reset leave Active Workout mounted empty**
- Daniel's hardware smoke found that after a completed AMRAP flow, tapping a
  completion button could leave a dim/blank active-workout surface that needed
  extra back presses.
- Root cause: the active-workout route hides setup cards, but the completed
  card's reset action put the ViewModel back into Idle.
- `ActiveWorkoutRoutePolicy` now treats completed reset as a one-shot route
  exit, and `ActiveWorkoutRoute` resets the ViewModel before navigating away.

### Open Threads

- Closed by Daniel's 2026-05-14 hardware retest: the completion-reset route
  behavior is good.

---

## Session 018 - 2026-05-14 - Rest Countdown Context Request

### Decisions Made

**Capture rest-screen context as a later feature**
- Daniel wants the rest countdown to show previous-set reps while resting.
- Daniel also wants a preview of the upcoming exercise/setup during rest so
  equipment can be prepared before the next set starts.
- Logged as `F-001` in `features/F001_RestCountdownContext.md` and linked from
  the board backlog.

### Open Threads

- Implementation should likely introduce a small rest-context state shape
  rather than adding more ad hoc strings directly to `RestTimerCard`.
