package com.phantomnet.app.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.phantomnet.app.ui.call.CallOrchestratorScreen
import com.phantomnet.app.ui.call.PreCallPrivacyPanel
import com.phantomnet.app.ui.call.PrivacyMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    contactName: String,
    messages: List<Message>,
    onSendMessage: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    var showPreCallPanel by remember { mutableStateOf(false) }
    var isVideoCallRequest by remember { mutableStateOf(false) }
    
    // Call orchestration local override
    var activeCallMode by remember { mutableStateOf<PrivacyMode?>(null) }
    var activeCallIsVideo by remember { mutableStateOf(false) }

    if (activeCallMode != null) {
        CallOrchestratorScreen(
            contactName = contactName,
            initialMode = activeCallMode!!,
            isVideo = activeCallIsVideo,
            onEndCall = { activeCallMode = null }
        )
        return // Early return block underlying Chat rendering
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(contactName) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground, titleContentColor = Color.White, navigationIconContentColor = HackerGreen),
                actions = {
                    IconButton(onClick = { 
                        isVideoCallRequest = false
                        showPreCallPanel = true 
                    }) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Audio Call", tint = HackerGreen)
                    }
                    IconButton(onClick = { 
                        isVideoCallRequest = true
                        showPreCallPanel = true 
                    }) {
                        Icon(imageVector = Icons.Default.Videocam, contentDescription = "Video Call", tint = HackerGreen)
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = Color.Black) {
                TextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Encrypted Message...", color = Color.Gray) },
                    modifier = Modifier.weight(1f).padding(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Black,
                        unfocusedContainerColor = Color.Black,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = HackerGreen,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            onSendMessage(messageText)
                            messageText = ""
                        }
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = HackerGreen)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black),
            reverseLayout = true
        ) {
            items(messages) { message ->
                MessageBubble(message = message)
            }
        }
    }

    if (showPreCallPanel) {
        PreCallPrivacyPanel(
            contactName = contactName,
            isVideoCall = isVideoCallRequest,
            onConfirm = { privacyMode ->
                showPreCallPanel = false
                activeCallMode = privacyMode
                activeCallIsVideo = isVideoCallRequest
            },
            onCancel = { showPreCallPanel = false }
        )
    }
}

@Composable
fun MessageBubble(message: Message) {
    val align = if (message.isMe) Alignment.End else Alignment.Start
    val color = if (message.isMe) HackerGreen else Color.DarkGray
    val textColor = if (message.isMe) Color.Black else Color.White
    val shape = if (message.isMe) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = align
    ) {
        Box(
            modifier = Modifier
                .background(color, shape)
                .padding(12.dp)
        ) {
            Text(
                text = message.content,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp)),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
