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
