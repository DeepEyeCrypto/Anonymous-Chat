package com.phantomnet.core.database.dao;

import androidx.annotation.NonNull;
import androidx.room.EntityInsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.coroutines.FlowUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import com.phantomnet.core.database.entity.ConversationEntity;
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
public final class ConversationDao_Impl implements ConversationDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<ConversationEntity> __insertAdapterOfConversationEntity;

  public ConversationDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfConversationEntity = new EntityInsertAdapter<ConversationEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `conversations` (`id`,`contactName`,`contactFingerprint`,`contactPublicKey`,`lastMessagePreview`,`lastMessageTimestamp`,`unreadCount`,`isOnline`,`routingMode`,`sharedSecret`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final ConversationEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindText(1, entity.getId());
        }
        if (entity.getContactName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getContactName());
        }
        if (entity.getContactFingerprint() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getContactFingerprint());
        }
        if (entity.getContactPublicKey() == null) {
          statement.bindNull(4);
        } else {
          statement.bindBlob(4, entity.getContactPublicKey());
        }
        if (entity.getLastMessagePreview() == null) {
          statement.bindNull(5);
        } else {
          statement.bindText(5, entity.getLastMessagePreview());
        }
        statement.bindLong(6, entity.getLastMessageTimestamp());
        statement.bindLong(7, entity.getUnreadCount());
        final int _tmp = entity.isOnline() ? 1 : 0;
        statement.bindLong(8, _tmp);
        if (entity.getRoutingMode() == null) {
          statement.bindNull(9);
        } else {
          statement.bindText(9, entity.getRoutingMode());
        }
        if (entity.getSharedSecret() == null) {
          statement.bindNull(10);
        } else {
          statement.bindBlob(10, entity.getSharedSecret());
        }
      }
    };
  }

  @Override
  public Object upsert(final ConversationEntity conversation,
      final Continuation<? super Unit> $completion) {
    if (conversation == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __insertAdapterOfConversationEntity.insert(_connection, conversation);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Flow<List<ConversationEntity>> getAll() {
    final String _sql = "SELECT * FROM conversations ORDER BY lastMessageTimestamp DESC";
    return FlowUtil.createFlow(__db, false, new String[] {"conversations"}, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfContactName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "contactName");
        final int _columnIndexOfContactFingerprint = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "contactFingerprint");
        final int _columnIndexOfContactPublicKey = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "contactPublicKey");
        final int _columnIndexOfLastMessagePreview = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "lastMessagePreview");
        final int _columnIndexOfLastMessageTimestamp = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "lastMessageTimestamp");
        final int _columnIndexOfUnreadCount = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "unreadCount");
        final int _columnIndexOfIsOnline = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "isOnline");
        final int _columnIndexOfRoutingMode = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "routingMode");
        final int _columnIndexOfSharedSecret = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "sharedSecret");
        final List<ConversationEntity> _result = new ArrayList<ConversationEntity>();
        while (_stmt.step()) {
          final ConversationEntity _item;
          final String _tmpId;
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null;
          } else {
            _tmpId = _stmt.getText(_columnIndexOfId);
          }
          final String _tmpContactName;
          if (_stmt.isNull(_columnIndexOfContactName)) {
            _tmpContactName = null;
          } else {
            _tmpContactName = _stmt.getText(_columnIndexOfContactName);
          }
          final String _tmpContactFingerprint;
          if (_stmt.isNull(_columnIndexOfContactFingerprint)) {
            _tmpContactFingerprint = null;
          } else {
            _tmpContactFingerprint = _stmt.getText(_columnIndexOfContactFingerprint);
          }
          final byte[] _tmpContactPublicKey;
          if (_stmt.isNull(_columnIndexOfContactPublicKey)) {
            _tmpContactPublicKey = null;
          } else {
            _tmpContactPublicKey = _stmt.getBlob(_columnIndexOfContactPublicKey);
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
          final boolean _tmpIsOnline;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfIsOnline));
          _tmpIsOnline = _tmp != 0;
          final String _tmpRoutingMode;
          if (_stmt.isNull(_columnIndexOfRoutingMode)) {
            _tmpRoutingMode = null;
          } else {
            _tmpRoutingMode = _stmt.getText(_columnIndexOfRoutingMode);
          }
          final byte[] _tmpSharedSecret;
          if (_stmt.isNull(_columnIndexOfSharedSecret)) {
            _tmpSharedSecret = null;
          } else {
            _tmpSharedSecret = _stmt.getBlob(_columnIndexOfSharedSecret);
          }
          _item = new ConversationEntity(_tmpId,_tmpContactName,_tmpContactFingerprint,_tmpContactPublicKey,_tmpLastMessagePreview,_tmpLastMessageTimestamp,_tmpUnreadCount,_tmpIsOnline,_tmpRoutingMode,_tmpSharedSecret);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public Flow<ConversationEntity> getById(final String id) {
    final String _sql = "SELECT * FROM conversations WHERE id = ?";
    return FlowUtil.createFlow(__db, false, new String[] {"conversations"}, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (id == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, id);
        }
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfContactName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "contactName");
        final int _columnIndexOfContactFingerprint = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "contactFingerprint");
        final int _columnIndexOfContactPublicKey = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "contactPublicKey");
        final int _columnIndexOfLastMessagePreview = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "lastMessagePreview");
        final int _columnIndexOfLastMessageTimestamp = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "lastMessageTimestamp");
        final int _columnIndexOfUnreadCount = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "unreadCount");
        final int _columnIndexOfIsOnline = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "isOnline");
        final int _columnIndexOfRoutingMode = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "routingMode");
        final int _columnIndexOfSharedSecret = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "sharedSecret");
        final ConversationEntity _result;
        if (_stmt.step()) {
          final String _tmpId;
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null;
          } else {
            _tmpId = _stmt.getText(_columnIndexOfId);
          }
          final String _tmpContactName;
          if (_stmt.isNull(_columnIndexOfContactName)) {
            _tmpContactName = null;
          } else {
            _tmpContactName = _stmt.getText(_columnIndexOfContactName);
          }
          final String _tmpContactFingerprint;
          if (_stmt.isNull(_columnIndexOfContactFingerprint)) {
            _tmpContactFingerprint = null;
          } else {
            _tmpContactFingerprint = _stmt.getText(_columnIndexOfContactFingerprint);
          }
          final byte[] _tmpContactPublicKey;
          if (_stmt.isNull(_columnIndexOfContactPublicKey)) {
            _tmpContactPublicKey = null;
          } else {
            _tmpContactPublicKey = _stmt.getBlob(_columnIndexOfContactPublicKey);
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
          final boolean _tmpIsOnline;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfIsOnline));
          _tmpIsOnline = _tmp != 0;
          final String _tmpRoutingMode;
          if (_stmt.isNull(_columnIndexOfRoutingMode)) {
            _tmpRoutingMode = null;
          } else {
            _tmpRoutingMode = _stmt.getText(_columnIndexOfRoutingMode);
          }
          final byte[] _tmpSharedSecret;
          if (_stmt.isNull(_columnIndexOfSharedSecret)) {
            _tmpSharedSecret = null;
          } else {
            _tmpSharedSecret = _stmt.getBlob(_columnIndexOfSharedSecret);
          }
          _result = new ConversationEntity(_tmpId,_tmpContactName,_tmpContactFingerprint,_tmpContactPublicKey,_tmpLastMessagePreview,_tmpLastMessageTimestamp,_tmpUnreadCount,_tmpIsOnline,_tmpRoutingMode,_tmpSharedSecret);
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
  public Object getByFingerprint(final String fp,
      final Continuation<? super ConversationEntity> $completion) {
    final String _sql = "SELECT * FROM conversations WHERE contactFingerprint = ? LIMIT 1";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (fp == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, fp);
        }
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfContactName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "contactName");
        final int _columnIndexOfContactFingerprint = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "contactFingerprint");
        final int _columnIndexOfContactPublicKey = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "contactPublicKey");
        final int _columnIndexOfLastMessagePreview = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "lastMessagePreview");
        final int _columnIndexOfLastMessageTimestamp = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "lastMessageTimestamp");
        final int _columnIndexOfUnreadCount = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "unreadCount");
        final int _columnIndexOfIsOnline = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "isOnline");
        final int _columnIndexOfRoutingMode = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "routingMode");
        final int _columnIndexOfSharedSecret = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "sharedSecret");
        final ConversationEntity _result;
        if (_stmt.step()) {
          final String _tmpId;
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null;
          } else {
            _tmpId = _stmt.getText(_columnIndexOfId);
          }
          final String _tmpContactName;
          if (_stmt.isNull(_columnIndexOfContactName)) {
            _tmpContactName = null;
          } else {
            _tmpContactName = _stmt.getText(_columnIndexOfContactName);
          }
          final String _tmpContactFingerprint;
          if (_stmt.isNull(_columnIndexOfContactFingerprint)) {
            _tmpContactFingerprint = null;
          } else {
            _tmpContactFingerprint = _stmt.getText(_columnIndexOfContactFingerprint);
          }
          final byte[] _tmpContactPublicKey;
          if (_stmt.isNull(_columnIndexOfContactPublicKey)) {
            _tmpContactPublicKey = null;
          } else {
            _tmpContactPublicKey = _stmt.getBlob(_columnIndexOfContactPublicKey);
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
          final boolean _tmpIsOnline;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfIsOnline));
          _tmpIsOnline = _tmp != 0;
          final String _tmpRoutingMode;
          if (_stmt.isNull(_columnIndexOfRoutingMode)) {
            _tmpRoutingMode = null;
          } else {
            _tmpRoutingMode = _stmt.getText(_columnIndexOfRoutingMode);
          }
          final byte[] _tmpSharedSecret;
          if (_stmt.isNull(_columnIndexOfSharedSecret)) {
            _tmpSharedSecret = null;
          } else {
            _tmpSharedSecret = _stmt.getBlob(_columnIndexOfSharedSecret);
          }
          _result = new ConversationEntity(_tmpId,_tmpContactName,_tmpContactFingerprint,_tmpContactPublicKey,_tmpLastMessagePreview,_tmpLastMessageTimestamp,_tmpUnreadCount,_tmpIsOnline,_tmpRoutingMode,_tmpSharedSecret);
        } else {
          _result = null;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object getByIdSync(final String id,
      final Continuation<? super ConversationEntity> $completion) {
    final String _sql = "SELECT * FROM conversations WHERE id = ? LIMIT 1";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (id == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, id);
        }
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfContactName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "contactName");
        final int _columnIndexOfContactFingerprint = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "contactFingerprint");
        final int _columnIndexOfContactPublicKey = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "contactPublicKey");
        final int _columnIndexOfLastMessagePreview = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "lastMessagePreview");
        final int _columnIndexOfLastMessageTimestamp = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "lastMessageTimestamp");
        final int _columnIndexOfUnreadCount = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "unreadCount");
        final int _columnIndexOfIsOnline = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "isOnline");
        final int _columnIndexOfRoutingMode = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "routingMode");
        final int _columnIndexOfSharedSecret = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "sharedSecret");
        final ConversationEntity _result;
        if (_stmt.step()) {
          final String _tmpId;
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null;
          } else {
            _tmpId = _stmt.getText(_columnIndexOfId);
          }
          final String _tmpContactName;
          if (_stmt.isNull(_columnIndexOfContactName)) {
            _tmpContactName = null;
          } else {
            _tmpContactName = _stmt.getText(_columnIndexOfContactName);
          }
          final String _tmpContactFingerprint;
          if (_stmt.isNull(_columnIndexOfContactFingerprint)) {
            _tmpContactFingerprint = null;
          } else {
            _tmpContactFingerprint = _stmt.getText(_columnIndexOfContactFingerprint);
          }
          final byte[] _tmpContactPublicKey;
          if (_stmt.isNull(_columnIndexOfContactPublicKey)) {
            _tmpContactPublicKey = null;
          } else {
            _tmpContactPublicKey = _stmt.getBlob(_columnIndexOfContactPublicKey);
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
          final boolean _tmpIsOnline;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfIsOnline));
          _tmpIsOnline = _tmp != 0;
          final String _tmpRoutingMode;
          if (_stmt.isNull(_columnIndexOfRoutingMode)) {
            _tmpRoutingMode = null;
          } else {
            _tmpRoutingMode = _stmt.getText(_columnIndexOfRoutingMode);
          }
          final byte[] _tmpSharedSecret;
          if (_stmt.isNull(_columnIndexOfSharedSecret)) {
            _tmpSharedSecret = null;
          } else {
            _tmpSharedSecret = _stmt.getBlob(_columnIndexOfSharedSecret);
          }
          _result = new ConversationEntity(_tmpId,_tmpContactName,_tmpContactFingerprint,_tmpContactPublicKey,_tmpLastMessagePreview,_tmpLastMessageTimestamp,_tmpUnreadCount,_tmpIsOnline,_tmpRoutingMode,_tmpSharedSecret);
        } else {
          _result = null;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object markRead(final String id, final Continuation<? super Unit> $completion) {
    final String _sql = "UPDATE conversations SET unreadCount = 0 WHERE id = ?";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (id == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, id);
        }
        _stmt.step();
        return Unit.INSTANCE;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object updateLastMessage(final String conversationId, final String preview,
      final long timestamp, final boolean incrementUnread,
      final Continuation<? super Unit> $completion) {
    final String _sql = "\n"
            + "        UPDATE conversations \n"
            + "        SET lastMessagePreview = ?, \n"
            + "            lastMessageTimestamp = ?,\n"
            + "            unreadCount = unreadCount + CASE WHEN ? THEN 1 ELSE 0 END\n"
            + "        WHERE id = ?\n"
            + "    ";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (preview == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, preview);
        }
        _argIndex = 2;
        _stmt.bindLong(_argIndex, timestamp);
        _argIndex = 3;
        final int _tmp = incrementUnread ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 4;
        if (conversationId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, conversationId);
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
    final String _sql = "DELETE FROM conversations";
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

  @Override
  public Object updateSharedSecret(final String id, final byte[] secret,
      final Continuation<? super Unit> $completion) {
    final String _sql = "UPDATE conversations SET sharedSecret = ? WHERE id = ?";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (secret == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindBlob(_argIndex, secret);
        }
        _argIndex = 2;
        if (id == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, id);
        }
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
