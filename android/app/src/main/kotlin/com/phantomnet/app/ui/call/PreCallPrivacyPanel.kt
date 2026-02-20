package com.phantomnet.app.ui.call

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.phantomnet.app.ui.theme.DarkBackground
import com.phantomnet.app.ui.theme.HackerGreen

enum class PrivacyMode {
    FAST, PRIVATE, PARANOID
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreCallPrivacyPanel(
    contactName: String,
    initialMode: PrivacyMode = PrivacyMode.PRIVATE,
    isVideoCall: Boolean,
    onConfirm: (PrivacyMode) -> Unit,
    onCancel: () -> Unit
) {
    var selectedMode by remember { mutableStateOf(initialMode) }

    ModalBottomSheet(
        onDismissRequest = onCancel,
        containerColor = DarkBackground,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.DarkGray) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Secure ${if (isVideoCall) "Video" else "Audio"} Call",
                style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
            )
            Text(
                text = "with $contactName",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Mode Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ModeOption(
                    title = "Fast",
                    mode = PrivacyMode.FAST,
                    selected = selectedMode == PrivacyMode.FAST,
                    onSelect = { selectedMode = PrivacyMode.FAST },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                ModeOption(
                    title = "Private",
                    mode = PrivacyMode.PRIVATE,
                    selected = selectedMode == PrivacyMode.PRIVATE,
                    onSelect = { selectedMode = PrivacyMode.PRIVATE },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                ModeOption(
                    title = "Paranoid",
                    mode = PrivacyMode.PARANOID,
                    selected = selectedMode == PrivacyMode.PARANOID,
                    onSelect = { selectedMode = PrivacyMode.PARANOID },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Warning Banner
            WarningBanner(mode = selectedMode)

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Button(
                onClick = { onConfirm(selectedMode) },
                colors = ButtonDefaults.buttonColors(containerColor = HackerGreen),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Start ${if (isVideoCall) "Video" else "Audio"} Call", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel", color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ModeOption(
    title: String,
    mode: PrivacyMode,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) HackerGreen else Color.DarkGray
    val bgColor = if (selected) HackerGreen.copy(alpha = 0.1f) else Color.Transparent
    val textColor = if (selected) HackerGreen else Color.Gray

    Box(
        modifier = modifier
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .background(bgColor, RoundedCornerShape(12.dp))
            .clickable { onSelect() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(title, color = textColor, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun WarningBanner(mode: PrivacyMode) {
    val (icon, title, desc, color) = when (mode) {
        PrivacyMode.FAST -> listOf(
            Icons.Default.Warning,
            "IP Exposure Risk",
            "Fastest quality, but peer may see your IP address through direct P2P connection.",
            Color(0xFFFF9800)
        )
        PrivacyMode.PRIVATE -> listOf(
            Icons.Default.Info,
            "Relayed Privacy",
            "Better anonymity with moderate delay. Media is routed through Phantom relays.",
            HackerGreen
        )
        PrivacyMode.PARANOID -> listOf(
            Icons.Default.Info,
            "Maximum Anonymity",
            "Highest latency. Both signaling and media wrapped in multi-hop Mixnet overlay.",
            Color(0xFF9C27B0)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = (color as Color).copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon as androidx.compose.ui.graphics.vector.ImageVector,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title as String, color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(desc as String, color = Color.LightGray, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
