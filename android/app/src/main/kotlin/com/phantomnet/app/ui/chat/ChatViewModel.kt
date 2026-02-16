package com.phantomnet.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phantomnet.app.domain.model.Message
import com.phantomnet.core.crypto.SignalBridge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    fun loadMessages(conversationId: String) {
        // TODO: Load from DB
        // For MVP, just initial mock data logic is in MainActivity for now, 
        // effectively this VM manages active session state.
    }

    fun sendMessage(text: String, recipientKey: String) {
        viewModelScope.launch {
            // 1. Encrypt (Native Rust Call)
            // In a real app, recipientKey would be their Identity Key + PreKeys.
            // Here we just pass a string to satisfy the signature.
            val encryptedBase64 = SignalBridge.encryptMessage(text, recipientKey)
            
            // 2. Add "My" message to UI (Optimistic update)
            val myMessage = Message(
                id = UUID.randomUUID().toString(),
                senderId = "me",
                content = text, // We show plaintext to ourselves
                timestamp = System.currentTimeMillis(),
                isMe = true,
                isDelivered = false
            )
            _messages.value += myMessage
            
            // 3. Simulation: Receive the encrypted message back (Echo loopback)
            // In reality, we'd send `encryptedBase64` via Tor/DHT here.
            simulateNetworkEcho(encryptedBase64)
        }
    }

    private fun simulateNetworkEcho(encryptedPayload: String) {
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000) // Network latency
            
            // 4. Decrypt (Native Rust Call)
            val decryptedText = SignalBridge.decryptMessage(encryptedPayload)
            
            val replyMessage = Message(
                id = UUID.randomUUID().toString(),
                senderId = "other",
                content = "Echo: $decryptedText", // Prove we decrypted it!
                timestamp = System.currentTimeMillis(),
                isMe = false
            )
            _messages.value += replyMessage
        }
    }
}
