package com.phantomnet.app.ui.privacy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phantomnet.core.PhantomCore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyDashboardScreen(
    onBackClick: () -> Unit,
    onBackupClick: () -> Unit
) {
    var chatPrivacyMode by remember { mutableStateOf(0f) }
    var selectedAdversaryIndex by remember { mutableStateOf(0) }
    
    // Audit Logic
    val auditReport = remember(chatPrivacyMode, selectedAdversaryIndex) {
        val adversary = when(selectedAdversaryIndex) {
            0 -> "LocalThief"
            1 -> "NetworkMonitor"
            else -> "GlobalNationState"
        }
        
        val configJson = """
            {
                "use_mixnet": ${chatPrivacyMode > 0.66f},
                "use_psi": true,
                "is_sharded": true,
                "cover_traffic_enabled": ${chatPrivacyMode > 0.8f},
                "selected_adversary": "$adversary"
            }
        """.trimIndent()
        
        val result = PhantomCore.runPrivacyAuditSafe(configJson)
        // Simplified parsing for MVP
        val scoreMatch = """"risk_score":(\d+)""".toRegex().find(result)
        val score = scoreMatch?.groupValues?.get(1)?.toInt() ?: 50
        val colorMatch = """"status_color":"(#[A-F0-9]+)"""".toRegex().find(result)
        val colorStr = colorMatch?.groupValues?.get(1) ?: "#FFD600"
        
        try {
            Pair(score, Color(android.graphics.Color.parseColor(colorStr)))
        } catch (_: Throwable) {
            Pair(50, Color.Yellow)
        }
    }

    val modeName = when {
        chatPrivacyMode < 0.33f -> "Fast (Onion)"
        chatPrivacyMode < 0.66f -> "Balanced"
        else -> "Paranoia (Mixnet)"
    }
    
    val modeColor = when {
        chatPrivacyMode < 0.33f -> Color(0xFF00E676) // Emerald
        chatPrivacyMode < 0.66f -> Color(0xFFFFD600) // Yellow
        else -> Color(0xFF00C853) // Deep Emerald
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Privacy Dashboard", color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF0B0E11)
                )
            )
        },
        containerColor = Color(0xFF0B0E11)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Privacy Slider Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1C1F26),
                tonalElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Privacy Mode",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = modeName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = modeColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Slider(
                        value = chatPrivacyMode,
                        onValueChange = { chatPrivacyMode = it },
                        colors = SliderDefaults.colors(
                            thumbColor = modeColor,
                            activeTrackColor = modeColor,
                            inactiveTrackColor = Color.Gray
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Tuning: Hops, Delays, and Cover Traffic",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Threat Sanity Checker Section
            Text(
                text = "Threat Sanity Checker",
                modifier = Modifier.align(Alignment.Start),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ThreatOptionCard(
                title = "Who is your primary adversary?",
                options = listOf("Local Thief", "ISP / Network Monitor", "Global Nation State"),
                selectedIndex = selectedAdversaryIndex,
                onOptionSelected = { selectedAdversaryIndex = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = auditReport.second.copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Privacy Audit Score",
                            style = MaterialTheme.typography.titleSmall,
                            color = auditReport.second
                        )
                        Text(
                            text = "${auditReport.first}/100",
                            style = MaterialTheme.typography.titleMedium,
                            color = auditReport.second,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (auditReport.first > 80) 
                            "Configuration hardened against selected threat." 
                            else "Warning: Current settings may leak metadata to this adversary.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onBackupClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1F26)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Configure Secure Backup (SSS)", color = Color.White)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    PhantomCore.triggerSentinelActionSafe(1)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("PANIC: SHRED IDENTITY", color = Color.White, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = modeColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Lock Settings", color = Color.Black)
            }
        }
    }
}

@Composable
fun ThreatOptionCard(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1C1F26)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, color = Color.White, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(12.dp))
            options.forEachIndexed { index, option ->
                OutlinedButton(
                    onClick = { onOptionSelected(index) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (selectedIndex == index) Color(0xFF00E676) else Color.LightGray,
                        containerColor = if (selectedIndex == index) Color(0xFF00E676).copy(alpha = 0.1f) else Color.Transparent
                    )
                ) {
                    Text(option)
                }
            }
        }
    }
}
