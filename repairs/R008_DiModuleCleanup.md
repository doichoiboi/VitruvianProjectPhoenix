# R-008 - Split DI Composition Boundaries

## Goal

Remove the catch-all `AppModule` and make dependency wiring easier to debug
without changing runtime behavior or switching injection style in the same
slice.

## Completed Slice

- Moved Room migrations from `di` to `data/local/migration/DatabaseMigrations`.
- Kept `DatabaseModule` focused on Room construction and DAO providers.
- Replaced `AppModule` with focused Hilt modules:
  - `BleModule` owns BLE manager, connection logger, and BLE repository binding.
  - `RepositoryModule` owns repository construction.
  - `PreferencesModule` owns preferences construction.
  - `ExerciseImportModule` owns exercise import construction.
  - `DomainModule` owns the rep-counter use case provider.
- Updated `KableBleModule` comments so future Kable switching points at
  `BleModule` instead of the deleted `AppModule`.
- Updated migration tests to import migrations from the data-local package.

## Deferred On Purpose

- Do not remove existing class-level `@Inject` annotations yet.
- Do not convert interface bindings to `@Binds` yet.
- Do not move repository interfaces/classes into new packages yet.
- Do not split Gradle modules yet.

Those are reasonable next steps, but they change ownership conventions beyond
the mechanical module split and should get a focused review before landing.

## Current DI Shape

- `di/DatabaseModule.kt`: database builder plus DAO providers.
- `data/local/migration/DatabaseMigrations.kt`: migration objects plus the
  `ALL` list used by the Room builder.
- `di/BleModule.kt`: BLE-facing app composition.
- `di/RepositoryModule.kt`: repository composition.
- `di/PreferencesModule.kt`: DataStore preferences composition.
- `di/ExerciseImportModule.kt`: bundled exercise import composition.
- `di/DomainModule.kt`: small domain-use-case composition.

## Recommended Next Cleanup

- Review whether `PreferencesManager` and `ExerciseImporter` should rely on
  constructor injection directly or keep explicit providers.
- Convert stable interface bindings like `BleRepository` and
  `ExerciseRepository` to `@Binds` after deciding that constructor injection is
  the preferred local convention.
- Consider moving repository interfaces out of implementation files so DI
  modules can read as boundary declarations instead of implementation details.
- Continue R-007 by extracting the next low-risk route owner before touching
  active workout execution.

## Validation Commands

Run these after any follow-up DI edits:

```powershell
.\gradlew.bat :app:compileProductionDebugKotlin
.\gradlew.bat :app:compileProductionDebugAndroidTestKotlin
.\gradlew.bat :app:testProductionDebugUnitTest
```

For runtime smoke when an emulator is already running:

```powershell
.\gradlew.bat :app:installProductionDebug
adb -s emulator-5554 shell am start -n com.example.vitruvianredux.debug/com.example.vitruvianredux.MainActivity
adb -s emulator-5554 logcat -b crash -d
```

If multiple devices are attached and Gradle targets the wrong one, build first
and install directly to the emulator:

```powershell
adb -s emulator-5554 install -r app\build\outputs\apk\production\debug\app-production-debug.apk
```
