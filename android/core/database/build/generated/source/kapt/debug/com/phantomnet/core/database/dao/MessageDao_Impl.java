package com.phantomnet.core.database.dao;

import androidx.annotation.NonNull;
import androidx.room.EntityInsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.coroutines.FlowUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import com.phantomnet.core.database.entity.MessageEntity;
import java.lang.Class;
import java.lang.Long;
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
public final class MessageDao_Impl implements MessageDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<MessageEntity> __insertAdapterOfMessageEntity;

  public MessageDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfMessageEntity = new EntityInsertAdapter<MessageEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `messages` (`id`,`conversationId`,`senderId`,`contentPlaintext`,`contentCiphertext`,`timestamp`,`isMe`,`status`,`expiresAt`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final MessageEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindText(1, entity.getId());
        }
        if (entity.getConversationId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getConversationId());
        }
        if (entity.getSenderId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getSenderId());
        }
        if (entity.getContentPlaintext() == null) {
          statement.bindNull(4);
        } else {
          statement.bindText(4, entity.getContentPlaintext());
        }
        if (entity.getContentCiphertext() == null) {
          statement.bindNull(5);
        } else {
          statement.bindBlob(5, entity.getContentCiphertext());
        }
        statement.bindLong(6, entity.getTimestamp());
        final int _tmp = entity.isMe() ? 1 : 0;
        statement.bindLong(7, _tmp);
        if (entity.getStatus() == null) {
          statement.bindNull(8);
        } else {
          statement.bindText(8, entity.getStatus());
        }
        if (entity.getExpiresAt() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getExpiresAt());
        }
      }
    };
  }

  @Override
  public Object insert(final MessageEntity message, final Continuation<? super Unit> $completion) {
    if (message == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __insertAdapterOfMessageEntity.insert(_connection, message);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Flow<List<MessageEntity>> getForContext(final String id) {
    final String _sql = "SELECT * FROM messages WHERE conversationId = ? OR conversationId = ? ORDER BY timestamp DESC";
    return FlowUtil.createFlow(__db, false, new String[] {"messages"}, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (id == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, id);
        }
        _argIndex = 2;
        if (id == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, id);
        }
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfConversationId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "conversationId");
        final int _columnIndexOfSenderId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "senderId");
        final int _columnIndexOfContentPlaintext = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "contentPlaintext");
        final int _columnIndexOfContentCiphertext = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "contentCiphertext");
        final int _columnIndexOfTimestamp = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "timestamp");
        final int _columnIndexOfIsMe = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "isMe");
        final int _columnIndexOfStatus = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "status");
        final int _columnIndexOfExpiresAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "expiresAt");
        final List<MessageEntity> _result = new ArrayList<MessageEntity>();
        while (_stmt.step()) {
          final MessageEntity _item;
          final String _tmpId;
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null;
          } else {
            _tmpId = _stmt.getText(_columnIndexOfId);
          }
          final String _tmpConversationId;
          if (_stmt.isNull(_columnIndexOfConversationId)) {
            _tmpConversationId = null;
          } else {
            _tmpConversationId = _stmt.getText(_columnIndexOfConversationId);
          }
          final String _tmpSenderId;
          if (_stmt.isNull(_columnIndexOfSenderId)) {
            _tmpSenderId = null;
          } else {
            _tmpSenderId = _stmt.getText(_columnIndexOfSenderId);
          }
          final String _tmpContentPlaintext;
          if (_stmt.isNull(_columnIndexOfContentPlaintext)) {
            _tmpContentPlaintext = null;
          } else {
            _tmpContentPlaintext = _stmt.getText(_columnIndexOfContentPlaintext);
          }
          final byte[] _tmpContentCiphertext;
          if (_stmt.isNull(_columnIndexOfContentCiphertext)) {
            _tmpContentCiphertext = null;
          } else {
            _tmpContentCiphertext = _stmt.getBlob(_columnIndexOfContentCiphertext);
          }
          final long _tmpTimestamp;
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp);
          final boolean _tmpIsMe;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfIsMe));
          _tmpIsMe = _tmp != 0;
          final String _tmpStatus;
          if (_stmt.isNull(_columnIndexOfStatus)) {
            _tmpStatus = null;
          } else {
            _tmpStatus = _stmt.getText(_columnIndexOfStatus);
          }
          final Long _tmpExpiresAt;
          if (_stmt.isNull(_columnIndexOfExpiresAt)) {
            _tmpExpiresAt = null;
          } else {
            _tmpExpiresAt = _stmt.getLong(_columnIndexOfExpiresAt);
          }
          _item = new MessageEntity(_tmpId,_tmpConversationId,_tmpSenderId,_tmpContentPlaintext,_tmpContentCiphertext,_tmpTimestamp,_tmpIsMe,_tmpStatus,_tmpExpiresAt);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public Flow<List<MessageEntity>> getRecentForConversation(final String convId, final int limit) {
    final String _sql = "SELECT * FROM messages WHERE conversationId = ? ORDER BY timestamp DESC LIMIT ?";
    return FlowUtil.createFlow(__db, false, new String[] {"messages"}, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (convId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, convId);
        }
        _argIndex = 2;
        _stmt.bindLong(_argIndex, limit);
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfConversationId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "conversationId");
        final int _columnIndexOfSenderId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "senderId");
        final int _columnIndexOfContentPlaintext = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "contentPlaintext");
        final int _columnIndexOfContentCiphertext = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "contentCiphertext");
        final int _columnIndexOfTimestamp = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "timestamp");
        final int _columnIndexOfIsMe = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "isMe");
        final int _columnIndexOfStatus = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "status");
        final int _columnIndexOfExpiresAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "expiresAt");
        final List<MessageEntity> _result = new ArrayList<MessageEntity>();
        while (_stmt.step()) {
          final MessageEntity _item;
          final String _tmpId;
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null;
          } else {
            _tmpId = _stmt.getText(_columnIndexOfId);
          }
          final String _tmpConversationId;
          if (_stmt.isNull(_columnIndexOfConversationId)) {
            _tmpConversationId = null;
          } else {
            _tmpConversationId = _stmt.getText(_columnIndexOfConversationId);
          }
          final String _tmpSenderId;
          if (_stmt.isNull(_columnIndexOfSenderId)) {
            _tmpSenderId = null;
          } else {
            _tmpSenderId = _stmt.getText(_columnIndexOfSenderId);
          }
          final String _tmpContentPlaintext;
          if (_stmt.isNull(_columnIndexOfContentPlaintext)) {
            _tmpContentPlaintext = null;
          } else {
            _tmpContentPlaintext = _stmt.getText(_columnIndexOfContentPlaintext);
          }
          final byte[] _tmpContentCiphertext;
          if (_stmt.isNull(_columnIndexOfContentCiphertext)) {
            _tmpContentCiphertext = null;
          } else {
            _tmpContentCiphertext = _stmt.getBlob(_columnIndexOfContentCiphertext);
          }
          final long _tmpTimestamp;
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp);
          final boolean _tmpIsMe;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfIsMe));
          _tmpIsMe = _tmp != 0;
          final String _tmpStatus;
          if (_stmt.isNull(_columnIndexOfStatus)) {
            _tmpStatus = null;
          } else {
            _tmpStatus = _stmt.getText(_columnIndexOfStatus);
          }
          final Long _tmpExpiresAt;
          if (_stmt.isNull(_columnIndexOfExpiresAt)) {
            _tmpExpiresAt = null;
          } else {
            _tmpExpiresAt = _stmt.getLong(_columnIndexOfExpiresAt);
          }
          _item = new MessageEntity(_tmpId,_tmpConversationId,_tmpSenderId,_tmpContentPlaintext,_tmpContentCiphertext,_tmpTimestamp,_tmpIsMe,_tmpStatus,_tmpExpiresAt);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public Object updateStatus(final String messageId, final String status,
      final Continuation<? super Unit> $completion) {
    final String _sql = "UPDATE messages SET status = ? WHERE id = ?";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (status == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, status);
        }
        _argIndex = 2;
        if (messageId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, messageId);
        }
        _stmt.step();
        return Unit.INSTANCE;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object deleteForConversation(final String convId,
      final Continuation<? super Unit> $completion) {
    final String _sql = "DELETE FROM messages WHERE conversationId = ?";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (convId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, convId);
        }
        _stmt.step();
        return Unit.INSTANCE;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object deleteExpired(final long now, final Continuation<? super Unit> $completion) {
    final String _sql = "DELETE FROM messages WHERE expiresAt IS NOT NULL AND expiresAt < ?";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, now);
        _stmt.step();
        return Unit.INSTANCE;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    final String _sql = "DELETE FROM messages";
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
