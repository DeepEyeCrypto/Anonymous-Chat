package com.phantomnet.app.ui.home

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phantomnet.app.domain.model.Conversation
import com.phantomnet.app.ui.theme.HackerGreen
import com.phantomnet.app.ui.theme.SignalPurple
import com.phantomnet.app.ui.theme.TorOnion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    conversations: List<Conversation>,
    torStatus: String,
    dhtStatus: String,
    meshStatus: String,
    onConversationClick: (String) -> Unit,
    onFabClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PHANTOM NET", fontWeight = FontWeight.Bold, color = HackerGreen) },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onFabClick,
                containerColor = HackerGreen,
                contentColor = Color.Black
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "New Chat")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            NetworkStatusBanner(torStatus, dhtStatus, meshStatus)
            
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(conversations) { conversation ->
                    ConversationItem(conversation = conversation, onClick = { onConversationClick(conversation.id) })
                    Divider(color = Color.DarkGray, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun NetworkStatusBanner(torStatus: String, dhtStatus: String, meshStatus: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray.copy(alpha = 0.5f))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatusPill(label = "TOR", status = torStatus, activeColor = TorOnion)
        StatusPill(label = "DHT", status = dhtStatus, activeColor = HackerGreen)
        StatusPill(label = "MESH", status = meshStatus, activeColor = Color.Blue)
    }
}

@Composable
fun StatusPill(label: String, status: String, activeColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (status.contains("Started") || status.contains("Connected") || status.contains("Scanning")) activeColor else Color.Red)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$label",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
fun ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    // ... item code ...
    // (Repeating item code or just using previously defined one if in same file context)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar Placeholder
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(if (conversation.isOnline) HackerGreen else Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = conversation.contactName.take(1).uppercase(),
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = conversation.contactName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = DateUtils.getRelativeTimeSpanString(conversation.lastMessageTimestamp).toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = conversation.lastMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = if (conversation.unreadCount > 0) Color.White else Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        if (conversation.unreadCount > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            Badge { Text(text = conversation.unreadCount.toString()) }
        }
    }
}
