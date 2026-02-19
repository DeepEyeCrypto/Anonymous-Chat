package com.phantomnet.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.phantomnet.core.database.model.Conversation
import com.phantomnet.core.database.repository.ConversationRepository
import com.phantomnet.core.identity.IdentityManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ConversationListViewModel(application: Application) : AndroidViewModel(application) {
    private val identityManager = IdentityManager.getInstance(application)
    private var repository: ConversationRepository? = null

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    init {
        val db = identityManager.getDatabase()
        if (db != null) {
            val repo = ConversationRepository(db)
            repository = repo
            
            viewModelScope.launch {
                repo.getConversations().collect { list ->
                    _conversations.value = list
                }
            }
        }
    }
}
