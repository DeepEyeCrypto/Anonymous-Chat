package com.phantomnet.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onReady: (hasPersona: Boolean) -> Unit,
    splashState: OnboardingState
) {
    var showText by remember { mutableStateOf(false) }
    var showSubtext by remember { mutableStateOf(false) }

    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseAnim.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val glowAlpha by pulseAnim.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    LaunchedEffect(Unit) {
        delay(400)
        showText = true
        delay(600)
        showSubtext = true
    }

    LaunchedEffect(splashState) {
        if (splashState is OnboardingState.Ready) {
            delay(800) // let animation play
            onReady(splashState.hasPersona)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0E11)),
        contentAlignment = Alignment.Center
    ) {
        // Emerald radial glow
        Box(
            modifier = Modifier
                .size(280.dp)
                .scale(pulseScale)
                .alpha(glowAlpha)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00E676).copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        radius = 400f
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Phantom mask icon (text placeholder)
            Text(
                text = "👻",
                fontSize = 72.sp,
                modifier = Modifier.scale(pulseScale)
            )

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = showText,
                enter = fadeIn(tween(600)),
                exit = fadeOut()
            ) {
                Text(
                    text = "PHANTOM NET",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E676),
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(
                visible = showSubtext,
                enter = fadeIn(tween(600)),
                exit = fadeOut()
            ) {
                Text(
                    text = "messages without metadata",
                    fontSize = 14.sp,
                    color = Color(0xFF8B949E),
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Loading dots
            AnimatedVisibility(
                visible = showSubtext,
                enter = fadeIn(tween(400)),
                exit = fadeOut()
            ) {
                LoadingDots()
            }
        }
    }
}

@Composable
private fun LoadingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600), repeatMode = RepeatMode.Reverse),
        label = "d1"
    )
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(600, delayMillis = 200), repeatMode = RepeatMode.Reverse
        ),
        label = "d2"
    )
    val alpha3 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(600, delayMillis = 400), repeatMode = RepeatMode.Reverse
        ),
        label = "d3"
    )

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(alpha1, alpha2, alpha3).forEach { a ->
            Text(
                "●",
                color = Color(0xFF00E676).copy(alpha = a),
                fontSize = 10.sp
            )
        }
    }
}
