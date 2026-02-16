package com.phantomnet.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.phantomnet.app.domain.NetworkStatus
import com.phantomnet.app.domain.model.Conversation
import com.phantomnet.app.domain.model.Message
import com.phantomnet.app.ui.chat.ChatScreen
import com.phantomnet.app.ui.chat.ChatViewModel
import com.phantomnet.app.ui.home.ConversationListScreen
import com.phantomnet.app.ui.theme.PhantomNetTheme
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PhantomNetTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    // Network Status
    val torStatus by NetworkStatus.torStatus.collectAsState()
    val dhtStatus by NetworkStatus.dhtStatus.collectAsState()

    // Mock Data
    val conversations = remember {
        listOf(
            Conversation(
                id = "1",
                contactName = "Alice (Tor)",
                lastMessage = "Signal handshake complete. Identity verified.",
                lastMessageTimestamp = System.currentTimeMillis(),
                unreadCount = 2,
                isOnline = true
            ),
            Conversation(
                id = "2",
                contactName = "Bob (DHT)",
                lastMessage = "Can you send the architectural blueprints?",
                lastMessageTimestamp = System.currentTimeMillis() - 3600000,
                unreadCount = 0,
                isOnline = false
            )
        )
    }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            ConversationListScreen(
                conversations = conversations,
                torStatus = torStatus,
                dhtStatus = dhtStatus,
                onConversationClick = { id -> navController.navigate("chat/$id") },
                onFabClick = { /* TODO: New Chat */ }
            )
        }
        composable("chat/{conversationId}") { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId")
            val conversation = conversations.find { it.id == conversationId }
            
            // Instantiate ViewModel scoped to this nav entry
            val chatViewModel: ChatViewModel = viewModel()
            // Observe State
            val messages by chatViewModel.messages.collectAsState()

            if (conversation != null) {
                ChatScreen(
                    contactName = conversation.contactName,
                    messages = messages, // Real messages from VM
                    onSendMessage = { text -> 
                        chatViewModel.sendMessage(text, "mock_recipient_key")
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
