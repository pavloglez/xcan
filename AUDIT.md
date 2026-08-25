# XCan — Architecture & Security Audit
**Auditor:** Senior Android Architect / Security Specialist  
**Date:** 2026-08-13  
**Project:** XCan — Vehicle Maintenance & OBD2 Diagnostic App  
**Scope:** Full codebase review across all modules

---

## Executive Summary

The project demonstrates a **solid architectural foundation**: multi-module structure is clean, MVI intent is present, Hilt DI is wired correctly, and the team is clearly thinking about scalability. However, several **high-severity violations** of the stated architecture rules exist, along with **critical security vulnerabilities** that must be addressed before any production deployment. The issues range from mutable global state in a singleton parser (a data corruption and thread-safety bug) to an unencrypted Room database storing sensitive vehicle diagnostics, to OBD command injection vectors.

**Severity Legend:** 🔴 Critical · 🟠 High · 🟡 Medium · 🔵 Low · ✅ Good

---

## Part 1 — Architecture Review

### 1.1 Module Structure & Dependency Graph

✅ **What's good:** The module graph is clean and correctly directional.

```
:app
  └──▶ :feature:dashboard, :feature:diagnostics, :feature:maintenance, :feature:config
           └──▶ :core:data, :core:bluetooth, :core:ui, :core:model
                    :core:data ──▶ :core:database, :core:network, :core:model
                    :core:bluetooth ──▶ :core:model
```

No circular dependencies detected. Features don't know about each other. `core:model` is a true leaf module with no Android dependencies. This is architecturally sound.

---

### 1.2 MVI Implementation

#### 🟠 ISSUE-A1 — No MVI Effects (One-Time Events)

**Files:** All ViewModels  
**Severity:** High  

None of the ViewModels implement a `Effect` channel for one-time side effects. According to the project's own AGENTS.md standard, MVI requires State + Intent + **Effect**. This means transient events (navigation, toasts, snack bars) are currently leaked into `State`, making state non-idempotent.

**Current pattern:**
```kotlin
// State used as a side-effect vehicle — wrong
data class DashboardState(
    val connectionLogs: List<String> = emptyList(), // grows forever
    ...
)
```

**Recommended fix:** Add a sealed `Effect` and expose it via `Channel`:
```kotlin
sealed interface DashboardEffect {
    data class ShowError(val message: String) : DashboardEffect
    object NavigateToDiagnostics : DashboardEffect
}

private val _effect = Channel<DashboardEffect>(Channel.BUFFERED)
val effect = _effect.receiveAsFlow()
```

---

#### 🟡 ISSUE-A2 — `ConfigState.overrideProtocol` is Dead State

**File:** [`ConfigViewModel.kt`](file:///home/pablo/lab/carSync/feature/config/src/main/java/com/jpdgbv/xcan/feature/config/ConfigViewModel.kt)  
**Severity:** Medium  

`ConfigState` declares `overrideProtocol: String = "AUTO"` and `ConfigIntent.SetProtocol` exists, but neither is persisted, nor reflected in the UI, nor derived from a repository. This is phantom state that will mislead future developers.

**Fix:** Either implement the persistence or remove it entirely. Don't ship placeholder state.

---

#### 🟡 ISSUE-A3 — `DashboardViewModel.state` Uses Fragile 11-Flow Array `combine`

**File:** [`DashboardViewModel.kt`](file:///home/pablo/lab/carSync/feature/dashboard/src/main/java/com/jpdgbv/xcan/feature/dashboard/DashboardViewModel.kt)  
**Severity:** Medium  

```kotlin
val state = combine(
    flow1, flow2, ..., flow11
) { args ->
    val status = args[0] as ConnectionStatus  // ← unchecked cast, index-based
    val telemetry = args[1] as? TelemetryFrame
    ...
}
```

Using the `Array<Any?>` form of `combine` with 11 flows is brittle. The index-to-variable mapping is invisible; any reordering of parameters silently produces wrong state with no compiler feedback. Unchecked casts (`args[0] as ConnectionStatus`) will throw `ClassCastException` at runtime, not at compile time.

**Fix:** Break the state into sub-flows and combine them in stages (max 5 per `combine`):
```kotlin
// Stage 1 — BLE state
private val bleState = combine(bleDataSource.connectionState, bleDataSource.telemetry, _connectionLogs, _discoveredDevices, _isScanning) { ... }

// Stage 2 — Vehicle & preferences  
private val vehiclePrefs = combine(carRepository.getAllCars(), carRepository.getActiveCar(), userPreferencesRepository.useMetric) { ... }

// Stage 3 — Final merge
val state = combine(bleState, vehiclePrefs, sensorState) { ble, vehicle, sensors -> ... }
```

---

### 1.3 SOLID Principles

#### 🔴 ISSUE-A4 — `ObdParser` is a Stateful Global Singleton (SRP + Thread Safety)

**File:** [`ObdParser.kt`](file:///home/pablo/lab/carSync/core/bluetooth/src/main/java/com/jpdgbv/xcan/core/bluetooth/ObdParser.kt)  
**Severity:** Critical  

`ObdParser` is declared as a Kotlin `object` (global singleton) but holds **mutable shared state**:

```kotlin
object ObdParser {
    private val currentSensors = mutableMapOf<String, Float>() // ← NOT thread-safe
    
    fun parse(data: String, sensorRepo: SensorRepository): TelemetryFrame? {
        currentSensors[fullPid] = evaluateFormula(...)  // ← mutates shared state
        return TelemetryFrame(sensors = currentSensors.toMap())
    }
}
```

**Problems:**
1. **Data corruption** — `currentSensors` is a `HashMap` (not `ConcurrentHashMap`). It is mutated from a coroutine running on `Dispatchers.IO`. If two coroutines call `parse()` concurrently (e.g., during Rx collection + a retry), the map will corrupt.
2. **SRP violation** — The parser both parses individual frames AND maintains global accumulated sensor state.
3. **Untestable** — Singleton state bleeds across tests. The `ObdParserTest` is already broken because of this (see ISSUE-T1).
4. **Wrong ownership** — State accumulation belongs in `BleDataSourceImpl`, not in a parser utility.

**Fix:** Convert `ObdParser` to a stateless class and move the accumulator to `BleDataSourceImpl`:
```kotlin
class ObdParser {  // Not an object
    fun parse(data: String, sensor: ObdSensor): Float? { ... }
}

// In BleDataSourceImpl:
private val _currentSensors = ConcurrentHashMap<String, Float>()
```

---

#### 🔴 ISSUE-A5 — Hardcoded `Dispatchers.IO` Throughout Data Layer

**Files:** [`CarRepository.kt`](file:///home/pablo/lab/carSync/core/data/src/main/java/com/jpdgbv/xcan/core/data/CarRepository.kt), [`MaintenanceRepository.kt`](file:///home/pablo/lab/carSync/core/data/src/main/java/com/jpdgbv/xcan/core/data/MaintenanceRepository.kt), [`TelemetryRepository.kt`](file:///home/pablo/lab/carSync/core/data/src/main/java/com/jpdgbv/xcan/core/data/TelemetryRepository.kt)  
**Severity:** Critical — **Direct violation of AGENTS.md Rule #3**  

```kotlin
suspend fun addCar(car: CarProfile) {
    withContext(Dispatchers.IO) { ... }  // ← hardcoded, untestable
}
```

This is explicitly prohibited in your own architecture rules. Hardcoded dispatchers make unit tests non-deterministic and prevent injection of `StandardTestDispatcher`.

**Fix:** Inject a `CoroutineDispatcher` via Hilt:
```kotlin
// In a DI module:
@Provides @IoDispatcher
fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

// In the repository:
class CarRepository @Inject constructor(
    private val carProfileDao: CarProfileDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun addCar(car: CarProfile) {
        withContext(ioDispatcher) { ... }
    }
}
```

---

#### 🟠 ISSUE-A6 — `DashboardViewModel` Violates SRP Massively

**File:** [`DashboardViewModel.kt`](file:///home/pablo/lab/carSync/feature/dashboard/src/main/java/com/jpdgbv/xcan/feature/dashboard/DashboardViewModel.kt)  
**Severity:** High  

This ViewModel manages **5 separate concerns** simultaneously:
1. BLE scanning (`StartScanning`, `StopScanning`, `scanningJob`)
2. BLE device connection (`Connect`, `Disconnect`)
3. Car profile management (`AddCar`, `SelectCar`)
4. Sensor discovery & selection (`ScanSensors`, `SetSelectedSensors`)
5. User preferences (`useMetric`)

It injects 4 dependencies and produces a state with 11 fields. This is a God ViewModel. It will become unmaintainable and untestable.

**Fix:** Split into coordinated ViewModels or use a shared `UiStateStore`:
- `BleConnectionViewModel` — handles scanning & connection lifecycle
- `VehicleViewModel` — handles car CRUD
- `DashboardViewModel` — reads from both + presents telemetry only

---

#### 🟠 ISSUE-A7 — `BleDataSourceImpl` Has Its Own Unmanaged `CoroutineScope`

**File:** [`BleDataSourceImpl.kt`](file:///home/pablo/lab/carSync/core/bluetooth/src/main/java/com/jpdgbv/xcan/core/bluetooth/internal/BleDataSourceImpl.kt)  
**Severity:** High  

```kotlin
private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
```

This scope is never cancelled. While as a `@Singleton` it lives for the app's lifetime (making leaks a non-issue in production), it makes **testing impossible** because you cannot cleanly reset coroutine state between tests. It also violates the dispatcher injection rule.

**Fix:** Inject an `ApplicationScope` (a long-lived scope tied to the application's lifecycle) and inject the `IO` dispatcher:
```kotlin
class BleDataSourceImpl @Inject constructor(
    private val sensorRepo: SensorRepository,
    @ApplicationScope private val appScope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BleDataSource
```

---

#### 🟡 ISSUE-A8 — `TelemetryRepository` Missing `@Singleton`

**File:** [`TelemetryRepository.kt`](file:///home/pablo/lab/carSync/core/data/src/main/java/com/jpdgbv/xcan/core/data/TelemetryRepository.kt)  
**Severity:** Medium  

Every other repository (`CarRepository`, `MaintenanceRepository`, `UserPreferencesRepository`) is annotated with `@Singleton`. `TelemetryRepository` is missing it, meaning a new instance is created at every injection point — breaking referential equality expectations.

**Fix:** Add `@Singleton` to `TelemetryRepository`.

---

#### 🟡 ISSUE-A9 — `SensorRepositoryImpl` Uses Non-Thread-Safe Mutable Collections

**File:** [`SensorRepositoryImpl.kt`](file:///home/pablo/lab/carSync/core/data/src/main/java/com/jpdgbv/xcan/core/data/repository/SensorRepositoryImpl.kt)  
**Severity:** Medium  

```kotlin
private val customSensors = mutableListOf<ObdSensor>() // ← not thread-safe
```

`saveSensor()` is a `suspend` function that mutates `customSensors` without synchronization. Since the BLE polling runs on `Dispatchers.IO` and `saveSensor` is called from `viewModelScope` (main thread context), concurrent access is realistic.

**Fix:** Use `@GuardedBy` and a `Mutex`, or replace `mutableListOf` with a thread-safe structure:
```kotlin
private val mutex = Mutex()
private val customSensors = mutableListOf<ObdSensor>()

override suspend fun saveSensor(sensor: ObdSensor) {
    mutex.withLock {
        val existing = customSensors.indexOfFirst { it.pid == sensor.pid }
        if (existing != -1) customSensors[existing] = sensor else customSensors.add(sensor)
        _sensors.value = standardSensors + customSensors
    }
}
```

---

#### 🟡 ISSUE-A10 — `evaluateFormula()` Uses String Matching (OCP Violation)

**File:** [`ObdParser.kt`](file:///home/pablo/lab/carSync/core/bluetooth/src/main/java/com/jpdgbv/xcan/core/bluetooth/ObdParser.kt)  
**Severity:** Medium  

```kotlin
when (formula) {
    "(A*256+B)/4" -> ...
    "A" -> ...
    // Adding a new formula requires editing this file
}
```

Every new OBD sensor formula requires modifying `ObdParser`, violating the Open/Closed Principle. The README claims "dynamic sensor parsing without recompiling" but the implementation contradicts this.

**Fix:** The formula string should be a proper parseable expression. Integrate a lightweight math evaluator (e.g., `exp4j` or a hand-rolled recursive descent parser), or use a `Map<String, (IntArray) -> Float>` strategy:
```kotlin
// In SensorRepositoryImpl or a FormulaEngine class:
private val formulaEvaluators: Map<String, (IntArray) -> Float> = mapOf(
    "(A*256+B)/4" to { b -> ((b[0] * 256) + b[1]) / 4f },
    "A-40" to { b -> (b[0] - 40).toFloat() },
    ...
)
```

---

### 1.4 Android & Compose Standards

#### 🟠 ISSUE-A11 — Public Composables Missing `modifier: Modifier = Modifier`

**Files:** [`DashboardScreen.kt`](file:///home/pablo/lab/carSync/feature/dashboard/src/main/java/com/jpdgbv/xcan/feature/dashboard/DashboardScreen.kt), [`ConfigScreen.kt`](file:///home/pablo/lab/carSync/feature/config/src/main/java/com/jpdgbv/xcan/feature/config/ConfigScreen.kt)  
**Severity:** High — **Direct violation of AGENTS.md Rule #2**  

Public composables including `DashboardScreen`, `TelemetryDial`, `DeviceSelectionDialog`, `ConnectionLogsDialog`, and `ConfigScreen` do not accept a `modifier` parameter. This makes them impossible to lay out correctly from a parent, violates Compose best practices, and is explicitly prohibited in your architecture rules.

**Fix:** Add `modifier: Modifier = Modifier` to every public composable signature and apply it to the root layout element.

---

#### 🟡 ISSUE-A12 — `DashboardRoute` Has Excessive Dialog State (UI State Leak)

**File:** [`DashboardScreen.kt`](file:///home/pablo/lab/carSync/feature/dashboard/src/main/java/com/jpdgbv/xcan/feature/dashboard/DashboardScreen.kt)  
**Severity:** Medium  

Five `var show*Dialog` booleans are declared with `remember` in `DashboardRoute`. While dialog visibility is UI state (correct to keep in the composable), five separate booleans can only show one dialog at a time yet nothing enforces mutual exclusion. Two dialogs being shown simultaneously is a possible bug.

**Fix:** Use a sealed class for the currently shown dialog:
```kotlin
private sealed interface ActiveDialog {
    object None : ActiveDialog
    object DeviceSelection : ActiveDialog
    object CarSelect : ActiveDialog
    object AddCar : ActiveDialog
    object Logs : ActiveDialog
    object Config : ActiveDialog
}
var activeDialog by remember { mutableStateOf<ActiveDialog>(ActiveDialog.None) }
```

---

#### 🟡 ISSUE-A13 — Type-Unsafe String-Based Navigation

**File:** [`MainActivity.kt`](file:///home/pablo/lab/carSync/app/src/main/java/com/jpdgbv/xcan/MainActivity.kt)  
**Severity:** Medium  

Navigation routes are plain string literals:
```kotlin
navController.navigate("dashboard")
navController.navigate("diagnostics")
```

A typo in any route string will cause a silent navigation failure at runtime with no compile-time detection.

**Fix:** Use Compose Navigation's type-safe routes (available since Navigation 2.8+):
```kotlin
@Serializable object Dashboard
@Serializable object Diagnostics
// ...
NavHost(startDestination = Dashboard) {
    composable<Dashboard> { DashboardRoute() }
}
```

---

#### 🟡 ISSUE-A14 — Double-Trigger of `ScanSensors` Intent

**File:** [`DashboardViewModel.kt`](file:///home/pablo/lab/carSync/feature/dashboard/src/main/java/com/jpdgbv/xcan/feature/dashboard/DashboardViewModel.kt) + [`DashboardScreen.kt`](file:///home/pablo/lab/carSync/feature/dashboard/src/main/java/com/jpdgbv/xcan/feature/dashboard/DashboardScreen.kt)  
**Severity:** Medium  

In the `init` block of `DashboardViewModel`:
```kotlin
init {
    viewModelScope.launch {
        bleDataSource.connectionState.collect { status ->
            if (status == ConnectionStatus.CONNECTED) {
                onIntent(DashboardIntent.ScanSensors)  // ← fires on connect
            }
        }
    }
}
```

And in `DashboardRoute`:
```kotlin
LaunchedEffect(Unit) {
    viewModel.onIntent(DashboardIntent.ScanSensors)  // ← also fires on composition
}
```

`ScanSensors` is triggered unconditionally on every composition AND on every connect event. The `LaunchedEffect` call fires when the screen is first shown (before any connection), causing a no-op BLE command that wastes resources.

**Fix:** Remove the `LaunchedEffect` from `DashboardRoute`. The ViewModel's `init`-block listener already handles triggering on connect. Sensor scanning should only happen when connected.

---

#### 🔵 ISSUE-A15 — `minSdk` Inconsistency Between App and Library Modules

**Files:** `app/build.gradle.kts` (minSdk = **33**), all `core/*/build.gradle.kts` (minSdk = **26**)  
**Severity:** Low  

While Gradle allows this (the app's minSdk applies at package time), the discrepancy is misleading. Code in library modules may use deprecated APIs on API 26–32 that generate lint warnings in the wrong context. Align all modules to `minSdk = 33` since the app already targets it.

---

#### 🔵 ISSUE-A16 — `fix2.py` Committed to Repository

**File:** [`fix2.py`](file:///home/pablo/lab/carSync/fix2.py)  
**Severity:** Low  

A debugging script used to mass-remove plugin declarations from build files is committed to the repository root. This should be in `.gitignore` or removed entirely. It signals that a manual patching step was needed, which should instead have been done correctly in the Gradle files from the start.

---

#### 🔵 ISSUE-A17 — Inline Version Strings in Feature `build.gradle.kts` Files

**Files:** `feature/dashboard/build.gradle.kts`, `feature/config/build.gradle.kts`  
**Severity:** Low  

```kotlin
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")  // ← not in version catalog
implementation("androidx.hilt:hilt-navigation-compose:1.1.0")           // ← not in version catalog
```

These bypass `libs.versions.toml`, creating two sources of truth for version management.

**Fix:** Add these to `libs.versions.toml` and reference them via `libs.*` aliases.

---

### 1.5 Testing Standards

#### 🔴 ISSUE-T1 — `ObdParserTest` Tests Are Broken (Wrong Method Signature)

**File:** [`ObdParserTest.kt`](file:///home/pablo/lab/carSync/core/bluetooth/src/test/java/com/jpdgbv/xcan/core/bluetooth/ObdParserTest.kt)  
**Severity:** Critical  

```kotlin
// Test calls:
val frame = ObdParser.parse("41 0C 1A F8")  // ← 1 argument

// But the actual implementation requires:
fun parse(data: String, sensorRepo: SensorRepository): TelemetryFrame?  // ← 2 arguments
```

These tests **will not compile**. This reveals that the `ObdParser` API changed (when `SensorRepository` was injected) but the tests were never updated, leaving the entire parser with zero working test coverage.

---

#### 🟡 ISSUE-T2 — `DashboardViewModelTest` Missing `StandardTestDispatcher` Injection

**Files:** ViewModel tests  
**Severity:** Medium — **Direct violation of AGENTS.md Rule #4**  

Since `CarRepository` and other dependencies hardcode `Dispatchers.IO` (ISSUE-A5), it is impossible to inject `StandardTestDispatcher` to make coroutine execution deterministic in tests. This is the downstream consequence of the dispatcher violation.

---

#### 🟡 ISSUE-T3 — No User Input Validation Tests

**Severity:** Medium  

The `DashboardRoute` passes `year.toIntOrNull() ?: 2000` directly to `AddCar`. There are no tests for boundary cases (year = 0, year = 9999, empty make/model strings). Business entity validation belongs in the ViewModel or domain layer, not silently defaulted at the UI.

---

### 1.6 Duplicate Theme Definition

#### 🔵 ISSUE-A18 — Dead Theme in `:app` Module

**Files:** [`app/src/main/java/com/jpdgbv/xcan/ui/theme/`](file:///home/pablo/lab/carSync/app/src/main/java/com/jpdgbv/xcan/ui/theme/)  
**Severity:** Low  

The `:app` module contains a full Material theme (`Color.kt`, `Theme.kt`, `Type.kt`) using the default purple/pink Material 3 palette — completely different from the XCan design system defined in `:core:ui`. The actual `XCanTheme` used everywhere is from `core:ui`. The `app` module theme is dead code that could confuse developers.

**Fix:** Delete `app/src/main/java/com/jpdgbv/xcan/ui/theme/` entirely.

---

## Part 2 — Security Audit

### 🔴 SEC-1 — Room Database Is Unencrypted

**File:** [`DatabaseModule.kt`](file:///home/pablo/lab/carSync/core/database/src/main/java/com/jpdgbv/xcan/core/database/di/DatabaseModule.kt)  
**Severity:** Critical  

The Room database stores **vehicle profiles, maintenance logs, full telemetry history including speed, RPM, location-correlated sensor data, and DTC fault codes**. It is built with the standard `Room.databaseBuilder()`, resulting in a plaintext SQLite file at `/data/data/com.pavloglez.xcan/databases/xcan.db`.

On a rooted device, or via ADB backup on a debug build, this file is fully readable.

**Attack vector:** Physical access to a rooted device → `adb pull` of the database → full vehicle diagnostics and driving history exposed.

**Fix:** Integrate SQLCipher via Room's `SupportFactory`:
```kotlin
// build.gradle.kts
implementation("net.zetetic:android-database-sqlcipher:4.5.4")
implementation("androidx.sqlite:sqlite-ktx:2.4.0")

// DatabaseModule.kt
val passphrase = /* retrieve from EncryptedSharedPreferences or Android Keystore */
val factory = SupportFactory(SQLiteDatabase.getBytes(passphrase.toCharArray()))
Room.databaseBuilder(context, XCanDatabase::class.java, "xcan.db")
    .openHelperFactory(factory)
    .build()
```

The encryption key must be stored in the Android Keystore (never in code or `SharedPreferences`).

---

### 🔴 SEC-2 — `android:allowBackup="true"` with No Exclusion Rules

**File:** [`AndroidManifest.xml`](file:///home/pablo/lab/carSync/app/src/main/AndroidManifest.xml), [`data_extraction_rules.xml`](file:///home/pablo/lab/carSync/app/src/main/res/xml/data_extraction_rules.xml)  
**Severity:** Critical  

```xml
<application android:allowBackup="true" ...>
```

`data_extraction_rules.xml` contains only a TODO comment. `backup_rules.xml` is completely empty. This means:

- **ADB backup** (`adb backup com.pavloglez.xcan`) will extract the entire app sandbox, including the Room database, DataStore preferences, and any cached OBD data.
- **Cloud backup (Android Auto Backup)** will upload all this data to the user's Google account with no exclusions.
- On **Android 12+**, `data_extraction_rules.xml` controls cloud backup — a blank file means everything is backed up.

**Attack vector:** An attacker with physical access and USB debugging enabled can extract the full vehicle diagnostic history with a single ADB command.

**Fix:**
```xml
<!-- data_extraction_rules.xml -->
<data-extraction-rules>
    <cloud-backup disableIfNoEncryptionCapabilities="true">
        <exclude domain="database" path="xcan.db" />
        <exclude domain="database" path="xcan.db-shm" />
        <exclude domain="database" path="xcan.db-wal" />
        <exclude domain="sharedpref" path="user_preferences.preferences_pb" />
    </cloud-backup>
    <device-transfer>
        <exclude domain="database" path="." />
    </device-transfer>
</data-extraction-rules>
```

Or if no backup is desired: `android:allowBackup="false"`.

---

### 🔴 SEC-3 — Legacy Bluetooth Permissions Without `maxSdkVersion`

**File:** [`AndroidManifest.xml`](file:///home/pablo/lab/carSync/app/src/main/AndroidManifest.xml)  
**Severity:** Critical  

```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
```

On Android 12+ (API 31+), `BLUETOOTH` and `BLUETOOTH_ADMIN` are deprecated and replaced by `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`, and `BLUETOOTH_ADVERTISE`. However, these legacy permissions are **still granted on API 31+ devices at the `PROTECTION_NORMAL` level**, meaning they're silently auto-granted without user awareness, creating an unnecessarily broad permission footprint.

More critically, `BLUETOOTH_ADMIN` (legacy) grants the ability to **enable/disable the Bluetooth adapter** — a dangerous capability the app does not need.

**Fix:**
```xml
<uses-permission android:name="android.permission.BLUETOOTH" android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" android:maxSdkVersion="30" />
```

---

### 🔴 SEC-4 — OBD Command Injection Vector

**File:** [`BleDataSourceImpl.kt`](file:///home/pablo/lab/carSync/core/bluetooth/src/main/java/com/jpdgbv/xcan/core/bluetooth/internal/BleDataSourceImpl.kt)  
**Severity:** Critical  

PIDs are written directly to the BLE characteristic without sanitization:
```kotlin
peripheral?.write(txCharacteristic, "$pid\r".toByteArray())
```

The `pid` values originate from `userPreferencesRepository.selectedSensors` (DataStore), which stores user-defined string sets. If DataStore were manipulated (e.g., via a backup restore of a tampered preferences file), arbitrary ELM327 AT commands could be injected.

For example, `AT MA` would put the ELM327 into monitor-all mode; `AT WS` would warm-start the device. More dangerously, `AT IGN` reads the ignition line state and other AT commands can disable safety features on some OBD adapters.

**Fix:** Whitelist all valid PIDs with a strict regex before sending:
```kotlin
private val VALID_PID_REGEX = Regex("^[0-9A-Fa-f]{4}$")

fun isValidPid(pid: String): Boolean = VALID_PID_REGEX.matches(pid)

// Before writing:
for (pid in pidsToPoll) {
    if (!isValidPid(pid)) {
        log("Skipping invalid PID: $pid")
        continue
    }
    peripheral?.write(txCharacteristic, "$pid\r".toByteArray())
}
```

---

### 🔴 SEC-5 — MAC Address Logged in Plaintext

**File:** [`BleDataSourceImpl.kt`](file:///home/pablo/lab/carSync/core/bluetooth/src/main/java/com/jpdgbv/xcan/core/bluetooth/internal/BleDataSourceImpl.kt)  
**Severity:** Critical  

```kotlin
log("Establishing connection with adapter $macAddress...")
```

A BLE MAC address is **personally identifiable information (PII)** under GDPR and CCPA — it uniquely identifies a physical device and, by extension, its owner's vehicle and location patterns. This full MAC is written to the `connectionLogs` `SharedFlow`, which is then **displayed in a raw text dialog in the UI** and persisted in `DashboardState.connectionLogs` (a `List<String>` capped at 100 entries in memory).

**Attack vector:** Screenshot of the logs dialog or memory dump of the app process exposes the vehicle's OBD adapter MAC address.

**Fix:** Mask the MAC in all logs:
```kotlin
private fun String.maskMac(): String {
    return replace(Regex("([0-9A-Fa-f]{2}:){4}([0-9A-Fa-f]{2}:)([0-9A-Fa-f]{2})")) { match ->
        "XX:XX:XX:XX:${match.value.takeLast(5)}"
    }
}
log("Establishing connection with adapter ${macAddress.maskMac()}...")
```

---

### 🔴 SEC-6 — `@SuppressLint("MissingPermission")` Is a TOCTOU Vulnerability

**File:** [`BleDataSourceImpl.kt`](file:///home/pablo/lab/carSync/core/bluetooth/src/main/java/com/jpdgbv/xcan/core/bluetooth/internal/BleDataSourceImpl.kt)  
**Severity:** Critical (Security pattern violation)  

```kotlin
@SuppressLint("MissingPermission")
override fun scanForDevices(): Flow<List<ScannedDevice>> { ... }

@SuppressLint("MissingPermission")  
override suspend fun connect(macAddress: String) { ... }
```

The permission check happens in `DashboardRoute` (UI layer) before calling `viewModel.onIntent(DashboardIntent.StartScanning)`. The actual BLE operation happens asynchronously in `BleDataSourceImpl` moments later. Between the check and the operation, the user could revoke the permission (e.g., through Settings). This is a **Time-of-Check-Time-of-Use (TOCTOU)** race condition.

Additionally, `@SuppressLint` at this level means the IDE and linter will never warn about this path again — the safety net is permanently removed.

**Fix:** Perform the permission check at the point of use inside `BleDataSourceImpl`. Inject a `PermissionChecker` interface:
```kotlin
interface PermissionChecker {
    fun hasBluetoothPermissions(): Boolean
}

// In BleDataSourceImpl:
override fun scanForDevices(): Flow<List<ScannedDevice>> {
    if (!permissionChecker.hasBluetoothPermissions()) {
        return flow { throw SecurityException("Bluetooth permissions not granted") }
    }
    ...
}
```

---

### 🟠 SEC-7 — No Network Security Configuration / No Certificate Pinning

**File:** [`NetworkModule.kt`](file:///home/pablo/lab/carSync/core/network/src/main/java/com/jpdgbv/xcan/core/network/di/NetworkModule.kt)  
**Severity:** High  

The Retrofit client is configured without a `NetworkSecurityConfig`. While all modern Android apps default to HTTPS-only (cleartext disabled by default on API 28+), the app does not:

1. Define a `network_security_config.xml` to explicitly enforce this
2. Implement certificate pinning for the `XCanApiService` backend
3. Configure a custom `OkHttpClient` with a certificate pinner

**Attack vector:** MITM attack on the maintenance sync endpoint could serve malicious `MaintenanceLogDto` data that gets persisted to the local database.

**Fix:**
```xml
<!-- res/xml/network_security_config.xml -->
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">api.xcan.example.com</domain>
        <pin-set expiration="2027-01-01">
            <pin digest="SHA-256">your-server-cert-public-key-hash=</pin>
            <pin digest="SHA-256">backup-pin-hash=</pin>
        </pin-set>
    </domain-config>
</network-security-config>
```

```xml
<!-- AndroidManifest.xml -->
<application android:networkSecurityConfig="@xml/network_security_config" ...>
```

---

### 🟠 SEC-8 — Broad Exception Swallowing Hides Security-Relevant Failures

**Files:** Multiple — `BleDataSourceImpl.kt`, `MaintenanceRepository.kt`, `ObdParser.kt`  
**Severity:** High  

```kotlin
} catch (e: Exception) {
    return null // Parsing error, ignore
}

} catch (e: Exception) {
    // Ignore network errors, rely on local cache
}

} catch (e: Exception) {
    e.printStackTrace()  // ← goes to logcat, invisible in production
}
```

Catching `Exception` wholesale and ignoring it (or only logging to `System.err`) means:
- A `SecurityException` from BLE permission revocation is silently swallowed
- A `SSLHandshakeException` during sync is silently ignored (MITM could cause this)
- A `NumberFormatException` from a crafted BLE response is swallowed, hiding injection attempts

`e.printStackTrace()` outputs to `logcat`, which is readable by **any app on the device** on Android ≤ 4.1 and by the shell user on newer versions.

**Fix:** Use structured error handling with typed results:
```kotlin
sealed interface BleResult<out T> {
    data class Success<T>(val value: T) : BleResult<T>
    data class Error(val cause: Throwable) : BleResult<Nothing>
}
```

Log errors through a proper logging framework (e.g., Timber) with production log levels that suppress debug output in release builds. Never use `e.printStackTrace()` in production code.

---

### 🟠 SEC-9 — `DtcParser` Vulnerable to Malformed BLE Data (Parsing Exception Risk)

**File:** [`DtcParser.kt`](file:///home/pablo/lab/carSync/core/bluetooth/src/main/java/com/jpdgbv/xcan/core/bluetooth/DtcParser.kt)  
**Severity:** High  

```kotlin
val a = hex.substring(0, 2).toInt(16)  // ← throws NumberFormatException if malformed
val b = hex.substring(2, 4).toInt(16)  // ← same
```

A rogue BLE device (e.g., a BLE device masquerading as an OBD adapter) could send crafted responses containing non-hex characters that survive the `replace(Regex("[^0-9A-F]"), "")` step (which is applied to the outer string but not re-validated on substrings after `indexOf`). If a multi-line `0:` format response is received, the frame number index bytes can inject non-hex characters.

**Attack vector:** A malicious BLE peripheral (Evil OBD adapter attack) sends crafted responses to crash the parser or cause unexpected state transitions.

**Fix:** Wrap `toInt(16)` calls with `runCatching` and validate each parsed substring independently. Also add a max-response-length guard:
```kotlin
if (hexResponse.length > MAX_OBD_RESPONSE_LENGTH) return emptyList()
```

---

### 🟡 SEC-10 — `ACCESS_COARSE_LOCATION` Is Unnecessary and Expands Attack Surface

**File:** [`AndroidManifest.xml`](file:///home/pablo/lab/carSync/app/src/main/AndroidManifest.xml)  
**Severity:** Medium  

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

When `ACCESS_FINE_LOCATION` is granted, `ACCESS_COARSE_LOCATION` is automatically included. Declaring both is redundant but — more importantly — increases the permission surface shown to users and to security scanners. Remove `ACCESS_COARSE_LOCATION`. Also, since the app targets API 33+ (minSdk=33), and uses `BLUETOOTH_SCAN` with `neverForLocation`, location permissions should not be needed for BLE at all on the target SDK.

**Fix:** Remove both location permissions from the manifest (they are only listed for pre-API 31 fallback in code, but minSdk=33 makes that code dead). If BLE scanning *still* requires location for some edge case, keep only `ACCESS_FINE_LOCATION`.

---

### 🟡 SEC-11 — BLE Infinite Reconnect Loop (Denial of Service / Battery Drain)

**File:** [`BleDataSourceImpl.kt`](file:///home/pablo/lab/carSync/core/bluetooth/src/main/java/com/jpdgbv/xcan/core/bluetooth/internal/BleDataSourceImpl.kt)  
**Severity:** Medium  

```kotlin
while (true) {
    try {
        peripheral?.connect()
        ...
    } catch (e: Exception) {
        log("Connection failed: ${e.message}. Retrying in ${retryCount}s...")
    }
    retryCount++
    delay(minOf(retryCount * 1000L, 5000L))
}
```

The reconnect loop runs forever with a maximum 5-second backoff. A malicious BLE device that repeatedly accepts-then-drops connections will hold the app in a permanent retry loop, draining the battery. There is also no maximum retry count.

**Fix:** Implement exponential backoff with jitter and a maximum retry count:
```kotlin
val maxRetries = 10
val baseDelay = 1000L
val maxDelay = 30000L

for (attempt in 1..maxRetries) {
    try {
        peripheral?.connect()
        break
    } catch (e: Exception) { ... }
    val jitter = Random.nextLong(0, 500)
    val backoff = minOf(baseDelay * (2.0.pow(attempt)).toLong(), maxDelay) + jitter
    delay(backoff)
}
_connectionState.value = ConnectionStatus.DISCONNECTED // Give up after max retries
```

---

### 🟡 SEC-12 — No Input Validation on Vehicle Fields

**File:** [`DashboardScreen.kt`](file:///home/pablo/lab/carSync/feature/dashboard/src/main/java/com/jpdgbv/xcan/feature/dashboard/DashboardScreen.kt)  
**Severity:** Medium  

```kotlin
Button(onClick = {
    viewModel.onIntent(DashboardIntent.AddCar(make, model, year.toIntOrNull() ?: 2000))
    // ← make and model can be empty strings, year can be 9999 or -1
})
```

There is no validation on `make`, `model`, or `year` before they are persisted to Room. Empty strings, excessively long strings (potential DB performance issue), or years outside valid automotive ranges (1886–current+1) are all accepted silently.

**Fix:** Add validation in the ViewModel (not the UI):
```kotlin
is DashboardIntent.AddCar -> {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    if (intent.make.isBlank() || intent.model.isBlank()) {
        _effect.send(DashboardEffect.ShowError("Make and model cannot be empty"))
        return
    }
    if (intent.year !in 1886..currentYear + 1) {
        _effect.send(DashboardEffect.ShowError("Invalid vehicle year"))
        return
    }
    viewModelScope.launch { ... }
}
```

---

## Summary Table

| ID | Area | Finding | Severity |
|---|---|---|---|
| A4 | Architecture | `ObdParser` is a stateful global singleton with non-thread-safe mutable map | 🔴 Critical |
| A5 | Architecture | Hardcoded `Dispatchers.IO` in all repositories (violates AGENTS.md rule) | 🔴 Critical |
| T1 | Testing | `ObdParserTest` won't compile — wrong method signature, zero parser test coverage | 🔴 Critical |
| SEC-1 | Security | Room database is unencrypted (vehicle diagnostics in plaintext SQLite) | 🔴 Critical |
| SEC-2 | Security | `allowBackup=true` with no exclusion rules — full DB extractable via ADB | 🔴 Critical |
| SEC-3 | Security | Legacy `BLUETOOTH_ADMIN` permission grants adapter control without `maxSdkVersion` | 🔴 Critical |
| SEC-4 | Security | OBD command injection via unvalidated PID strings from DataStore | 🔴 Critical |
| SEC-5 | Security | MAC address (PII) logged and displayed in plaintext UI | 🔴 Critical |
| SEC-6 | Security | TOCTOU race on Bluetooth permissions — `@SuppressLint` removes all safety nets | 🔴 Critical |
| A1 | Architecture | No MVI Effects pattern — one-time events leak into State | 🟠 High |
| A6 | Architecture | `DashboardViewModel` is a God Object (5 concerns, 4 injections, 11-field state) | 🟠 High |
| A7 | Architecture | `BleDataSourceImpl` manages its own unmanaged `CoroutineScope` | 🟠 High |
| A11 | Compose | Public Composables missing `modifier: Modifier = Modifier` (violates AGENTS.md) | 🟠 High |
| SEC-7 | Security | No `NetworkSecurityConfig`, no certificate pinning on maintenance sync endpoint | 🟠 High |
| SEC-8 | Security | Broad `catch (Exception)` swallowing hides security failures, `e.printStackTrace()` in production | 🟠 High |
| SEC-9 | Security | `DtcParser` vulnerable to malformed BLE data → `NumberFormatException` from rogue device | 🟠 High |
| A2 | Architecture | `ConfigState.overrideProtocol` is dead state — never persisted or used | 🟡 Medium |
| A3 | Architecture | Fragile 11-flow array `combine` with unchecked runtime casts | 🟡 Medium |
| A8 | Architecture | `TelemetryRepository` missing `@Singleton` annotation | 🟡 Medium |
| A9 | Architecture | `SensorRepositoryImpl.customSensors` is non-thread-safe `mutableListOf` | 🟡 Medium |
| A10 | Architecture | `evaluateFormula` uses hardcoded `when` strings, violates OCP | 🟡 Medium |
| A12 | Compose | 5 boolean dialog flags — no mutual exclusion, should be sealed class | 🟡 Medium |
| A13 | Navigation | String-based navigation routes — type-unsafe, typos fail silently at runtime | 🟡 Medium |
| A14 | Architecture | `ScanSensors` intent triggered twice on screen composition + connect | 🟡 Medium |
| T2 | Testing | `StandardTestDispatcher` injection impossible due to hardcoded dispatchers | 🟡 Medium |
| T3 | Testing | No validation tests for vehicle fields | 🟡 Medium |
| SEC-10 | Security | Redundant `ACCESS_COARSE_LOCATION` unnecessarily expands permission surface | 🟡 Medium |
| SEC-11 | Security | Infinite reconnect loop — susceptible to battery DoS from malicious BLE peripheral | 🟡 Medium |
| SEC-12 | Security | No server-side input validation on vehicle make/model/year before Room insert | 🟡 Medium |
| A15 | Build | `minSdk` inconsistency: app=33, libraries=26 | 🔵 Low |
| A16 | Build | `fix2.py` debugging script committed to repository | 🔵 Low |
| A17 | Build | Inline version strings in feature Gradle files bypass version catalog | 🔵 Low |
| A18 | Architecture | Dead duplicate theme in `:app` module contradicts `core:ui` design system | 🔵 Low |

---

## Recommended Fix Priority

### Immediate (Before Any Beta Release)
1. **SEC-1** — Encrypt Room database with SQLCipher + Android Keystore
2. **SEC-2** — Configure `data_extraction_rules.xml` to exclude all sensitive data
3. **SEC-4** — Add PID whitelist validation before every BLE write
4. **SEC-3** — Add `maxSdkVersion="30"` to legacy Bluetooth permissions
5. **SEC-5** — Mask MAC addresses in all log output
6. **SEC-6** — Move permission checks into `BleDataSourceImpl`, remove `@SuppressLint`
7. **A4** — Refactor `ObdParser` from `object` to `class`, move state to `BleDataSourceImpl`
8. **T1** — Fix `ObdParserTest` to match current API and restore parser test coverage

### Short-Term (Next Sprint)
9. **A5** — Inject `CoroutineDispatcher` throughout the data layer
10. **A6** — Split `DashboardViewModel` into focused ViewModels
11. **A1** — Implement `Effect` sealed interface in all ViewModels
12. **SEC-7** — Add `network_security_config.xml` with certificate pinning
13. **SEC-8** — Replace `catch (Exception)` with typed error results and Timber logging
14. **A11** — Add `modifier: Modifier = Modifier` to all public Composables

### Medium-Term (Backlog)
15. **A3** — Refactor 11-flow `combine` into staged combinations
16. **A13** — Migrate to type-safe Compose Navigation
17. **A9** — Add `Mutex` to `SensorRepositoryImpl`
18. **SEC-9** — Harden `DtcParser` against malformed BLE responses
19. **SEC-11** — Implement capped exponential backoff with jitter
20. All remaining Low severity items
