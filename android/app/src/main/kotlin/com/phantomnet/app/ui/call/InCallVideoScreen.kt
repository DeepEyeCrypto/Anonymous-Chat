package com.phantomnet.app.ui.call

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.phantomnet.app.ui.theme.DarkBackground
import com.phantomnet.app.ui.theme.HackerGreen

@Composable
fun InCallVideoScreen(
    contactName: String,
    callDuration: String,
    privacyMode: PrivacyMode,
    isMuted: Boolean,
    isCameraOff: Boolean,
    onMuteToggle: () -> Unit,
    onCameraToggle: () -> Unit,
    onSwitchCamera: () -> Unit,
    onAudioOnlyFallback: () -> Unit,
    onEndCall: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Mock Remote Video Canvas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1E1E1E))
        ) {
            // Placeholder for actual video rendering view
            Text(
                "Remote Video Stream\n(Encryption Active)",
                color = Color.DarkGray,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Top Overlay Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModeBadge(mode = privacyMode)

            Text(
                text = "$contactName - $callDuration",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            )
        }

        // Mock Local PiP Window (Picture-in-Picture)
        if (!isCameraOff) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 80.dp, end = 24.dp)
                    .width(100.dp)
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, HackerGreen, RoundedCornerShape(12.dp))
                    .background(Color.DarkGray)
            ) {
                Text(
                    "Local\nPreview",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        // Bottom Toggles Overlay
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CallControlButton(
                icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                isActive = isMuted,
                onClick = onMuteToggle
            )

            CallControlButton(
                icon = if (isCameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                isActive = isCameraOff,
                onClick = onCameraToggle
            )

            CallControlButton(
                icon = Icons.Default.Cameraswitch,
                isActive = false,
                onClick = onSwitchCamera
            )

            CallControlButton(
                icon = Icons.Default.Call,
                isActive = false,
                onClick = onAudioOnlyFallback
            )

            FilledIconButton(
                onClick = onEndCall,
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.Red)
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}
