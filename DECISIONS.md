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
- Version metadata needs review: README says `0.6.0-beta`, Gradle production says `1.1.0`, and beta flavor says `0.6.2-beta`.
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
