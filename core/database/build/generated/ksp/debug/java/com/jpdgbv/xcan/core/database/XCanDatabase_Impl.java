package com.jpdgbv.xcan.core.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.jpdgbv.xcan.core.database.dao.CarProfileDao;
import com.jpdgbv.xcan.core.database.dao.CarProfileDao_Impl;
import com.jpdgbv.xcan.core.database.dao.MaintenanceDao;
import com.jpdgbv.xcan.core.database.dao.MaintenanceDao_Impl;
import com.jpdgbv.xcan.core.database.dao.TelemetryDao;
import com.jpdgbv.xcan.core.database.dao.TelemetryDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class XCanDatabase_Impl extends XCanDatabase {
  private volatile CarProfileDao _carProfileDao;

  private volatile MaintenanceDao _maintenanceDao;

  private volatile TelemetryDao _telemetryDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(3) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `maintenance_logs` (`id` TEXT NOT NULL, `carId` TEXT NOT NULL, `serviceType` TEXT NOT NULL, `dateMs` INTEGER NOT NULL, `mileage` INTEGER NOT NULL, `cost` REAL NOT NULL, `notes` TEXT NOT NULL, `relatedDtc` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`carId`) REFERENCES `car_profiles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_maintenance_logs_carId` ON `maintenance_logs` (`carId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `telemetry_frames` (`id` TEXT NOT NULL, `timestampMs` INTEGER NOT NULL, `sensors` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `car_profiles` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `make` TEXT NOT NULL, `model` TEXT NOT NULL, `year` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'b0095eaa762019d475ceb8adcfdd4187')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `maintenance_logs`");
        db.execSQL("DROP TABLE IF EXISTS `telemetry_frames`");
        db.execSQL("DROP TABLE IF EXISTS `car_profiles`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsMaintenanceLogs = new HashMap<String, TableInfo.Column>(8);
        _columnsMaintenanceLogs.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMaintenanceLogs.put("carId", new TableInfo.Column("carId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMaintenanceLogs.put("serviceType", new TableInfo.Column("serviceType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMaintenanceLogs.put("dateMs", new TableInfo.Column("dateMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMaintenanceLogs.put("mileage", new TableInfo.Column("mileage", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMaintenanceLogs.put("cost", new TableInfo.Column("cost", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMaintenanceLogs.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMaintenanceLogs.put("relatedDtc", new TableInfo.Column("relatedDtc", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMaintenanceLogs = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysMaintenanceLogs.add(new TableInfo.ForeignKey("car_profiles", "CASCADE", "NO ACTION", Arrays.asList("carId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesMaintenanceLogs = new HashSet<TableInfo.Index>(1);
        _indicesMaintenanceLogs.add(new TableInfo.Index("index_maintenance_logs_carId", false, Arrays.asList("carId"), Arrays.asList("ASC")));
        final TableInfo _infoMaintenanceLogs = new TableInfo("maintenance_logs", _columnsMaintenanceLogs, _foreignKeysMaintenanceLogs, _indicesMaintenanceLogs);
        final TableInfo _existingMaintenanceLogs = TableInfo.read(db, "maintenance_logs");
        if (!_infoMaintenanceLogs.equals(_existingMaintenanceLogs)) {
          return new RoomOpenHelper.ValidationResult(false, "maintenance_logs(com.jpdgbv.xcan.core.database.entity.MaintenanceLogEntity).\n"
                  + " Expected:\n" + _infoMaintenanceLogs + "\n"
                  + " Found:\n" + _existingMaintenanceLogs);
        }
        final HashMap<String, TableInfo.Column> _columnsTelemetryFrames = new HashMap<String, TableInfo.Column>(3);
        _columnsTelemetryFrames.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTelemetryFrames.put("timestampMs", new TableInfo.Column("timestampMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTelemetryFrames.put("sensors", new TableInfo.Column("sensors", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTelemetryFrames = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTelemetryFrames = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTelemetryFrames = new TableInfo("telemetry_frames", _columnsTelemetryFrames, _foreignKeysTelemetryFrames, _indicesTelemetryFrames);
        final TableInfo _existingTelemetryFrames = TableInfo.read(db, "telemetry_frames");
        if (!_infoTelemetryFrames.equals(_existingTelemetryFrames)) {
          return new RoomOpenHelper.ValidationResult(false, "telemetry_frames(com.jpdgbv.xcan.core.database.entity.TelemetryFrameEntity).\n"
                  + " Expected:\n" + _infoTelemetryFrames + "\n"
                  + " Found:\n" + _existingTelemetryFrames);
        }
        final HashMap<String, TableInfo.Column> _columnsCarProfiles = new HashMap<String, TableInfo.Column>(6);
        _columnsCarProfiles.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCarProfiles.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCarProfiles.put("make", new TableInfo.Column("make", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCarProfiles.put("model", new TableInfo.Column("model", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCarProfiles.put("year", new TableInfo.Column("year", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCarProfiles.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCarProfiles = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCarProfiles = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCarProfiles = new TableInfo("car_profiles", _columnsCarProfiles, _foreignKeysCarProfiles, _indicesCarProfiles);
        final TableInfo _existingCarProfiles = TableInfo.read(db, "car_profiles");
        if (!_infoCarProfiles.equals(_existingCarProfiles)) {
          return new RoomOpenHelper.ValidationResult(false, "car_profiles(com.jpdgbv.xcan.core.database.entity.CarProfileEntity).\n"
                  + " Expected:\n" + _infoCarProfiles + "\n"
                  + " Found:\n" + _existingCarProfiles);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "b0095eaa762019d475ceb8adcfdd4187", "5e54d7d1e7dd0acc649cb667ff6c1b62");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "maintenance_logs","telemetry_frames","car_profiles");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `maintenance_logs`");
      _db.execSQL("DELETE FROM `telemetry_frames`");
      _db.execSQL("DELETE FROM `car_profiles`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(CarProfileDao.class, CarProfileDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(MaintenanceDao.class, MaintenanceDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TelemetryDao.class, TelemetryDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public CarProfileDao carProfileDao() {
    if (_carProfileDao != null) {
      return _carProfileDao;
    } else {
      synchronized(this) {
        if(_carProfileDao == null) {
          _carProfileDao = new CarProfileDao_Impl(this);
        }
        return _carProfileDao;
      }
    }
  }

  @Override
  public MaintenanceDao maintenanceDao() {
    if (_maintenanceDao != null) {
      return _maintenanceDao;
    } else {
      synchronized(this) {
        if(_maintenanceDao == null) {
          _maintenanceDao = new MaintenanceDao_Impl(this);
        }
        return _maintenanceDao;
      }
    }
  }

  @Override
  public TelemetryDao telemetryDao() {
    if (_telemetryDao != null) {
      return _telemetryDao;
    } else {
      synchronized(this) {
        if(_telemetryDao == null) {
          _telemetryDao = new TelemetryDao_Impl(this);
        }
        return _telemetryDao;
      }
    }
  }
}
