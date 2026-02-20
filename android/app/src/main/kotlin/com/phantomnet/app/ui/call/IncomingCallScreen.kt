package com.phantomnet.app.ui.call

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Videocam
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
fun IncomingCallScreen(
    callerName: String,
    privacyMode: PrivacyMode,
    isVideoCall: Boolean,
    isCallerVerified: Boolean,
    onAcceptRequest: (Boolean) -> Unit, // accept video if true, audio if false
    onDecline: () -> Unit
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

            Spacer(modifier = Modifier.height(32.dp))

            // Caller Identity Verification State
            Text(
                text = if (isCallerVerified) "Trusted Contact" else "Unverified Caller",
                color = if (isCallerVerified) HackerGreen else Color.Yellow,
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Avatar / Profile Glyph
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.DarkGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = callerName.firstOrNull()?.toString()?.uppercase() ?: "?",
                    color = Color.White,
                    style = MaterialTheme.typography.displayLarge
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Contact Name
            Text(
                text = callerName,
                style = MaterialTheme.typography.headlineMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Calling Status
            Text(
                text = if (isVideoCall) "Incoming Video Call..." else "Incoming Audio Call...",
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.LightGray)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mode requested by Caller
            ModeBadge(mode = privacyMode)

            Spacer(modifier = Modifier.height(64.dp))

            // Actions
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Decline Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FilledIconButton(
                        onClick = onDecline,
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.Red)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "Decline",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Text("Decline", color = Color.White, modifier = Modifier.padding(top = 8.dp))
                }

                if (isVideoCall) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        FilledIconButton(
                            onClick = { onAcceptRequest(false) },
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.DarkGray)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Accept Audio-Only",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text("Audio Only", color = Color.White, modifier = Modifier.padding(top = 8.dp))
                    }
                }

                // Accept Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FilledIconButton(
                        onClick = { onAcceptRequest(isVideoCall) },
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = HackerGreen)
                    ) {
                        Icon(
                            imageVector = if (isVideoCall) Icons.Default.Videocam else Icons.Default.Call,
                            contentDescription = "Accept",
                            tint = Color.Black,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Text("Accept", color = Color.White, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}
