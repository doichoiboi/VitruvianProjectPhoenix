# The Salvage Bay - Shop Board

> Active/proposed repairs live here. Detailed repair specs can move to `repairs/` when the Build needs them.

---

## Active Repairs

| ID | Title | Owner | File / Note |
|---|---|---|---|
| R-007 | Reduce MainViewModel Fan-Out | Spanner -> Sightline | `repairs/R007_MainViewModelFanout.md`; route boundaries extracted through active workout; active route navigation state/timing, AMRAP manual-save, multi-set progression, and empty-start guarding, Just Lift next-start parameter mapping, and rest-elapsed state/formatting now have focused JVM coverage; full Compose route harness remains deferred before deeper semantic workout changes |

---

## Proposed

| ID | Title | Owner | File / Note |
|---|---|---|---|
| R-001 | Inventory the Rig | Spanner | First orientation pass: architecture map, build variants, BLE control surface, test entry points |
| R-002 | Load Test the Build | Spanner | Run the local build/test/lint path and document the reliable verification command set |
| R-003 | True the Manual | Ledger | Current public/review lane documented as beta `0.6.2-beta`; production `1.1.0` remains unreleased metadata until Daniel promotes a production release |
| R-004 | Check the Labels | Tag -> Ledger | Confirm license, affiliation disclaimer, permissions language, and release-risk wording |
| R-005 | Rebadge the Build | Pulse -> Tag + Ledger | Plan the app rename/rebrand while preserving clear attribution to the original project and developer |

---

## Bugs

| ID | Title | Owner | Note |
|---|---|---|---|
| B-005 | AMRAP next-set resistance did not load | Spanner -> Sightline | Daniel hit this during hardware smoke on a last AMRAP set: UI showed warmup `0/3`, resistance did not seem to load, and the app moved to Continue. App-side false completion is now guarded; root no-load cause needs focused retest/log inspection. |

---

## Backlog

| ID | Title | Owner | Note |
|---|---|---|---|
| - | Hardware test checklist | Spanner + Sightline | `HARDWARE_SMOKE_TESTS.md`; running real-machine checklist for BLE, route exit, Stop At Top, Just Lift, and AMRAP smoke passes. |
| F-001 | Rest countdown context | Sightline -> Spanner | `features/F001_RestCountdownContext.md`; show previous-set reps and upcoming exercise/setup preview during rest so the lifter can prepare equipment before the timer ends. |
| - | Contributor setup pass | Ledger + Spanner | Make sure build requirements, JDK/Android Studio expectations, and hardware requirements match reality. |
| - | Permission onboarding review | Sightline + Tag | Check Bluetooth/location/notification prompts and rationale copy. |
| - | Attribution surface | Tag + Ledger | Decide where original-project credit belongs: README, About screen, release notes, and license/notice files. |

---

## Completed Repairs

| ID | Title | Owner | File / Note |
|---|---|---|---|
| R-006 | Forge the Clean Workout Base | Spanner -> Sightline | `repairs/R006_CleanWorkoutBase.md`; workout engine/parser boundary established |
| R-008 | Split DI Composition Boundaries | Spanner -> Ledger | `repairs/R008_DiModuleCleanup.md`; `AppModule` removed, database migrations moved to data-local, focused Hilt modules established |
| B-004 | Data import transaction protection | Root -> Sightline | Backup import now runs in a Room transaction and can restore missing child rows for existing parent records during retry. |
| B-003 | AMRAP manual-save coverage | Sightline -> Spanner | `MainViewModelWorkoutFlowTest` now proves manual AMRAP stop saves actual working reps instead of the zero target placeholder and shows the set summary with the measured rep count. |
| B-002 | `stopAtTop` modern rep counting | Spanner -> Sightline | `RepCounterFromMachineTest` now proves modern packets stop at the final top movement when `stopAtTop=true`, count the target rep, and suppress duplicate completion when bottom confirmation later arrives. |
| B-001 | Version drift between README and Gradle | Ledger -> Spanner | Public/review lane is beta `0.6.2-beta`; production `1.1.0` metadata is documented as unreleased until Daniel chooses a production promotion. |
| B-006 | Active workout completion reset blank route | Spanner -> Sightline | `ActiveWorkoutRoutePolicy` now treats completed-state reset as a one-shot route exit, so the Active Workout route cannot reset to hidden Idle content and leave a blank/dim workout surface. |
