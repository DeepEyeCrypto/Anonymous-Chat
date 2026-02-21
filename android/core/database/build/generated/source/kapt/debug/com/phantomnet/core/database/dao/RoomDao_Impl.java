package com.phantomnet.core.database.dao;

import androidx.annotation.NonNull;
import androidx.room.EntityInsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.coroutines.FlowUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import com.phantomnet.core.database.entity.RoomEntity;
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
public final class RoomDao_Impl implements RoomDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<RoomEntity> __insertAdapterOfRoomEntity;

  public RoomDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfRoomEntity = new EntityInsertAdapter<RoomEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `rooms` (`id`,`name`,`type`,`sharedSecretsJson`,`lastMessagePreview`,`lastMessageTimestamp`,`unreadCount`,`isActive`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final RoomEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindText(1, entity.getId());
        }
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getName());
        }
        if (entity.getType() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getType());
        }
        if (entity.getSharedSecretsJson() == null) {
          statement.bindNull(4);
        } else {
          statement.bindText(4, entity.getSharedSecretsJson());
        }
        if (entity.getLastMessagePreview() == null) {
          statement.bindNull(5);
        } else {
          statement.bindText(5, entity.getLastMessagePreview());
        }
        statement.bindLong(6, entity.getLastMessageTimestamp());
        statement.bindLong(7, entity.getUnreadCount());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(8, _tmp);
      }
    };
  }

  @Override
  public Object upsert(final RoomEntity room, final Continuation<? super Unit> $completion) {
    if (room == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __insertAdapterOfRoomEntity.insert(_connection, room);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Flow<List<RoomEntity>> getAll() {
    final String _sql = "SELECT * FROM rooms ORDER BY lastMessageTimestamp DESC";
    return FlowUtil.createFlow(__db, false, new String[] {"rooms"}, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "name");
        final int _columnIndexOfType = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "type");
        final int _columnIndexOfSharedSecretsJson = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "sharedSecretsJson");
        final int _columnIndexOfLastMessagePreview = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "lastMessagePreview");
        final int _columnIndexOfLastMessageTimestamp = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "lastMessageTimestamp");
        final int _columnIndexOfUnreadCount = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "unreadCount");
        final int _columnIndexOfIsActive = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "isActive");
        final List<RoomEntity> _result = new ArrayList<RoomEntity>();
        while (_stmt.step()) {
          final RoomEntity _item;
          final String _tmpId;
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null;
          } else {
            _tmpId = _stmt.getText(_columnIndexOfId);
          }
          final String _tmpName;
          if (_stmt.isNull(_columnIndexOfName)) {
            _tmpName = null;
          } else {
            _tmpName = _stmt.getText(_columnIndexOfName);
          }
          final String _tmpType;
          if (_stmt.isNull(_columnIndexOfType)) {
            _tmpType = null;
          } else {
            _tmpType = _stmt.getText(_columnIndexOfType);
          }
          final String _tmpSharedSecretsJson;
          if (_stmt.isNull(_columnIndexOfSharedSecretsJson)) {
            _tmpSharedSecretsJson = null;
          } else {
            _tmpSharedSecretsJson = _stmt.getText(_columnIndexOfSharedSecretsJson);
          }
          final String _tmpLastMessagePreview;
          if (_stmt.isNull(_columnIndexOfLastMessagePreview)) {
            _tmpLastMessagePreview = null;
          } else {
            _tmpLastMessagePreview = _stmt.getText(_columnIndexOfLastMessagePreview);
          }
          final long _tmpLastMessageTimestamp;
          _tmpLastMessageTimestamp = _stmt.getLong(_columnIndexOfLastMessageTimestamp);
          final int _tmpUnreadCount;
          _tmpUnreadCount = (int) (_stmt.getLong(_columnIndexOfUnreadCount));
          final boolean _tmpIsActive;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfIsActive));
          _tmpIsActive = _tmp != 0;
          _item = new RoomEntity(_tmpId,_tmpName,_tmpType,_tmpSharedSecretsJson,_tmpLastMessagePreview,_tmpLastMessageTimestamp,_tmpUnreadCount,_tmpIsActive);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public Flow<RoomEntity> getById(final String id) {
    final String _sql = "SELECT * FROM rooms WHERE id = ?";
    return FlowUtil.createFlow(__db, false, new String[] {"rooms"}, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (id == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, id);
        }
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "name");
        final int _columnIndexOfType = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "type");
        final int _columnIndexOfSharedSecretsJson = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "sharedSecretsJson");
        final int _columnIndexOfLastMessagePreview = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "lastMessagePreview");
        final int _columnIndexOfLastMessageTimestamp = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "lastMessageTimestamp");
        final int _columnIndexOfUnreadCount = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "unreadCount");
        final int _columnIndexOfIsActive = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "isActive");
        final RoomEntity _result;
        if (_stmt.step()) {
          final String _tmpId;
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null;
          } else {
            _tmpId = _stmt.getText(_columnIndexOfId);
          }
          final String _tmpName;
          if (_stmt.isNull(_columnIndexOfName)) {
            _tmpName = null;
          } else {
            _tmpName = _stmt.getText(_columnIndexOfName);
          }
          final String _tmpType;
          if (_stmt.isNull(_columnIndexOfType)) {
            _tmpType = null;
          } else {
            _tmpType = _stmt.getText(_columnIndexOfType);
          }
          final String _tmpSharedSecretsJson;
          if (_stmt.isNull(_columnIndexOfSharedSecretsJson)) {
            _tmpSharedSecretsJson = null;
          } else {
            _tmpSharedSecretsJson = _stmt.getText(_columnIndexOfSharedSecretsJson);
          }
          final String _tmpLastMessagePreview;
          if (_stmt.isNull(_columnIndexOfLastMessagePreview)) {
            _tmpLastMessagePreview = null;
          } else {
            _tmpLastMessagePreview = _stmt.getText(_columnIndexOfLastMessagePreview);
          }
          final long _tmpLastMessageTimestamp;
          _tmpLastMessageTimestamp = _stmt.getLong(_columnIndexOfLastMessageTimestamp);
          final int _tmpUnreadCount;
          _tmpUnreadCount = (int) (_stmt.getLong(_columnIndexOfUnreadCount));
          final boolean _tmpIsActive;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfIsActive));
          _tmpIsActive = _tmp != 0;
          _result = new RoomEntity(_tmpId,_tmpName,_tmpType,_tmpSharedSecretsJson,_tmpLastMessagePreview,_tmpLastMessageTimestamp,_tmpUnreadCount,_tmpIsActive);
        } else {
          _result = null;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    final String _sql = "DELETE FROM rooms";
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
