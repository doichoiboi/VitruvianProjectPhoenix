# R-009 Visual System Refresh

## Goal

Make the app feel more like a serious training tool and less like a bubbly
prototype before new feature work continues.

## First Pass Scope

- Reduce global corner radii.
- Reduce heavy card elevation and thick borders.
- Normalize the global type scale for dense workout screens.
- Keep gradients restrained and route shared gradients through the theme.
- Preserve behavior and navigation. This pass is visual-system cleanup only.

## Palette Thread

The current visual target is now centralized in `ui/theme/Color.kt` and
`ui/theme/Theme.kt`. Before this pass, dark mode leaned purple, light mode
leaned teal/cyan, and several screens defined their own lavender, pink, indigo,
orange, and blue gradients directly in screen code.

Palette direction:

- Dark mode: slick modern mono tones. Use black, charcoal, zinc, and steel
  surfaces with restrained contrast. Avoid purple, lavender, teal, and saturated
  blue as the main dark-mode identity.
- Light mode: clean neutral base with orange pop. Use white/off-white surfaces,
  dark text, and orange only where the app needs energy or hierarchy.
- Keep green for success/performance, red for destructive/error, amber for
  warning, and blue/cyan only for information or connection/scanning states.

Candidate token set:

- Dark background: `#050505`
- Dark surface: `#101010`
- Dark raised surface: `#191919`
- Dark elevated surface: `#242424`
- Dark border: `#343434`
- Light background: `#FAFAF9`
- Light surface: `#FFFFFF`
- Light raised surface: `#F3F4F6`
- Light border: `#E5E7EB`
- Primary orange: `#F97316`
- Primary orange active: `#EA580C`
- Soft orange container: `#FFEDD5`
- Success/performance green: `#22C55E`
- Warning amber: `#F59E0B`
- Error red: `#EF4444`
- Info blue: `#3B82F6`
- Dark text: `#E5E7EB`
- Muted dark text: `#94A3B8`
- Light text: `#111827`
- Muted light text: `#64748B`

Gradient policy if gradients stay:

- Prefer subtle black-to-charcoal background gradients in dark mode.
- Prefer flat neutral surfaces in light mode with orange accent areas.
- Avoid lavender/pink/purple gradients for the main app shell.
- Avoid teal/cyan as the main light-mode accent.
- Avoid using orange/red gradients except for warning, destructive, or PR
  celebration moments. Orange is allowed as the light-mode brand pop, but it
  should be used intentionally rather than everywhere.

The theme pass centralizes the palette in `ui/theme/Color.kt`, exposes Material
and app-specific roles from `ui/theme/Theme.kt`, and moves the main app shell and
primary screens away from local brand-color constants.

## Theme Usage Contract

- Normal UI must use `MaterialTheme.colorScheme`, `MaterialTheme.typography`,
  and `MaterialTheme.shapes`.
- Success, warning, and info colors must use `MaterialTheme.appStatusColors`.
- Charts must use `MaterialTheme.appChartColors`; they can be more colorful
  than the rest of the app because data series need separation.
- Combo charts that compare discrete sessions must share explicit x positions
  across series and use point-to-point lines so bars and lines do not imply
  different session timing.
- Shared screen gradients and accent icon backgrounds must use
  `MaterialTheme.appBrushes`.
- Feature screens should not define their own dark/light palette logic. If a
  new visual role is needed, add it to the theme layer first.
- Direct `Color(0x...)` values are allowed in presentation code only for
  inherently content-specific colors, such as user-selectable LED swatches or
  rank/celebration colors.

## Verification

Run `:app:compileProductionDebugKotlin` after the visual reset. Use hardware or
emulator smoke only if the UI should be judged visually on device.
