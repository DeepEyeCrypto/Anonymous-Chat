package com.phantomnet.app.ui.call

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.phantomnet.app.ui.theme.DarkBackground
import com.phantomnet.app.ui.theme.HackerGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteDetailsBottomSheet(
    privacyMode: PrivacyMode,
    latencyMs: Int,
    packetLossPct: Float,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                "Call Route & Security",
                style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Current Mode Details
            RouteDetailSection(
                icon = Icons.Default.Security,
                title = "Privacy Mode: ${privacyMode.name}",
                value = getModeDescription(privacyMode),
                color = HackerGreen
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Route Class
            RouteDetailSection(
                icon = Icons.Default.Info,
                title = "Transport Route",
                value = getRouteClass(privacyMode),
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Connection Quality Metrics
            RouteDetailSection(
                icon = Icons.Default.SignalCellularAlt,
                title = "Connection Quality",
                value = "Latency: ${latencyMs}ms\nPacket Loss: ${packetLossPct}%",
                color = Color.Cyan
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun RouteDetailSection(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(value, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun getModeDescription(mode: PrivacyMode) = when (mode) {
    PrivacyMode.FAST -> "Direct P2P connectivity allows lower latency but exposes your IP address to the peer."
    PrivacyMode.PRIVATE -> "Traffic is relayed through Phantom nodes. Your IP is hidden."
    PrivacyMode.PARANOID -> "Signaling and media are routed through a multi-hop mixnet."
}

private fun getRouteClass(mode: PrivacyMode) = when (mode) {
    PrivacyMode.FAST -> "Direct (ICE/STUN)"
    PrivacyMode.PRIVATE -> "Phantom Relay (TURN)"
    PrivacyMode.PARANOID -> "Onion/Mixnet Overlay"
}
