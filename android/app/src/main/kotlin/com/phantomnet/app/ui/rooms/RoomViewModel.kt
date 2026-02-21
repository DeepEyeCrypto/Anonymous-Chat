package com.phantomnet.app.ui.rooms

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.phantomnet.core.database.model.Message
import com.phantomnet.core.database.repository.RoomRepository
import com.phantomnet.core.identity.IdentityManager
import com.phantomnet.core.PhantomCore
import com.phantomnet.core.network.MailboxManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RoomViewModel(application: Application) : AndroidViewModel(application) {
    private val identityManager = IdentityManager.getInstance(application)
    private var repository: RoomRepository? = null

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _roomName = MutableStateFlow("Secret Room")
    val roomName: StateFlow<String> = _roomName.asStateFlow()

    fun initRoom(roomId: String) {
        val db = identityManager.getDatabase() ?: return
        val repo = RoomRepository(db)
        repository = repo

        viewModelScope.launch {
            repo.getMessages(roomId).collect {
                _messages.value = it
            }
        }
        
        // Background polling for DC-Net contributions
        viewModelScope.launch {
            while (true) {
                pollRoomContributions(roomId)
                kotlinx.coroutines.delay(10000) // Poll every 10s
            }
        }
    }

    fun sendMessage(roomId: String, text: String) {
        val repo = repository ?: return
        viewModelScope.launch {
            // For MVP, myId is hardcoded to 0. 
            // Real apps would have participants perform a handshake to determine IDs 0..N-1
            val contribution = PhantomCore.computeDcNetContributionSafe(0, text)
            MailboxManager.postRoomContribution(roomId, 0, contribution)
            repo.insertSentMessage(roomId, text)
        }
    }

    private fun pollRoomContributions(roomId: String) {
        viewModelScope.launch {
            // 1. Fetch from DHT (P2P Mesh/Mixnet)
            MailboxManager.fetchRoomContributions(roomId, 3) 
            
            // 2. Poll the local cache for the latest round
            val contribs = MailboxManager.pollRoomContributions(roomId, 3)
            
            if (contribs.size >= 3) {
                // 3. Aggregate contributions via native DC-Net engine
                val revealed = PhantomCore.aggregateDcNetContributionsSafe(contribs.toTypedArray())
                
                if (revealed.isNotBlank() && revealed.startsWith("HELLO")) {
                    // Valid protocol message revealed!
                    repository?.insertRevealedMessage(roomId, revealed)
                }
            }
        }
    }
}
