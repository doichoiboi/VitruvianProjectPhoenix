# R-010 MainViewModel Event Reducer

## Goal

Move `MainViewModel` toward one explicit event entry point without rewriting
workout execution in a single risky pass.

## First Slice Scope

- Add `MainViewModelEvent` as the typed event contract for app-shell actions.
- Add `MainViewModel.onEvent(event)` with a direct `when` over supported events.
- Route low-risk shell interactions through the event path first:
  - theme mode changes
  - device connection requests
  - machine disconnect
  - auto-connect cancellation
  - connection error dismissal
  - connection-lost alert dismissal
- Keep existing public methods as wrappers for now so current screens and tests
  do not need a broad rewrite.

## Guardrails

- Do not move workout start, rep counting, rest progression, routine
  progression, or save behavior into events until focused regression coverage
  already exists for the behavior being moved.
- Events may trigger existing private implementations, but they should not hide
  callback-heavy flows behind ambiguous lambdas.
- Prefer one small group of related events per slice, followed by tests.

## Validation

- Add focused JVM coverage proving the first event path delegates to the same
  theme, BLE, and disconnect behavior.
- Run `:app:compileProductionDebugKotlin`.
- Run `:app:testProductionDebugUnitTest`.

## Follow-Up Slices

- Introduce feature-specific events after their UI state is stable.
- Move app-shell connection callbacks into a cleaner coordinator only after the
  current `ensureConnection` callback contract is covered.
- Continue replacing direct Compose collections with stable screen UI state
  models before deeper workout-flow event migration.

## Connection Coverage Slice

Focused `ensureConnection` coverage now pins the app-shell connection contract:

- already-connected state calls `onConnected` without scanning
- scan timeout clears the connecting overlay, cancels BLE work, and reports the
  timeout error
- user cancellation clears the overlay without calling `onFailed`
- discovered-device success stops scanning, connects, clears the overlay, and
  invokes `onConnected` exactly once

The success-path test exposed a stale pending-callback path that could call
`onConnected` twice. `ensureConnection` now owns that callback decision, while
`connectToDevice` only connects and clears the overlay when connection state
reports success.
