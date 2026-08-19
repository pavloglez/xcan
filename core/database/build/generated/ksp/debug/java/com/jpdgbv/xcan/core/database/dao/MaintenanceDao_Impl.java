package com.jpdgbv.xcan.core.database.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.jpdgbv.xcan.core.database.entity.MaintenanceLogEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MaintenanceDao_Impl implements MaintenanceDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MaintenanceLogEntity> __insertionAdapterOfMaintenanceLogEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteLog;

  public MaintenanceDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMaintenanceLogEntity = new EntityInsertionAdapter<MaintenanceLogEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `maintenance_logs` (`id`,`carId`,`serviceType`,`dateMs`,`mileage`,`cost`,`notes`,`relatedDtc`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MaintenanceLogEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getCarId());
        statement.bindString(3, entity.getServiceType());
        statement.bindLong(4, entity.getDateMs());
        statement.bindLong(5, entity.getMileage());
        statement.bindDouble(6, entity.getCost());
        statement.bindString(7, entity.getNotes());
        if (entity.getRelatedDtc() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getRelatedDtc());
        }
      }
    };
    this.__preparedStmtOfDeleteLog = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM maintenance_logs WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public long insertLog(final MaintenanceLogEntity log) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfMaintenanceLogEntity.insertAndReturnId(log);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<Long> insertLogs(final List<MaintenanceLogEntity> logs) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final List<Long> _result = __insertionAdapterOfMaintenanceLogEntity.insertAndReturnIdsList(logs);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public int deleteLog(final String id) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteLog.acquire();
    int _argIndex = 1;
    _stmt.bindString(_argIndex, id);
    try {
      __db.beginTransaction();
      try {
        final int _result = _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
        return _result;
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteLog.release(_stmt);
    }
  }

  @Override
  public Flow<List<MaintenanceLogEntity>> getAllLogs(final String carId) {
    final String _sql = "SELECT * FROM maintenance_logs WHERE carId = ? ORDER BY dateMs DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, carId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"maintenance_logs"}, new Callable<List<MaintenanceLogEntity>>() {
      @Override
      @NonNull
      public List<MaintenanceLogEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCarId = CursorUtil.getColumnIndexOrThrow(_cursor, "carId");
          final int _cursorIndexOfServiceType = CursorUtil.getColumnIndexOrThrow(_cursor, "serviceType");
          final int _cursorIndexOfDateMs = CursorUtil.getColumnIndexOrThrow(_cursor, "dateMs");
          final int _cursorIndexOfMileage = CursorUtil.getColumnIndexOrThrow(_cursor, "mileage");
          final int _cursorIndexOfCost = CursorUtil.getColumnIndexOrThrow(_cursor, "cost");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfRelatedDtc = CursorUtil.getColumnIndexOrThrow(_cursor, "relatedDtc");
          final List<MaintenanceLogEntity> _result = new ArrayList<MaintenanceLogEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MaintenanceLogEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpCarId;
            _tmpCarId = _cursor.getString(_cursorIndexOfCarId);
            final String _tmpServiceType;
            _tmpServiceType = _cursor.getString(_cursorIndexOfServiceType);
            final long _tmpDateMs;
            _tmpDateMs = _cursor.getLong(_cursorIndexOfDateMs);
            final int _tmpMileage;
            _tmpMileage = _cursor.getInt(_cursorIndexOfMileage);
            final double _tmpCost;
            _tmpCost = _cursor.getDouble(_cursorIndexOfCost);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpRelatedDtc;
            if (_cursor.isNull(_cursorIndexOfRelatedDtc)) {
              _tmpRelatedDtc = null;
            } else {
              _tmpRelatedDtc = _cursor.getString(_cursorIndexOfRelatedDtc);
            }
            _item = new MaintenanceLogEntity(_tmpId,_tmpCarId,_tmpServiceType,_tmpDateMs,_tmpMileage,_tmpCost,_tmpNotes,_tmpRelatedDtc);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
