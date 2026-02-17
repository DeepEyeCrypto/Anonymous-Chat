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
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
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
            val context = LocalContext.current
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                listOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } else {
                listOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            }

            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { perms ->
                val allGranted = perms.values.all { it }
                if (allGranted) {
                    // Permissions granted
                } else {
                    Toast.makeText(context, "Bluetooth permissions required for Mesh", Toast.LENGTH_LONG).show()
                }
            }

            LaunchedEffect(Unit) {
                val missingPermissions = permissions.filter {
                    ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
                }
                if (missingPermissions.isNotEmpty()) {
                    launcher.launch(missingPermissions.toTypedArray())
                }
            }

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
    val meshStatus by NetworkStatus.meshStatus.collectAsState()

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
                meshStatus = meshStatus,
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
