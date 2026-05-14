# F-001 - Rest Countdown Context

## Feature Request

During the rest countdown, keep enough workout context visible for the lifter to
prepare the next set without leaving the rest screen.

## User Need

Daniel wants the rest countdown to show:

- Previous-set reps, especially the reps just completed.
- Upcoming exercise preview so equipment/cable setup can be prepared before the
  timer ends.

## Desired Behavior

- While resting between sets, show the previous set's completed working reps.
- While resting before a new exercise, show the upcoming exercise name and setup
  details that matter for preparation.
- The preview should help answer: what movement is next, what equipment/cable
  configuration is needed, target or AMRAP status, planned weight, mode, and
  any relevant rest/rep details.
- Keep the rest timer primary; this is supporting context, not a separate
  workout-planning screen.

## Notes

- Current `WorkoutState.Resting` already carries some next-exercise text and
  set counts, but it does not preserve previous-set result context.
- This likely needs a small route/view-model state shape rather than pushing
  more ad hoc strings into the Compose card.
- Useful validation should cover routine set-to-set and exercise-to-exercise
  rest transitions, including AMRAP where actual previous reps differ from the
  configured `0` placeholder.
