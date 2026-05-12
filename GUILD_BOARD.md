# The Salvage Bay - Shop Board

> Active/proposed repairs live here. Detailed repair specs can move to `repairs/` when the Build needs them.

---

## Active Repairs

| ID | Title | Owner | File / Note |
|---|---|---|---|
| R-007 | Reduce MainViewModel Fan-Out | Spanner -> Sightline | `repairs/R007_MainViewModelFanout.md`; settings owner plus home/weekly route boundaries extracted; continue route/presenter extraction before active workout |

---

## Proposed

| ID | Title | Owner | File / Note |
|---|---|---|---|
| R-001 | Inventory the Rig | Spanner | First orientation pass: architecture map, build variants, BLE control surface, test entry points |
| R-002 | Load Test the Build | Spanner | Run the local build/test/lint path and document the reliable verification command set |
| R-003 | True the Manual | Ledger | Reconcile README status/version with Gradle production and beta flavor versions |
| R-004 | Check the Labels | Tag -> Ledger | Confirm license, affiliation disclaimer, permissions language, and release-risk wording |
| R-005 | Rebadge the Build | Pulse -> Tag + Ledger | Plan the app rename/rebrand while preserving clear attribution to the original project and developer |

---

## Bugs

| ID | Title | Owner | Note |
|---|---|---|---|
| B-001 | Version drift between README and Gradle | Ledger -> Spanner | README says `0.6.0-beta`; Gradle production says `1.1.0`; beta flavor says `0.6.2-beta`. Confirm intended public version before release notes or APK distribution. |
| B-002 | `stopAtTop` is ignored by modern rep counting | Spanner -> Sightline | Discovered pre-existing issue: `RepCounterFromMachine` stores `stopAtTop`, but modern `repsSetCount` completion does not use it. Decide intended firmware behavior before changing workout stop logic. |
| B-003 | AMRAP manual-save coverage is incomplete | Sightline -> Spanner | Current tests verify AMRAP parameter loading and auto-stop behavior, but do not prove manual stop saves actual completed reps. Add a focused workout-flow test before changing AMRAP persistence. |

---

## Backlog

| ID | Title | Owner | Note |
|---|---|---|---|
| - | Hardware test checklist | Spanner + Sightline | Define the minimum real-machine test path before cutting releases. |
| - | Contributor setup pass | Ledger + Spanner | Make sure build requirements, JDK/Android Studio expectations, and hardware requirements match reality. |
| - | Permission onboarding review | Sightline + Tag | Check Bluetooth/location/notification prompts and rationale copy. |
| - | Attribution surface | Tag + Ledger | Decide where original-project credit belongs: README, About screen, release notes, and license/notice files. |

---

## Completed Repairs

| ID | Title | Owner | File / Note |
|---|---|---|---|
| R-006 | Forge the Clean Workout Base | Spanner -> Sightline | `repairs/R006_CleanWorkoutBase.md`; workout engine/parser boundary established |
| B-004 | Data import transaction protection | Root -> Sightline | Backup import now runs in a Room transaction and can restore missing child rows for existing parent records during retry. |
