package com.phantomnet.core.database.dao;

import androidx.annotation.NonNull;
import androidx.room.EntityInsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.coroutines.FlowUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import com.phantomnet.core.database.entity.PersonaEntity;
import java.lang.Boolean;
import java.lang.Class;
import java.lang.Integer;
import java.lang.NullPointerException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@SuppressWarnings({"unchecked", "deprecation", "removal"})
public final class PersonaDao_Impl implements PersonaDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<PersonaEntity> __insertAdapterOfPersonaEntity;

  public PersonaDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfPersonaEntity = new EntityInsertAdapter<PersonaEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `personas` (`id`,`publicKeyX25519`,`publicKeyKyber`,`privateKeyEncrypted`,`fingerprint`,`prekeyBundleJson`,`secretBundleJson`,`createdAt`,`isActive`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final PersonaEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindText(1, entity.getId());
        }
        if (entity.getPublicKeyX25519() == null) {
          statement.bindNull(2);
        } else {
          statement.bindBlob(2, entity.getPublicKeyX25519());
        }
        if (entity.getPublicKeyKyber() == null) {
          statement.bindNull(3);
        } else {
          statement.bindBlob(3, entity.getPublicKeyKyber());
        }
        if (entity.getPrivateKeyEncrypted() == null) {
          statement.bindNull(4);
        } else {
          statement.bindBlob(4, entity.getPrivateKeyEncrypted());
        }
        if (entity.getFingerprint() == null) {
          statement.bindNull(5);
        } else {
          statement.bindText(5, entity.getFingerprint());
        }
        if (entity.getPrekeyBundleJson() == null) {
          statement.bindNull(6);
        } else {
          statement.bindText(6, entity.getPrekeyBundleJson());
        }
        if (entity.getSecretBundleJson() == null) {
          statement.bindNull(7);
        } else {
          statement.bindText(7, entity.getSecretBundleJson());
        }
        statement.bindLong(8, entity.getCreatedAt());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(9, _tmp);
      }
    };
  }

  @Override
  public Object insert(final PersonaEntity persona, final Continuation<? super Unit> $completion) {
    if (persona == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __insertAdapterOfPersonaEntity.insert(_connection, persona);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Flow<PersonaEntity> getActivePersona() {
    final String _sql = "SELECT * FROM personas WHERE isActive = 1 LIMIT 1";
    return FlowUtil.createFlow(__db, false, new String[] {"personas"}, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfPublicKeyX25519 = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "publicKeyX25519");
        final int _columnIndexOfPublicKeyKyber = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "publicKeyKyber");
        final int _columnIndexOfPrivateKeyEncrypted = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "privateKeyEncrypted");
        final int _columnIndexOfFingerprint = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "fingerprint");
        final int _columnIndexOfPrekeyBundleJson = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "prekeyBundleJson");
        final int _columnIndexOfSecretBundleJson = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "secretBundleJson");
        final int _columnIndexOfCreatedAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "createdAt");
        final int _columnIndexOfIsActive = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "isActive");
        final PersonaEntity _result;
        if (_stmt.step()) {
          final String _tmpId;
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null;
          } else {
            _tmpId = _stmt.getText(_columnIndexOfId);
          }
          final byte[] _tmpPublicKeyX25519;
          if (_stmt.isNull(_columnIndexOfPublicKeyX25519)) {
            _tmpPublicKeyX25519 = null;
          } else {
            _tmpPublicKeyX25519 = _stmt.getBlob(_columnIndexOfPublicKeyX25519);
          }
          final byte[] _tmpPublicKeyKyber;
          if (_stmt.isNull(_columnIndexOfPublicKeyKyber)) {
            _tmpPublicKeyKyber = null;
          } else {
            _tmpPublicKeyKyber = _stmt.getBlob(_columnIndexOfPublicKeyKyber);
          }
          final byte[] _tmpPrivateKeyEncrypted;
          if (_stmt.isNull(_columnIndexOfPrivateKeyEncrypted)) {
            _tmpPrivateKeyEncrypted = null;
          } else {
            _tmpPrivateKeyEncrypted = _stmt.getBlob(_columnIndexOfPrivateKeyEncrypted);
          }
          final String _tmpFingerprint;
          if (_stmt.isNull(_columnIndexOfFingerprint)) {
            _tmpFingerprint = null;
          } else {
            _tmpFingerprint = _stmt.getText(_columnIndexOfFingerprint);
          }
          final String _tmpPrekeyBundleJson;
          if (_stmt.isNull(_columnIndexOfPrekeyBundleJson)) {
            _tmpPrekeyBundleJson = null;
          } else {
            _tmpPrekeyBundleJson = _stmt.getText(_columnIndexOfPrekeyBundleJson);
          }
          final String _tmpSecretBundleJson;
          if (_stmt.isNull(_columnIndexOfSecretBundleJson)) {
            _tmpSecretBundleJson = null;
          } else {
            _tmpSecretBundleJson = _stmt.getText(_columnIndexOfSecretBundleJson);
          }
          final long _tmpCreatedAt;
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt);
          final boolean _tmpIsActive;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfIsActive));
          _tmpIsActive = _tmp != 0;
          _result = new PersonaEntity(_tmpId,_tmpPublicKeyX25519,_tmpPublicKeyKyber,_tmpPrivateKeyEncrypted,_tmpFingerprint,_tmpPrekeyBundleJson,_tmpSecretBundleJson,_tmpCreatedAt,_tmpIsActive);
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
  public Object hasAnyPersona(final Continuation<? super Boolean> $completion) {
    final String _sql = "SELECT COUNT(*) > 0 FROM personas";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final Boolean _result;
        if (_stmt.step()) {
          final Integer _tmp;
          if (_stmt.isNull(0)) {
            _tmp = null;
          } else {
            _tmp = (int) (_stmt.getLong(0));
          }
          _result = _tmp == null ? null : _tmp != 0;
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
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    final String _sql = "DELETE FROM personas";
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
