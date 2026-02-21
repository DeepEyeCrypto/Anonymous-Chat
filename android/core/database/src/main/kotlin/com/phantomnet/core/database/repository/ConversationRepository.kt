package com.phantomnet.core.database.repository

import com.phantomnet.core.database.PhantomDatabase
import com.phantomnet.core.database.entity.ConversationEntity
import com.phantomnet.core.database.entity.MessageEntity
import com.phantomnet.core.database.model.Conversation
import com.phantomnet.core.database.model.Message
import com.phantomnet.core.database.model.MessageStatus
import com.phantomnet.core.database.model.RoutingMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class ConversationRepository(private val db: PhantomDatabase) {

    fun getConversations(): Flow<List<Conversation>> {
        return db.conversationDao().getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getMessages(conversationId: String): Flow<List<Message>> {
        return db.messageDao().getForContext(conversationId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun sendMessage(conversationId: String, content: String) {
        val now = System.currentTimeMillis()
        val messageId = UUID.randomUUID().toString()
        
        val message = MessageEntity(
            id = messageId,
            conversationId = conversationId,
            senderId = "me",
            contentPlaintext = content,
            contentCiphertext = null,
            timestamp = now,
            isMe = true,
            status = "SENDING",
            expiresAt = null
        )

        db.messageDao().insert(message)
        db.conversationDao().updateLastMessage(conversationId, content, now, false)
    }

    fun getConversation(id: String): Flow<Conversation?> {
        return db.conversationDao().getById(id).map { it?.toDomain() }
    }

    suspend fun getConversationSync(id: String): Conversation? {
        return db.conversationDao().getByIdSync(id)?.toDomain()
    }

    suspend fun updateSharedSecret(conversationId: String, secret: ByteArray) {
        db.conversationDao().updateSharedSecret(conversationId, secret)
    }

    private fun ConversationEntity.toDomain() = Conversation(
        id = id,
        contactName = contactName,
        contactFingerprint = contactFingerprint,
        lastMessage = lastMessagePreview,
        lastMessageTimestamp = lastMessageTimestamp,
        unreadCount = unreadCount,
        isOnline = isOnline,
        routingMode = RoutingMode.valueOf(routingMode),
        sharedSecret = sharedSecret
    )

    private fun MessageEntity.toDomain() = Message(
        id = id,
        senderId = senderId,
        content = contentPlaintext,
        timestamp = timestamp,
        isMe = isMe,
        status = MessageStatus.valueOf(status),
        expiresAt = expiresAt
    )
}
