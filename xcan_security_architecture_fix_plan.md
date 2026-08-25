# XCan Security & Architecture Fix Plan

Based on the `xcan_architecture_security_audit.md` document, I have verified that the majority of the issues are still present in the current codebase (with the exception of `A18` which we resolved in a previous UI refactoring step). 

Here is the proposed step-by-step plan to systematically fix these vulnerabilities and architectural flaws.

## Phase 1: Critical Security Hardening (Immediate)
**Goal:** Prevent data leaks, secure network/BLE communication, and fix critical DoS vulnerabilities.

1. **Database Encryption (SEC-1):** 
   - Add `net.zetetic:android-database-sqlcipher` and `androidx.security:security-crypto` to `libs.versions.toml` and `core:database`.
   - Update `DatabaseModule.kt` to generate an Android Keystore key and pass a `SupportFactory` to the Room database builder.
2. **Manifest & Data Extraction (SEC-2, SEC-3, SEC-10):**
   - Update `app/src/main/AndroidManifest.xml` to set `android:allowBackup="false"` (or configure `data_extraction_rules.xml` to exclude the database).
   - Add `android:maxSdkVersion="30"` to legacy `BLUETOOTH` and `BLUETOOTH_ADMIN` permissions.
   - Clean up unnecessary location permissions if minSdk is 33+.
3. **Network Security (SEC-7):**
   - Create `network_security_config.xml` to enforce certificate pinning for the sync endpoint.
   - Reference it in the `AndroidManifest.xml` via `android:networkSecurityConfig`.
4. **BLE Hardening & Privacy (SEC-4, SEC-5, SEC-6, SEC-9, SEC-11):**
   - **SEC-4:** Add a strict regex/whitelist for PIDs in `BleDataSourceImpl` before writing to the BLE characteristic.
   - **SEC-5:** Create an extension function to mask MAC addresses (e.g., `XX:XX:XX:XX:12:34`) before logging them in `BleDataSourceImpl`.
   - **SEC-6:** Refactor permission checks in `BleDataSourceImpl` to ensure TOCTOU (Time-of-check to time-of-use) safety and remove broad `@SuppressLint` tags.
   - **SEC-9:** Wrap `DtcParser` logic in safe `try/catch` blocks targeting `NumberFormatException`, returning a designated error state instead of crashing.
   - **SEC-11:** Implement an exponential backoff with jitter and a maximum retry limit for the BLE connection loop.
5. **Input Validation (SEC-12):**
   - Add strict boundary checks for `make`, `model`, and `year` inside `DashboardViewModel` or `CarRepository` before creating `DashboardIntent.AddCar`.

## Phase 2: Core Architecture & Concurrency
**Goal:** Enforce MVI purity, eliminate global mutable state, and fix threading violations.

1. **Fix Dispatcher Injection (A5 & T2):**
   - Create a `DispatcherProvider` interface (with `io`, `main`, `default`) in `core:model` or `core:data`.
   - Provide it via Hilt (`DispatchersModule.kt`).
   - Replace all hardcoded `withContext(Dispatchers.IO)` in repositories (`TelemetryRepository`, `LoggingRepository`, etc.) with the injected dispatcher.
2. **Remove Global Stateful Singletons (A4):**
   - Refactor `ObdParser` from an `object` to a `class` injected via Hilt.
   - Move the mutable parsing state into the scope of `BleDataSourceImpl` or a dedicated session manager.
3. **Fix Thread-Safety (A9):**
   - Use `Mutex` to protect the `customSensors` mutable list inside `SensorRepositoryImpl`, or migrate it entirely to a Room-backed table.
4. **ViewModel Refactoring (A1 & A6):**
   - **A1:** Introduce a `sealed interface Effect` to all ViewModels (starting with `DashboardViewModel`) and use a `Channel` to emit one-off events (like toasts/errors) instead of leaking them into State.
   - **A6:** Split the God Object `DashboardViewModel` into smaller, focused ViewModels if feasible, or delegate logic to Domain UseCases.

## Phase 3: Cleanup & Polish
**Goal:** Address lower severity issues and improve maintainability.

1. **Navigation (A13):** Migrate away from String-based routes to Compose Type-Safe Navigation using kotlinx.serialization objects.
2. **Build Scripts (A15, A17, A16):** 
   - Standardize `minSdk` to 33 across all `build.gradle.kts` files.
   - Use `libs.versions.toml` exclusively for all dependencies.
   - Delete stray debugging scripts like `fix2.py`.
3. **Restore Tests (T1, T3):** Update `ObdParserTest` and UI tests to compile with the newly refactored classes and injected `StandardTestDispatcher`.
