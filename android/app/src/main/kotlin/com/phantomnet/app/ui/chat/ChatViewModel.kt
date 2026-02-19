package com.phantomnet.app.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.phantomnet.core.database.model.Message
import com.phantomnet.core.database.repository.ConversationRepository
import com.phantomnet.core.identity.IdentityManager
import com.phantomnet.core.network.MailboxManager
import com.phantomnet.core.crypto.SignalBridge
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val identityManager = IdentityManager.getInstance(application)
    private var repository: ConversationRepository? = null

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private var currentConversationId: String? = null

    init {
        val db = identityManager.getDatabase()
        if (db != null) {
            repository = ConversationRepository(db)
        }
    }

    fun loadMessages(conversationId: String) {
        currentConversationId = conversationId
        val repo = repository ?: return
        
        viewModelScope.launch {
            repo.getMessages(conversationId).collect { msgs ->
                _messages.value = msgs
            }
        }
    }

    fun sendMessage(text: String, recipientFingerprint: String) {
        val convId = currentConversationId ?: return
        val repo = repository ?: return

        viewModelScope.launch {
            // 1. Save to Local DB (Optimistic)
            repo.sendMessage(convId, text)

            // 2. Encrypt & Post to DHT Mailbox
            try {
                // In Phase 2, we use a simple Signal handshake or DH.
                // We'll use a placeholder key if the peer's raw key isn't fully ready.
                val encrypted = SignalBridge.encryptMessageSafe(text, "mock_recipient_key")
                MailboxManager.postMessage(recipientFingerprint, encrypted)
            } catch (e: Exception) {
                // Background retry logic would go here
            }
        }
    }
}
