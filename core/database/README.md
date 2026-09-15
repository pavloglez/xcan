# Database Core Module (`:core:database`)

The `:core:database` module provides hardware-encrypted, offline-first local persistence using Android Jetpack Room backed by SQLCipher.

## Architecture Role
- **Room Database (`XCanDatabase`)**: Central schema definition (Version 4) managing all local relational tables.
- **Entities**:
  - `CarProfileEntity`: Stored vehicles (make, model, year, VIN, active selection).
  - `MaintenanceLogEntity`: Maintenance service events (service type, mileage, date, associated DTCs).
  - `TelemetryFrameEntity`: Raw and calculated OBD-II telemetry snapshots (speed, RPM, coolant, intake, MAF).
  - `LogSessionEntity`: Telemetry recording sessions (session UUID, label, start/end timestamps, entry count).
  - `LogEntryEntity`: High-frequency sensor samples captured during an active logging session.
- **Data Access Objects (DAOs)**:
  - `CarProfileDao`: Vehicle profile CRUD and reactive active vehicle observation.
  - `MaintenanceDao`: Service log insertion, queries, and vehicle history filtering.
  - `TelemetryDao`: Telemetry frame batch insertion and historical retrieval.
  - `LogSessionDao`: Session management and cascading deletions.
  - `LogEntryDao`: High-throughput time-series sensor entry batch insertions and querying by `sessionId`.

## Security Architecture (SQLCipher Encryption)
All data at rest is protected with AES-256 encryption via SQLCipher:
- **Passphrase Generation**: A cryptographically secure 256-bit (32-byte) key is generated using `SecureRandom`.
- **Hardware-Backed Key Storage**: The database passphrase is stored in `EncryptedSharedPreferences` protected by Android Keystore via AndroidX Security `MasterKey` (`KeyScheme.AES256_GCM`).
- **Open Helper Factory**: Room is initialized with `SupportOpenHelperFactory(passphrase)` from `net.zetetic:sqlcipher-android`, transparently encrypting all database pages, transaction journals, and SQLite metadata.

## Dependencies
- `:core:model`

