package com.phantomnet.core.database.dao;

import androidx.annotation.NonNull;
import androidx.room.EntityInsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.coroutines.FlowUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import com.phantomnet.core.database.entity.CallLogEntity;
import java.lang.Class;
import java.lang.NullPointerException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@SuppressWarnings({"unchecked", "deprecation", "removal"})
public final class CallLogDao_Impl implements CallLogDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<CallLogEntity> __insertAdapterOfCallLogEntity;

  public CallLogDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfCallLogEntity = new EntityInsertAdapter<CallLogEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `call_logs` (`sessionId`,`conversationId`,`direction`,`durationSec`,`timestamp`,`privacyMode`,`outcome`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final CallLogEntity entity) {
        if (entity.getSessionId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindText(1, entity.getSessionId());
        }
        if (entity.getConversationId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getConversationId());
        }
        if (entity.getDirection() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getDirection());
        }
        statement.bindLong(4, entity.getDurationSec());
        statement.bindLong(5, entity.getTimestamp());
        if (entity.getPrivacyMode() == null) {
          statement.bindNull(6);
        } else {
          statement.bindText(6, entity.getPrivacyMode());
        }
        if (entity.getOutcome() == null) {
          statement.bindNull(7);
        } else {
          statement.bindText(7, entity.getOutcome());
        }
      }
    };
  }

  @Override
  public Object insertCallLog(final CallLogEntity log,
      final Continuation<? super Unit> $completion) {
    if (log == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __insertAdapterOfCallLogEntity.insert(_connection, log);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Flow<List<CallLogEntity>> getLogsForConversation(final String convId) {
    final String _sql = "SELECT * FROM call_logs WHERE conversationId = ? ORDER BY timestamp DESC";
    return FlowUtil.createFlow(__db, false, new String[] {"call_logs"}, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (convId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, convId);
        }
        final int _columnIndexOfSessionId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "sessionId");
        final int _columnIndexOfConversationId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "conversationId");
        final int _columnIndexOfDirection = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "direction");
        final int _columnIndexOfDurationSec = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "durationSec");
        final int _columnIndexOfTimestamp = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "timestamp");
        final int _columnIndexOfPrivacyMode = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "privacyMode");
        final int _columnIndexOfOutcome = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "outcome");
        final List<CallLogEntity> _result = new ArrayList<CallLogEntity>();
        while (_stmt.step()) {
          final CallLogEntity _item;
          final String _tmpSessionId;
          if (_stmt.isNull(_columnIndexOfSessionId)) {
            _tmpSessionId = null;
          } else {
            _tmpSessionId = _stmt.getText(_columnIndexOfSessionId);
          }
          final String _tmpConversationId;
          if (_stmt.isNull(_columnIndexOfConversationId)) {
            _tmpConversationId = null;
          } else {
            _tmpConversationId = _stmt.getText(_columnIndexOfConversationId);
          }
          final String _tmpDirection;
          if (_stmt.isNull(_columnIndexOfDirection)) {
            _tmpDirection = null;
          } else {
            _tmpDirection = _stmt.getText(_columnIndexOfDirection);
          }
          final int _tmpDurationSec;
          _tmpDurationSec = (int) (_stmt.getLong(_columnIndexOfDurationSec));
          final long _tmpTimestamp;
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp);
          final String _tmpPrivacyMode;
          if (_stmt.isNull(_columnIndexOfPrivacyMode)) {
            _tmpPrivacyMode = null;
          } else {
            _tmpPrivacyMode = _stmt.getText(_columnIndexOfPrivacyMode);
          }
          final String _tmpOutcome;
          if (_stmt.isNull(_columnIndexOfOutcome)) {
            _tmpOutcome = null;
          } else {
            _tmpOutcome = _stmt.getText(_columnIndexOfOutcome);
          }
          _item = new CallLogEntity(_tmpSessionId,_tmpConversationId,_tmpDirection,_tmpDurationSec,_tmpTimestamp,_tmpPrivacyMode,_tmpOutcome);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public Object updateCallEnd(final String sessionId, final int durationSec, final String outcome,
      final Continuation<? super Unit> $completion) {
    final String _sql = "UPDATE call_logs SET durationSec = ?, outcome = ? WHERE sessionId = ?";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, durationSec);
        _argIndex = 2;
        if (outcome == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, outcome);
        }
        _argIndex = 3;
        if (sessionId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, sessionId);
        }
        _stmt.step();
        return Unit.INSTANCE;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    final String _sql = "DELETE FROM call_logs";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        _stmt.step();
        return Unit.INSTANCE;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
