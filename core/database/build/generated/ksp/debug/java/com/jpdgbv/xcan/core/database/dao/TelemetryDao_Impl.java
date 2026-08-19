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
import com.jpdgbv.xcan.core.database.Converters;
import com.jpdgbv.xcan.core.database.entity.TelemetryFrameEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Float;
import java.lang.IllegalStateException;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TelemetryDao_Impl implements TelemetryDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TelemetryFrameEntity> __insertionAdapterOfTelemetryFrameEntity;

  private final Converters __converters = new Converters();

  private final SharedSQLiteStatement __preparedStmtOfDeleteOldTelemetry;

  public TelemetryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTelemetryFrameEntity = new EntityInsertionAdapter<TelemetryFrameEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `telemetry_frames` (`id`,`timestampMs`,`sensors`) VALUES (?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TelemetryFrameEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindLong(2, entity.getTimestampMs());
        final String _tmp = __converters.fromStringMap(entity.getSensors());
        if (_tmp == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, _tmp);
        }
      }
    };
    this.__preparedStmtOfDeleteOldTelemetry = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM telemetry_frames WHERE timestampMs < ?";
        return _query;
      }
    };
  }

  @Override
  public long insertTelemetry(final TelemetryFrameEntity frame) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfTelemetryFrameEntity.insertAndReturnId(frame);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public int deleteOldTelemetry(final long olderThanMs) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteOldTelemetry.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, olderThanMs);
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
      __preparedStmtOfDeleteOldTelemetry.release(_stmt);
    }
  }

  @Override
  public Flow<List<TelemetryFrameEntity>> getRecentTelemetry(final int limit) {
    final String _sql = "SELECT * FROM telemetry_frames ORDER BY timestampMs DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"telemetry_frames"}, new Callable<List<TelemetryFrameEntity>>() {
      @Override
      @NonNull
      public List<TelemetryFrameEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestampMs = CursorUtil.getColumnIndexOrThrow(_cursor, "timestampMs");
          final int _cursorIndexOfSensors = CursorUtil.getColumnIndexOrThrow(_cursor, "sensors");
          final List<TelemetryFrameEntity> _result = new ArrayList<TelemetryFrameEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TelemetryFrameEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final long _tmpTimestampMs;
            _tmpTimestampMs = _cursor.getLong(_cursorIndexOfTimestampMs);
            final Map<String, Float> _tmpSensors;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfSensors)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfSensors);
            }
            final Map<String, Float> _tmp_1 = __converters.toStringMap(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.util.Map<java.lang.String, java.lang.Float>', but it was NULL.");
            } else {
              _tmpSensors = _tmp_1;
            }
            _item = new TelemetryFrameEntity(_tmpId,_tmpTimestampMs,_tmpSensors);
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
