package com.phantomnet.app.ui.discover

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Emerald = Color(0xFF00E676)
private val Obsidian = Color(0xFF0B0E11)
private val SurfaceCard = Color(0xFF1C1F26)
private val TextGray = Color(0xFF8B949E)

@Composable
fun DiscoveryScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "DISCOVER",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Emerald,
            letterSpacing = 3.sp,
            modifier = Modifier.padding(top = 16.dp, bottom = 48.dp)
        )

        Spacer(modifier = Modifier.weight(0.3f))

        // Radar animation
        RadarAnimation(
            modifier = Modifier.size(160.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            "Private Discovery",
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
            "Find contacts without uploading your\naddress book, using Private Set\nIntersection (PSI).",
            fontSize = 15.sp,
            color = TextGray,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Info chips
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                InfoChip("🔐", "Zero-Knowledge Protocol")
                Spacer(modifier = Modifier.height(14.dp))
                InfoChip("📱", "Local-Only Computation")
                Spacer(modifier = Modifier.height(14.dp))
                InfoChip("👁", "No Server Sees Your Contacts")
            }
        }

        Spacer(modifier = Modifier.weight(0.5f))
    }
}

@Composable
private fun RadarAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "sweep"
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f, targetValue = 0.4f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "ring"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxRadius = size.minDimension / 2

        // Concentric rings
        listOf(0.3f, 0.6f, 0.9f).forEach { ratio ->
            drawCircle(
                color = Emerald.copy(alpha = ringAlpha * 0.5f),
                radius = maxRadius * ratio,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Sweep line
        val radians = Math.toRadians(sweepAngle.toDouble())
        val endX = center.x + maxRadius * 0.9f * kotlin.math.cos(radians).toFloat()
        val endY = center.y + maxRadius * 0.9f * kotlin.math.sin(radians).toFloat()
        drawLine(
            color = Emerald.copy(alpha = 0.6f),
            start = center,
            end = Offset(endX, endY),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Center dot
        drawCircle(color = Emerald, radius = 4.dp.toPx(), center = center)
    }
}

@Composable
private fun InfoChip(emoji: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(14.dp))
        Text(label, fontSize = 14.sp, color = Color.White)
    }
}
