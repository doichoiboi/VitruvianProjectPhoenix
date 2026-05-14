# Hardware Smoke Tests

Use this as the running real-machine checklist for changes that cannot be fully
proved by JVM tests. Add new cases as features or repairs introduce hardware
behavior risk.

## How To Record Results

For each pass, add a short note with:

- date
- app build/commit if known
- machine model or identifying note
- pass/fail
- any weird timing, resistance, navigation, or history behavior

## Pending Smoke List

### BLE Connect And Basic Workout

- [ ] Launch the app fresh.
- [ ] Scan and connect to the Vitruvian machine.
- [ ] Start a simple non-AMRAP, non-Just-Lift workout.
- [ ] Confirm resistance starts normally.
- [ ] Manually stop the workout.
- [ ] Confirm the app leaves active workout cleanly and the machine is no
      longer applying workout resistance.

### Active Workout Exit Guard

- [ ] Start a normal active workout.
- [ ] Press the app bar/system back action.
- [ ] Confirm the exit confirmation appears.
- [ ] Dismiss it and confirm the workout stays active.
- [ ] Press back again and confirm exit.
- [ ] Confirm the app navigates away once and does not pop multiple screens.

### Stop At Top

- [ ] Enable `Settings -> Workout Preferences -> Stop At Top`, or enable
      `Finish At Top` in the workout setup dialog.
- [ ] Start a short normal set, such as 2 or 3 reps.
- [ ] On the final rep, stop at the contracted/top position.
- [ ] Confirm the machine unloads/stops applying workout resistance at the top
      instead of requiring the handles to return to the bottom.
- [ ] Confirm the saved workout shows the target working rep count.

### Just Lift Auto-Start And Rest Marker

- [ ] Open Just Lift.
- [ ] Confirm the app is idle and waiting for handles.
- [ ] Grab the handles and hold through the auto-start countdown.
- [ ] Confirm the workout starts.
- [ ] Finish the set manually or through the existing auto-stop behavior.
- [ ] Confirm Just Lift returns to idle for the next set.
- [ ] Confirm the star-marked `Resting M:SS` row appears and increments.
- [ ] Start the next set and confirm the rest elapsed display clears.

### Just Lift Next-Start Parameters

- [ ] In Just Lift, change mode/weight/progression settings before starting.
- [ ] For Echo mode, change Echo level and eccentric load before starting.
- [ ] Start the workout.
- [ ] Confirm the app does not send unexpected live resistance changes while
      idle and that the workout starts with the selected next-start settings.

### AMRAP Manual Stop

- [ ] Start an AMRAP routine/set.
- [ ] Complete several working reps.
- [ ] Manually stop the set.
- [ ] Confirm the set summary shows the actual completed working rep count.
- [ ] Confirm the saved history/session shows the actual completed reps, not
      the AMRAP `0` target placeholder.

## Result Notes

- 2026-05-12: Daniel ran a general hardware smoke after the route cleanup and
  reported the flow seemed fine. Specific cases above still need a focused pass.
