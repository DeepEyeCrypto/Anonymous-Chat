package com.phantomnet.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.phantomnet.core.database.model.Conversation
import com.phantomnet.core.database.model.Room
import com.phantomnet.core.database.repository.ConversationRepository
import com.phantomnet.core.database.repository.RoomRepository
import com.phantomnet.core.identity.IdentityManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ConversationListViewModel(application: Application) : AndroidViewModel(application) {
    private val identityManager = IdentityManager.getInstance(application)
    private var convRepository: ConversationRepository? = null
    private var roomRepository: RoomRepository? = null

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms.asStateFlow()

    init {
        val db = identityManager.getDatabase()
        if (db != null) {
            val convRepo = ConversationRepository(db)
            val roomRepo = RoomRepository(db)
            convRepository = convRepo
            roomRepository = roomRepo
            
            viewModelScope.launch {
                convRepo.getConversations().collect { list ->
                    _conversations.value = list
                }
            }

            viewModelScope.launch {
                roomRepo.getRooms().collect { list ->
                    _rooms.value = list
                }
            }
        }
    }
}
