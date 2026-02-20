package com.phantomnet.app.ui.rooms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.phantomnet.app.ui.theme.HackerGreen
import com.phantomnet.app.ui.theme.DarkBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DcNetRoomScreen(
    roomName: String,
    roomId: String = "demo_room",
    viewModel: RoomViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val messages by viewModel.messages.collectAsState()

    LaunchedEffect(roomId) {
        viewModel.initRoom(roomId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(roomName, color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Text("Untraceable DC-Net Mode", color = HackerGreen, style = MaterialTheme.typography.labelSmall)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Room Members (Mock participants)
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .padding(2.dp)
                            .background(HackerGreen, CircleShape)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            // Message Display
            Surface(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1C1F26)
            ) {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.padding(12.dp),
                    reverseLayout = true
                ) {
                    items(messages.size) { index ->
                        val msg = messages[index]
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(
                                text = if (msg.isMe) "My Contribution" else "Peer Broadcast",
                                color = HackerGreen,
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = msg.content,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Message Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Write untraceably...") },
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color(0xFF1C1F26),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = HackerGreen
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendMessage(roomId, messageText)
                            messageText = ""
                        }
                    },
                    modifier = Modifier.background(HackerGreen, CircleShape)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.Black)
                }
            }
        }
    }
}
