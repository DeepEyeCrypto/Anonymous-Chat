package com.phantomnet.app.ui.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Emerald = Color(0xFF00E676)
private val Obsidian = Color(0xFF0B0E11)
private val SurfaceCard = Color(0xFF1C1F26)
private val TextGray = Color(0xFF8B949E)

@Composable
fun AddContactScreen(
    onScanClick: () -> Unit,
    onShowMyQrClick: () -> Unit,
    onConnectClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var manualInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .padding(horizontal = 24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Text("←", color = Color.White, fontSize = 24.sp)
            }
            Text(
                "ADD CONTACT",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Emerald,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ActionCard(
                label = "Scan QR",
                emoji = "📷",
                modifier = Modifier.weight(1f),
                onClick = onScanClick
            )
            ActionCard(
                label = "My QR",
                emoji = "🆔",
                modifier = Modifier.weight(1f),
                onClick = onShowMyQrClick
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "MANUAL ENTRY",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextGray,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = manualInput,
            onValueChange = { manualInput = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Paste identity key or link...", color = TextGray.copy(alpha = 0.5f)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Emerald,
                unfocusedBorderColor = SurfaceCard,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Emerald,
                containerColor = SurfaceCard
            ),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { if (manualInput.isNotBlank()) onConnectClick(manualInput) },
            enabled = manualInput.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Emerald,
                contentColor = Obsidian,
                disabledContainerColor = Emerald.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Connect →", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Connecting to a peer initiates an X3DH key exchange over the DHT. Your identity remains anonymous to the rest of the network.",
            fontSize = 13.sp,
            color = TextGray,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun ActionCard(
    label: String,
    emoji: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                label,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}
