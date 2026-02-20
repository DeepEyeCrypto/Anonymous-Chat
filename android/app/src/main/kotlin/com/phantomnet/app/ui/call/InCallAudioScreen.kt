package com.phantomnet.app.ui.call

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.phantomnet.app.ui.theme.DarkBackground
import com.phantomnet.app.ui.theme.HackerGreen

@Composable
fun InCallAudioScreen(
    contactName: String,
    callDuration: String,
    privacyMode: PrivacyMode,
    qualityState: String = "Good",
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onVideoToggleRequest: () -> Unit,
    onEndCall: () -> Unit,
    onMoreActions: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar Elements
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Privacy Badge
                ModeBadge(mode = privacyMode)
                
                // Quality Chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    val qualityColor = if (qualityState == "Good") HackerGreen else Color.Yellow
                    Box(modifier = Modifier.size(8.dp).background(qualityColor, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = qualityState, color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
            }

            // Participant Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(Color.DarkGray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contactName.firstOrNull()?.toString()?.uppercase() ?: "?",
                        color = Color.White,
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = contactName,
                    style = MaterialTheme.typography.headlineMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = callDuration,
                    style = MaterialTheme.typography.titleMedium.copy(color = HackerGreen)
                )
            }

            // Bottom Control Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CallControlButton(
                    icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    isActive = isMuted,
                    onClick = onMuteToggle
                )
                
                CallControlButton(
                    icon = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                    isActive = isSpeakerOn,
                    onClick = onSpeakerToggle
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

                CallControlButton(
                    icon = Icons.Default.Videocam,
                    isActive = false,
                    onClick = onVideoToggleRequest
                )

                CallControlButton(
                    icon = Icons.Default.MoreHoriz,
                    isActive = false,
                    onClick = onMoreActions
                )
            }
        }
    }
}

@Composable
fun CallControlButton(
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    FilledIconButton(
        onClick = onClick,
        modifier = Modifier.size(56.dp),
        shape = CircleShape,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = if (isActive) Color.White else Color.DarkGray.copy(alpha = 0.5f),
            contentColor = if (isActive) Color.Black else Color.White
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}
