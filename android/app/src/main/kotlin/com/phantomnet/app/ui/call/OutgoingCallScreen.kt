package com.phantomnet.app.ui.call

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.phantomnet.app.ui.theme.DarkBackground
import com.phantomnet.app.ui.theme.HackerGreen

@Composable
fun OutgoingCallScreen(
    contactName: String,
    privacyMode: PrivacyMode,
    isVideoCall: Boolean,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // End-to-End Encryption Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(HackerGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = HackerGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("End-to-End Encrypted", color = HackerGreen, style = MaterialTheme.typography.labelMedium)
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Avatar / Profile Glyph
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.DarkGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contactName.firstOrNull()?.toString()?.uppercase() ?: "?",
                    color = Color.White,
                    style = MaterialTheme.typography.displayLarge
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Contact Name
            Text(
                text = contactName,
                style = MaterialTheme.typography.headlineMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Calling Status
            Text(
                text = if (isVideoCall) "Requesting Video Call..." else "Calling...",
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.LightGray)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Selected Privacy Mode Badge
            ModeBadge(mode = privacyMode)

            Spacer(modifier = Modifier.height(64.dp))

            // Cancel Button
            FilledIconButton(
                onClick = onCancel,
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.Red)
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "Cancel Call",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
fun ModeBadge(mode: PrivacyMode) {
    val (label, color) = when (mode) {
        PrivacyMode.FAST -> "Fast Mode" to Color(0xFFFF9800)
        PrivacyMode.PRIVATE -> "Private Relay" to HackerGreen
        PrivacyMode.PARANOID -> "Paranoid Mixnet" to Color(0xFF9C27B0)
    }

    Box(
        modifier = Modifier
            .border(1.dp, color, RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = color, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}
