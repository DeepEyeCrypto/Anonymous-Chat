package com.phantomnet.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.phantomnet.core.database.entity.PersonaEntity
import com.phantomnet.core.database.entity.ConversationEntity
import com.phantomnet.core.database.entity.MessageEntity
import com.phantomnet.core.database.entity.RoomEntity
import com.phantomnet.core.database.entity.CallLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonaDao {

    @Query("SELECT * FROM personas WHERE isActive = 1 LIMIT 1")
    fun getActivePersona(): Flow<PersonaEntity?>

    @Query("SELECT COUNT(*) > 0 FROM personas")
    suspend fun hasAnyPersona(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(persona: PersonaEntity)

    @Query("DELETE FROM personas")
    suspend fun deleteAll()
}

@Dao
interface ConversationDao {

    @Query("SELECT * FROM conversations ORDER BY lastMessageTimestamp DESC")
    fun getAll(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    fun getById(id: String): Flow<ConversationEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(conversation: ConversationEntity)

    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :id")
    suspend fun markRead(id: String)

    @Query("""
        UPDATE conversations 
        SET lastMessagePreview = :preview, 
            lastMessageTimestamp = :timestamp,
            unreadCount = unreadCount + CASE WHEN :incrementUnread THEN 1 ELSE 0 END
        WHERE id = :conversationId
    """)
    suspend fun updateLastMessage(
        conversationId: String,
        preview: String,
        timestamp: Long,
        incrementUnread: Boolean
    )

    @Query("DELETE FROM conversations")
    suspend fun deleteAll()
}

@Dao
interface RoomDao {
    @Query("SELECT * FROM rooms ORDER BY lastMessageTimestamp DESC")
    fun getAll(): Flow<List<RoomEntity>>

    @Query("SELECT * FROM rooms WHERE id = :id")
    fun getById(id: String): Flow<RoomEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(room: RoomEntity)

    @Query("DELETE FROM rooms")
    suspend fun deleteAll()
}

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE conversationId = :id OR conversationId = :id ORDER BY timestamp DESC")
    fun getForContext(id: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversationId = :convId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentForConversation(convId: String, limit: Int): Flow<List<MessageEntity>>

    @Insert
    suspend fun insert(message: MessageEntity)

    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    suspend fun updateStatus(messageId: String, status: String)

    @Query("DELETE FROM messages WHERE conversationId = :convId")
    suspend fun deleteForConversation(convId: String)

    @Query("DELETE FROM messages WHERE expiresAt IS NOT NULL AND expiresAt < :now")
    suspend fun deleteExpired(now: Long = System.currentTimeMillis())

    @Query("DELETE FROM messages")
    suspend fun deleteAll()
}

@Dao
interface CallLogDao {
    @Query("SELECT * FROM call_logs WHERE conversationId = :convId ORDER BY timestamp DESC")
    fun getLogsForConversation(convId: String): Flow<List<CallLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLog(log: CallLogEntity)

    @Query("UPDATE call_logs SET durationSec = :durationSec, outcome = :outcome WHERE sessionId = :sessionId")
    suspend fun updateCallEnd(sessionId: String, durationSec: Int, outcome: String)

    @Query("DELETE FROM call_logs")
    suspend fun deleteAll()
}
