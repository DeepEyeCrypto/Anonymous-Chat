@file:OptIn(ExperimentalFoundationApi::class)
package com.phantomnet.feature.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val Emerald = Color(0xFF00E676)
private val Obsidian = Color(0xFF0B0E11)
private val SurfaceCard = Color(0xFF1C1F26)
private val TextGray = Color(0xFF8B949E)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    creationState: IdentityCreationState,
    onGenerateIdentity: () -> Unit,
    onImport: () -> Unit,
    onSkip: () -> Unit,
    onComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    // Auto-navigate to main when identity is created
    LaunchedEffect(creationState) {
        if (creationState is IdentityCreationState.Created) {
            kotlinx.coroutines.delay(1200) // brief pause to show fingerprint
            onComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> WelcomePage()
                1 -> PrivacyPromisePage()
                2 -> IdentityCreationPage(
                    state = creationState,
                    onGenerate = onGenerateIdentity,
                    onImport = onImport
                )
            }
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == index) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index) Emerald
                                else TextGray.copy(alpha = 0.4f)
                            )
                    )
                }
            }

            // Next / Enter button
            if (pagerState.currentPage < 2) {
                Button(
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald,
                        contentColor = Obsidian
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "Next →",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Skip button (pages 0-1 only)
            if (pagerState.currentPage < 2) {
                TextButton(onClick = onSkip) {
                    Text("Skip", color = TextGray, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun WelcomePage() {
    val infiniteTransition = rememberInfiniteTransition(label = "shield")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.6f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "glow"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Shield with glow
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Emerald.copy(alpha = glowAlpha * 0.3f),
                                Color.Transparent
                            ),
                            radius = 300f
                        )
                    )
            )
            Text("🛡️", fontSize = 80.sp)
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            "Messages Without",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(
            "Metadata",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Emerald,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Your conversations leave no trace.\nNo phone number. No email. No servers.",
            fontSize = 16.sp,
            color = TextGray,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(120.dp)) // room for controls
    }
}

@Composable
private fun PrivacyPromisePage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🔐", fontSize = 72.sp)

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Your Keys.",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(
            "Your Rules.",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Emerald,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Feature cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                FeatureRow("🔐", "End-to-End Encrypted")
                Spacer(modifier = Modifier.height(16.dp))
                FeatureRow("🌐", "Zero Servers")
                Spacer(modifier = Modifier.height(16.dp))
                FeatureRow("👻", "Deniable Identities")
                Spacer(modifier = Modifier.height(16.dp))
                FeatureRow("🧅", "Onion Routed")
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
private fun FeatureRow(emoji: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, fontSize = 24.sp)
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun IdentityCreationPage(
    state: IdentityCreationState,
    onGenerate: () -> Unit,
    onImport: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fp_pulse")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "border"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Create Your Phantom",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(
            "Identity",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Emerald,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Fingerprint visualization card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(16.dp),
            border = when (state) {
                is IdentityCreationState.Created ->
                    CardDefaults.outlinedCardBorder().copy(
                        width = 2.dp,
                        brush = Brush.linearGradient(listOf(Emerald, Emerald.copy(alpha = borderAlpha)))
                    )
                else -> null
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (state) {
                    is IdentityCreationState.Idle -> {
                        Text(
                            "? ?  ? ?  ? ?  ? ?",
                            fontSize = 20.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextGray,
                            letterSpacing = 2.sp
                        )
                    }
                    is IdentityCreationState.Generating -> {
                        CircularProgressIndicator(
                            color = Emerald,
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp
                        )
                    }
                    is IdentityCreationState.Created -> {
                        Text(
                            state.fingerprint,
                            fontSize = 20.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Emerald,
                            letterSpacing = 2.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    is IdentityCreationState.Error -> {
                        Text(
                            "Generation failed",
                            fontSize = 16.sp,
                            color = Color(0xFFFF5252)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Kyber-768 + X25519",
                    fontSize = 12.sp,
                    color = TextGray,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Your identity exists only on this device.\nNo account. No email.",
            fontSize = 14.sp,
            color = TextGray,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        // CTA button
        Button(
            onClick = { if (state is IdentityCreationState.Idle || state is IdentityCreationState.Error) onGenerate() },
            enabled = state is IdentityCreationState.Idle || state is IdentityCreationState.Error,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Emerald,
                contentColor = Obsidian,
                disabledContainerColor = Emerald.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            when (state) {
                is IdentityCreationState.Generating -> {
                    CircularProgressIndicator(
                        color = Obsidian,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Generating Identity...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                is IdentityCreationState.Created -> {
                    Text("✓ Identity Created", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                is IdentityCreationState.Error -> {
                    Text("Retry →", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                else -> {
                    Text("Enter Phantom →", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        if (state is IdentityCreationState.Idle || state is IdentityCreationState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onImport,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Emerald),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Emerald)
            ) {
                Text("Sync from other device", fontWeight = FontWeight.Bold)
            }
        }
    }
}
