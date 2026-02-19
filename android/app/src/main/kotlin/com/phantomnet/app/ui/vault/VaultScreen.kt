package com.phantomnet.app.ui.vault

import androidx.compose.animation.core.*
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

private val Emerald = Color(0xFF00E676)
private val Obsidian = Color(0xFF0B0E11)
private val TextGray = Color(0xFF8B949E)

@Composable
fun VaultScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "vault")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "pulse"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f, targetValue = 0.4f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glow"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "VAULT",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Emerald,
            letterSpacing = 3.sp,
            modifier = Modifier.padding(top = 16.dp, bottom = 48.dp)
        )

        Spacer(modifier = Modifier.weight(0.4f))

        // Lock icon with glow
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(pulseScale)
                    .alpha(glowAlpha)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Emerald.copy(alpha = 0.2f), Color.Transparent),
                            radius = 300f
                        )
                    )
            )
            Text("🔒", fontSize = 72.sp, modifier = Modifier.scale(pulseScale))
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            "Encrypted Vault",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(
            "Coming Soon",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Emerald,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Store files, keys, and credentials\nin an encrypted local vault.\nZero cloud.",
            fontSize = 15.sp,
            color = TextGray,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.weight(0.6f))
    }
}
