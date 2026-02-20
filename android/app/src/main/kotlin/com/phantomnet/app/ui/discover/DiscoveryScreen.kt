package com.phantomnet.app.ui.discover

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun DiscoveryScreen(
    viewModel: DiscoveryViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by viewModel.state.collectAsState()
    val isScanning = state is DiscoveryState.Scanning

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

        Spacer(modifier = Modifier.weight(0.2f))

        // Radar animation
        RadarAnimation(
            modifier = Modifier.size(160.dp),
            isScanning = isScanning
        )

        Spacer(modifier = Modifier.height(40.dp))

        when (val s = state) {
            is DiscoveryState.Idle, is DiscoveryState.Scanning -> {
                Text(
                    if (isScanning) "Scanning P2P Network..." else "Private Discovery",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Find contacts via PSI",
                    fontSize = 18.sp,
                    color = Emerald,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Find contacts without uploading your\naddress book, using Zero-Knowledge\nPrivate Set Intersection.",
                    fontSize = 15.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = { viewModel.runPsiScan() },
                    enabled = !isScanning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isScanning) Emerald.copy(alpha = 0.2f) else Emerald,
                        contentColor = Obsidian
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(color = Obsidian, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("START PSI SCAN", fontWeight = FontWeight.Bold)
                    }
                }
            }
            is DiscoveryState.Success -> {
                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("SCAN COMPLETE", color = Emerald, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 2.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(s.message, color = Color.White, fontSize = 16.sp, lineHeight = 24.sp)
                        
                        if (s.matches > 0) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("✓ Found ${s.matches} potential contacts", color = Emerald, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.reset() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Emerald),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("SEARCH AGAIN", color = Emerald)
                        }
                    }
                }
            }
            is DiscoveryState.Error -> {
                Text("Discovery Error", color = Color.Red, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(s.message, color = TextGray, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { viewModel.reset() }) {
                    Text("TRY AGAIN")
                }
            }
        }

        Spacer(modifier = Modifier.weight(0.5f))
    }
}

@Composable
private fun RadarAnimation(modifier: Modifier = Modifier, isScanning: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isScanning) 1000 else 3000, easing = LinearEasing)
        ),
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
