package com.phantomnet.core.database.repository

import com.phantomnet.core.database.PhantomDatabase
import com.phantomnet.core.database.entity.RoomEntity
import com.phantomnet.core.database.entity.MessageEntity
import com.phantomnet.core.database.model.Room
import com.phantomnet.core.database.model.Message
import com.phantomnet.core.network.MailboxManager
import com.phantomnet.core.PhantomCore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class RoomRepository(private val db: PhantomDatabase) {

    fun getRooms(): Flow<List<Room>> {
        return db.roomDao().getAll().map { entities ->
            entities.map { it.toModel() }
        }
    }

    fun getMessages(roomId: String): Flow<List<Message>> {
        return db.messageDao().getForContext(roomId).map { entities ->
            entities.map { it.toModel() }
        }
    }

    suspend fun createRoom(name: String, type: String): RoomEntity {
        val room = RoomEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            type = type,
            sharedSecretsJson = "{}",
            lastMessagePreview = "Room created",
            lastMessageTimestamp = System.currentTimeMillis(),
            unreadCount = 0,
            isActive = true
        )
        db.roomDao().upsert(room)
        return room
    }

    suspend fun sendDcNetMessage(roomId: String, myParticipantId: Int, text: String): String {
        // 1. Compute local contribution
        val contribution = PhantomCore.computeDcNetContributionSafe(myParticipantId, text)
        
        // 2. Post to DHT
        MailboxManager.postRoomContribution(roomId, myParticipantId, contribution)
        
        // 3. Save to local DB (pending)
        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = roomId,
            senderId = "me",
            contentPlaintext = text,
            contentCiphertext = null,
            timestamp = System.currentTimeMillis(),
            isMe = true,
            status = "SENT",
            expiresAt = System.currentTimeMillis() + 86400000 // 24h TTL
        )
        db.messageDao().upsert(message)
        
        return contribution
    }

    private fun RoomEntity.toModel() = Room(
        id = id,
        name = name,
        type = type,
        lastMessage = lastMessagePreview,
        lastMessageTimestamp = lastMessageTimestamp,
        unreadCount = unreadCount,
        isActive = isActive
    )

    private fun MessageEntity.toModel() = Message(
        id = id,
        senderId = senderId,
        content = contentPlaintext,
        timestamp = timestamp,
        isMe = isMe,
        status = com.phantomnet.app.domain.model.MessageStatus.valueOf(status)
    )
}
