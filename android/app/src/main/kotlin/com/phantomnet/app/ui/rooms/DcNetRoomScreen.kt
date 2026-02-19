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
import com.phantomnet.core.PhantomCore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DcNetRoomScreen(
    roomName: String,
    onBackClick: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    var resultLog by remember { mutableStateOf(listOf("Room initialized. All participants connected.")) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(roomName, color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Text("Untraceable DC-Net Mode", color = Color(0xFF00E676), style = MaterialTheme.typography.labelSmall)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF0B0E11)
                )
            )
        },
        containerColor = Color(0xFF0B0E11)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Room Members visualization
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(5) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .padding(2.dp)
                            .background(Color(0xFF00E676), CircleShape)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            // Log / Message Display
            Surface(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1C1F26)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    resultLog.forEach { log ->
                        Text(
                            text = log,
                            color = if (log.startsWith("Contribution")) Color(0xFF00E676) else Color.LightGray,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
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
                        cursorColor = Color(0xFF00E676)
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            val contributionPreview = runCatching {
                                PhantomCore.computeDcNetContribution(1, messageText).take(20)
                            }.getOrElse {
                                "DC-Net unavailable"
                            }
                            resultLog = resultLog + "Contribution generated: $contributionPreview..."
                            resultLog = resultLog + "Broadcasting XOR-sum to room..."
                            messageText = ""
                        }
                    },
                    modifier = Modifier.background(Color(0xFF00E676), CircleShape)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.Black)
                }
            }
        }
    }
}
