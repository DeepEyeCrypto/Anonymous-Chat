package com.phantomnet.core.database;

import androidx.annotation.NonNull;
import androidx.room.InvalidationTracker;
import androidx.room.RoomOpenDelegate;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.SQLite;
import androidx.sqlite.SQLiteConnection;
import com.phantomnet.core.database.dao.CallLogDao;
import com.phantomnet.core.database.dao.CallLogDao_Impl;
import com.phantomnet.core.database.dao.ConversationDao;
import com.phantomnet.core.database.dao.ConversationDao_Impl;
import com.phantomnet.core.database.dao.MessageDao;
import com.phantomnet.core.database.dao.MessageDao_Impl;
import com.phantomnet.core.database.dao.PersonaDao;
import com.phantomnet.core.database.dao.PersonaDao_Impl;
import com.phantomnet.core.database.dao.RoomDao;
import com.phantomnet.core.database.dao.RoomDao_Impl;
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

@SuppressWarnings({"unchecked", "deprecation", "removal"})
public final class PhantomDatabase_Impl extends PhantomDatabase {
  private volatile PersonaDao _personaDao;

  private volatile ConversationDao _conversationDao;

  private volatile MessageDao _messageDao;

  private volatile RoomDao _roomDao;

  private volatile CallLogDao _callLogDao;

  @Override
  @NonNull
  protected RoomOpenDelegate createOpenDelegate() {
    final RoomOpenDelegate _openDelegate = new RoomOpenDelegate(1, "b8c88c612e60582e65b14c31df784a81", "dd3357ec18577397ab85698a771c3508") {
      @Override
      public void createAllTables(@NonNull final SQLiteConnection connection) {
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS `personas` (`id` TEXT NOT NULL, `publicKeyX25519` BLOB NOT NULL, `publicKeyKyber` BLOB NOT NULL, `privateKeyEncrypted` BLOB NOT NULL, `fingerprint` TEXT NOT NULL, `prekeyBundleJson` TEXT, `secretBundleJson` TEXT, `createdAt` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS `conversations` (`id` TEXT NOT NULL, `contactName` TEXT NOT NULL, `contactFingerprint` TEXT NOT NULL, `contactPublicKey` BLOB NOT NULL, `lastMessagePreview` TEXT NOT NULL, `lastMessageTimestamp` INTEGER NOT NULL, `unreadCount` INTEGER NOT NULL, `isOnline` INTEGER NOT NULL, `routingMode` TEXT NOT NULL, `sharedSecret` BLOB, PRIMARY KEY(`id`))");
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS `messages` (`id` TEXT NOT NULL, `conversationId` TEXT NOT NULL, `senderId` TEXT NOT NULL, `contentPlaintext` TEXT NOT NULL, `contentCiphertext` BLOB, `timestamp` INTEGER NOT NULL, `isMe` INTEGER NOT NULL, `status` TEXT NOT NULL, `expiresAt` INTEGER, PRIMARY KEY(`id`), FOREIGN KEY(`conversationId`) REFERENCES `conversations`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        SQLite.execSQL(connection, "CREATE INDEX IF NOT EXISTS `index_messages_conversationId` ON `messages` (`conversationId`)");
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS `rooms` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `type` TEXT NOT NULL, `sharedSecretsJson` TEXT NOT NULL, `lastMessagePreview` TEXT NOT NULL, `lastMessageTimestamp` INTEGER NOT NULL, `unreadCount` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS `call_logs` (`sessionId` TEXT NOT NULL, `conversationId` TEXT NOT NULL, `direction` TEXT NOT NULL, `durationSec` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `privacyMode` TEXT NOT NULL, `outcome` TEXT NOT NULL, PRIMARY KEY(`sessionId`))");
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        SQLite.execSQL(connection, "INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'b8c88c612e60582e65b14c31df784a81')");
      }

      @Override
      public void dropAllTables(@NonNull final SQLiteConnection connection) {
        SQLite.execSQL(connection, "DROP TABLE IF EXISTS `personas`");
        SQLite.execSQL(connection, "DROP TABLE IF EXISTS `conversations`");
        SQLite.execSQL(connection, "DROP TABLE IF EXISTS `messages`");
        SQLite.execSQL(connection, "DROP TABLE IF EXISTS `rooms`");
        SQLite.execSQL(connection, "DROP TABLE IF EXISTS `call_logs`");
      }

      @Override
      public void onCreate(@NonNull final SQLiteConnection connection) {
      }

      @Override
      public void onOpen(@NonNull final SQLiteConnection connection) {
        SQLite.execSQL(connection, "PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(connection);
      }

      @Override
      public void onPreMigrate(@NonNull final SQLiteConnection connection) {
        DBUtil.dropFtsSyncTriggers(connection);
      }

      @Override
      public void onPostMigrate(@NonNull final SQLiteConnection connection) {
      }

      @Override
      @NonNull
      public RoomOpenDelegate.ValidationResult onValidateSchema(
          @NonNull final SQLiteConnection connection) {
        final Map<String, TableInfo.Column> _columnsPersonas = new HashMap<String, TableInfo.Column>(9);
        _columnsPersonas.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersonas.put("publicKeyX25519", new TableInfo.Column("publicKeyX25519", "BLOB", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersonas.put("publicKeyKyber", new TableInfo.Column("publicKeyKyber", "BLOB", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersonas.put("privateKeyEncrypted", new TableInfo.Column("privateKeyEncrypted", "BLOB", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersonas.put("fingerprint", new TableInfo.Column("fingerprint", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersonas.put("prekeyBundleJson", new TableInfo.Column("prekeyBundleJson", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersonas.put("secretBundleJson", new TableInfo.Column("secretBundleJson", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersonas.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersonas.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final Set<TableInfo.ForeignKey> _foreignKeysPersonas = new HashSet<TableInfo.ForeignKey>(0);
        final Set<TableInfo.Index> _indicesPersonas = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPersonas = new TableInfo("personas", _columnsPersonas, _foreignKeysPersonas, _indicesPersonas);
        final TableInfo _existingPersonas = TableInfo.read(connection, "personas");
        if (!_infoPersonas.equals(_existingPersonas)) {
          return new RoomOpenDelegate.ValidationResult(false, "personas(com.phantomnet.core.database.entity.PersonaEntity).\n"
                  + " Expected:\n" + _infoPersonas + "\n"
                  + " Found:\n" + _existingPersonas);
        }
        final Map<String, TableInfo.Column> _columnsConversations = new HashMap<String, TableInfo.Column>(10);
        _columnsConversations.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConversations.put("contactName", new TableInfo.Column("contactName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConversations.put("contactFingerprint", new TableInfo.Column("contactFingerprint", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConversations.put("contactPublicKey", new TableInfo.Column("contactPublicKey", "BLOB", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConversations.put("lastMessagePreview", new TableInfo.Column("lastMessagePreview", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConversations.put("lastMessageTimestamp", new TableInfo.Column("lastMessageTimestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConversations.put("unreadCount", new TableInfo.Column("unreadCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConversations.put("isOnline", new TableInfo.Column("isOnline", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConversations.put("routingMode", new TableInfo.Column("routingMode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConversations.put("sharedSecret", new TableInfo.Column("sharedSecret", "BLOB", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final Set<TableInfo.ForeignKey> _foreignKeysConversations = new HashSet<TableInfo.ForeignKey>(0);
        final Set<TableInfo.Index> _indicesConversations = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoConversations = new TableInfo("conversations", _columnsConversations, _foreignKeysConversations, _indicesConversations);
        final TableInfo _existingConversations = TableInfo.read(connection, "conversations");
        if (!_infoConversations.equals(_existingConversations)) {
          return new RoomOpenDelegate.ValidationResult(false, "conversations(com.phantomnet.core.database.entity.ConversationEntity).\n"
                  + " Expected:\n" + _infoConversations + "\n"
                  + " Found:\n" + _existingConversations);
        }
        final Map<String, TableInfo.Column> _columnsMessages = new HashMap<String, TableInfo.Column>(9);
        _columnsMessages.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("conversationId", new TableInfo.Column("conversationId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("senderId", new TableInfo.Column("senderId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("contentPlaintext", new TableInfo.Column("contentPlaintext", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("contentCiphertext", new TableInfo.Column("contentCiphertext", "BLOB", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("isMe", new TableInfo.Column("isMe", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("expiresAt", new TableInfo.Column("expiresAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final Set<TableInfo.ForeignKey> _foreignKeysMessages = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysMessages.add(new TableInfo.ForeignKey("conversations", "CASCADE", "NO ACTION", Arrays.asList("conversationId"), Arrays.asList("id")));
        final Set<TableInfo.Index> _indicesMessages = new HashSet<TableInfo.Index>(1);
        _indicesMessages.add(new TableInfo.Index("index_messages_conversationId", false, Arrays.asList("conversationId"), Arrays.asList("ASC")));
        final TableInfo _infoMessages = new TableInfo("messages", _columnsMessages, _foreignKeysMessages, _indicesMessages);
        final TableInfo _existingMessages = TableInfo.read(connection, "messages");
        if (!_infoMessages.equals(_existingMessages)) {
          return new RoomOpenDelegate.ValidationResult(false, "messages(com.phantomnet.core.database.entity.MessageEntity).\n"
                  + " Expected:\n" + _infoMessages + "\n"
                  + " Found:\n" + _existingMessages);
        }
        final Map<String, TableInfo.Column> _columnsRooms = new HashMap<String, TableInfo.Column>(8);
        _columnsRooms.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRooms.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRooms.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRooms.put("sharedSecretsJson", new TableInfo.Column("sharedSecretsJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRooms.put("lastMessagePreview", new TableInfo.Column("lastMessagePreview", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRooms.put("lastMessageTimestamp", new TableInfo.Column("lastMessageTimestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRooms.put("unreadCount", new TableInfo.Column("unreadCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRooms.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final Set<TableInfo.ForeignKey> _foreignKeysRooms = new HashSet<TableInfo.ForeignKey>(0);
        final Set<TableInfo.Index> _indicesRooms = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoRooms = new TableInfo("rooms", _columnsRooms, _foreignKeysRooms, _indicesRooms);
        final TableInfo _existingRooms = TableInfo.read(connection, "rooms");
        if (!_infoRooms.equals(_existingRooms)) {
          return new RoomOpenDelegate.ValidationResult(false, "rooms(com.phantomnet.core.database.entity.RoomEntity).\n"
                  + " Expected:\n" + _infoRooms + "\n"
                  + " Found:\n" + _existingRooms);
        }
        final Map<String, TableInfo.Column> _columnsCallLogs = new HashMap<String, TableInfo.Column>(7);
        _columnsCallLogs.put("sessionId", new TableInfo.Column("sessionId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCallLogs.put("conversationId", new TableInfo.Column("conversationId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCallLogs.put("direction", new TableInfo.Column("direction", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCallLogs.put("durationSec", new TableInfo.Column("durationSec", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCallLogs.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCallLogs.put("privacyMode", new TableInfo.Column("privacyMode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCallLogs.put("outcome", new TableInfo.Column("outcome", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final Set<TableInfo.ForeignKey> _foreignKeysCallLogs = new HashSet<TableInfo.ForeignKey>(0);
        final Set<TableInfo.Index> _indicesCallLogs = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCallLogs = new TableInfo("call_logs", _columnsCallLogs, _foreignKeysCallLogs, _indicesCallLogs);
        final TableInfo _existingCallLogs = TableInfo.read(connection, "call_logs");
        if (!_infoCallLogs.equals(_existingCallLogs)) {
          return new RoomOpenDelegate.ValidationResult(false, "call_logs(com.phantomnet.core.database.entity.CallLogEntity).\n"
                  + " Expected:\n" + _infoCallLogs + "\n"
                  + " Found:\n" + _existingCallLogs);
        }
        return new RoomOpenDelegate.ValidationResult(true, null);
      }
    };
    return _openDelegate;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final Map<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final Map<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "personas", "conversations", "messages", "rooms", "call_logs");
  }

  @Override
  public void clearAllTables() {
    super.performClear(true, "personas", "conversations", "messages", "rooms", "call_logs");
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final Map<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(PersonaDao.class, PersonaDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ConversationDao.class, ConversationDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(MessageDao.class, MessageDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(RoomDao.class, RoomDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CallLogDao.class, CallLogDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final Set<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
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
  public PersonaDao personaDao() {
    if (_personaDao != null) {
      return _personaDao;
    } else {
      synchronized(this) {
        if(_personaDao == null) {
          _personaDao = new PersonaDao_Impl(this);
        }
        return _personaDao;
      }
    }
  }

  @Override
  public ConversationDao conversationDao() {
    if (_conversationDao != null) {
      return _conversationDao;
    } else {
      synchronized(this) {
        if(_conversationDao == null) {
          _conversationDao = new ConversationDao_Impl(this);
        }
        return _conversationDao;
      }
    }
  }

  @Override
  public MessageDao messageDao() {
    if (_messageDao != null) {
      return _messageDao;
    } else {
      synchronized(this) {
        if(_messageDao == null) {
          _messageDao = new MessageDao_Impl(this);
        }
        return _messageDao;
      }
    }
  }

  @Override
  public RoomDao roomDao() {
    if (_roomDao != null) {
      return _roomDao;
    } else {
      synchronized(this) {
        if(_roomDao == null) {
          _roomDao = new RoomDao_Impl(this);
        }
        return _roomDao;
      }
    }
  }

  @Override
  public CallLogDao callLogDao() {
    if (_callLogDao != null) {
      return _callLogDao;
    } else {
      synchronized(this) {
        if(_callLogDao == null) {
          _callLogDao = new CallLogDao_Impl(this);
        }
        return _callLogDao;
      }
    }
  }
}
