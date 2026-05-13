# BLE Communication Layer

This note explains how the app talks to a Vitruvian machine today. It is meant
as a map for learning the code, not as a complete protocol specification.

## Current Stack

Runtime communication uses the Nordic BLE path:

`MainViewModel -> BleRepository -> BleRepositoryImpl -> VitruvianBleManager -> Android BLE GATT -> machine`

`BleRepositoryImpl` is the active implementation provided by Hilt in
`di/BleModule.kt`. `KableBleRepositoryImpl` exists as an alternate/experimental
implementation, but the current app binding uses `BleRepositoryImpl`.

Key files:

- `data/repository/BleRepositoryImpl.kt` - app-facing BLE repository and
  connection state.
- `data/ble/VitruvianBleManager.kt` - Nordic `BleManager` implementation,
  service discovery, command writes, polling, notifications, and parsing hooks.
- `util/BleConstants.kt` - service UUIDs, characteristic UUIDs, command IDs,
  frame size notes, and timeout values.
- `util/ProtocolBuilder.kt` - binary command frame construction.
- `data/ble/MonitorPacketParser.kt` - monitor/sample packet parsing.

## Scan And Connect

The repository starts a low-latency Android BLE scan through
`BluetoothLeScanner`. It currently scans without BLE filters, then forwards
devices whose names start with `Vee`.

Connection is by BLE address:

1. Stop scanning.
2. Resolve the Android `BluetoothDevice`.
3. Close any old `VitruvianBleManager` to avoid stale GATT state.
4. Create a new `VitruvianBleManager`.
5. Connect with Nordic BLE, timeout, retry, and `useAutoConnect(false)`.
6. Wait briefly after connection, then run the initialization path.

The manager exposes connection status back to the repository, and the repository
maps that into the app-level `ConnectionState`.

## Services And Characteristics

The main service is the Nordic UART-style service:

- NUS service: `6e400001-b5a3-f393-e0a9-e50e24dcca9e`
- NUS RX command characteristic: `6e400002-b5a3-f393-e0a9-e50e24dcca9e`

The manager also discovers Vitruvian-specific characteristics, including:

- monitor/sample data
- rep notifications
- heuristic/Echo force telemetry
- mode/version/update/auth-style notification channels
- a set of writable workout command characteristics discovered from HCI logs

The code treats monitor/sample data as a readable characteristic that must be
polled. It does not enable notifications on the monitor characteristic.

## Commands

Commands are binary frames. The manager writes full frames to the NUS RX
characteristic with `WRITE_TYPE_NO_RESPONSE`.

Important constraint: frames must not be split. The code explicitly avoids BLE
library frame splitting because the machine expects exact protocol frame sizes.

Common command IDs:

- `0x0A` - init/reset path.
- `0x4F` - regular/program workout command.
- `0x4E` - Echo workout command.
- `0x50` - official stop/halt command.
- `0x04` - activation/program-frame related command path retained in protocol
  references.

`ProtocolBuilder` is the best starting point for understanding how workout
parameters become bytes.

## Telemetry

The app reads and listens to several data paths:

- Monitor/sample polling feeds `WorkoutMetric`.
- Rep notifications feed `RepNotification`.
- Heuristic data feeds Echo/force telemetry.
- Handle state is derived from monitor position and velocity.
- Deload/status flags are parsed from monitor data and forwarded for workout
  behavior decisions.

`BleRepositoryImpl` collects these manager flows and exposes them to
`MainViewModel`.

## Start And Stop Shape

Workout start flows through `MainViewModel.startWorkout`, then
`BleRepository.startWorkout`, then `VitruvianBleManager.sendCommand`.

Stop has two related paths:

- Full workout stop sends a reset/init-style stop path and stops polling.
- Just Lift/auto-stop can send a stop command while keeping polling alive so
  handle detection and quick restart behavior remain responsive.

This split is important because Just Lift depends on continuous machine
telemetry for auto-start/auto-stop behavior.

## Debugging And Learning Path

For code reading, start in this order:

1. `di/BleModule.kt` - confirm the active implementation.
2. `data/repository/BleRepositoryImpl.kt` - understand app-facing operations.
3. `data/ble/VitruvianBleManager.kt` - understand GATT setup and command IO.
4. `util/BleConstants.kt` - map UUIDs and command IDs.
5. `util/ProtocolBuilder.kt` - inspect command frame construction.
6. `data/ble/MonitorPacketParser.kt` - inspect telemetry parsing.

For live debugging, use `BLUETOOTH_DEBUGGING_GUIDE.md`.
