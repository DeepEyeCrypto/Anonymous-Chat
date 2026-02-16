package com.phantomnet.app.domain.model

data class Message(
    val id: String,
    val senderId: String,
    val content: String,
    val timestamp: Long,
    val isMe: Boolean,
    val isRead: Boolean = false,
    val isDelivered: Boolean = false
)

data class Conversation(
    val id: String,
    val contactName: String,
    val lastMessage: String,
    val lastMessageTimestamp: Long,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false
)
