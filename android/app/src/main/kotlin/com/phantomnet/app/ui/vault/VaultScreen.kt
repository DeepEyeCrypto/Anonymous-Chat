package com.phantomnet.app.ui.vault

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import com.phantomnet.core.database.model.Persona
import kotlinx.coroutines.flow.Flow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

private val Emerald = Color(0xFF00E676)
private val Obsidian = Color(0xFF0B0E11)
private val TextGray = Color(0xFF8B949E)

@Composable
fun VaultScreen(
    identityStream: Flow<Persona?>?,
    onWipeClick: () -> Unit,
    onBackupClick: () -> Unit
) {
    val persona by identityStream?.collectAsState(initial = null) ?: remember { mutableStateOf(null) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "vault")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f, targetValue = 1.02f,
        animationSpec = infiniteRepeatable(tween(3000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "SECURE VAULT",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Emerald,
            letterSpacing = 3.sp,
            modifier = Modifier.padding(top = 24.dp, bottom = 40.dp)
        )

        // Identity Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(pulseScale)
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Emerald.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🛡️", fontSize = 32.sp)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Master Identity",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                
                Text(
                    persona?.fingerprint ?: "Initializing...",
                    color = Emerald,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    VaultStat(label = "Protocol", value = "v2.0")
                    VaultStat(label = "KEM", value = "ML-KEM (Kyber)")
                    VaultStat(label = "Sign", value = "Dilithium x ED")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Security Audit Quick Access
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* This should navigate to privacy dashboard if possible, but it's a tab usually */ },
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2128)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Emerald.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📊", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Privacy Health Audit", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Analyze transport & metadata leaks", color = TextGray, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action Menu
        Text(
            "IDENTITY MANAGEMENT",
            color = TextGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, bottom = 8.dp)
        )

        VaultActionItem(
            title = "Export Root Identity",
            subtitle = "Backup your master secret and keys",
            emoji = "🔑",
            onClick = onBackupClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        VaultActionItem(
            title = "Panic Protocol",
            subtitle = "Instantly wipe all data and keys",
            emoji = "☢️",
            tint = Color(0xFFFF5252),
            onClick = onWipeClick
        )

        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            "Phantom Net Vault is local-only.\nYour keys never touch the cloud.",
            color = TextGray.copy(alpha = 0.6f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
    }
}

@Composable
private fun VaultStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextGray, fontSize = 11.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun VaultActionItem(
    title: String,
    subtitle: String,
    emoji: String,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 20.sp)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(title, color = tint, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(subtitle, color = TextGray, fontSize = 12.sp)
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text("→", color = TextGray)
        }
    }
}
