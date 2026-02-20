package com.phantomnet.app.ui.backup

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
fun ShardWizardScreen(
    onBackClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val identityManager = remember { com.phantomnet.core.identity.IdentityManager.getInstance(context) }
    
    var threshold by remember { mutableStateOf(3f) }
    var totalShards by remember { mutableStateOf(5f) }
    var resultText by remember { mutableStateOf("Ready to shard your identity root...") }
    var isGenerating by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("SHARD WIZARD", fontWeight = FontWeight.Bold, letterSpacing = 2.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF0B0E11),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0B0E11)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color(0xFF1C1F26),
                shape = RoundedCornerShape(16.dp),
                border = org.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Threshold Cryptography",
                        color = Color(0xFF00E676),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Split your Master Persona Key into multiple shards. Recover your identity only when a threshold of shards are reunited.",
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Threshold Slider
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "REQUIRED SHARDS", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(text = threshold.toInt().toString(), color = Color(0xFF00E676), fontWeight = FontWeight.Bold)
            }
            Slider(
                value = threshold,
                onValueChange = { threshold = it },
                valueRange = 1f..totalShards,
                steps = (totalShards - 1).toInt(),
                colors = SliderDefaults.colors(activeTrackColor = Color(0xFF00E676), thumbColor = Color(0xFF00E676))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Total Slider
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "TOTAL SHARDS", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(text = totalShards.toInt().toString(), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = totalShards,
                onValueChange = { 
                    totalShards = it
                    if (threshold > totalShards) threshold = totalShards
                },
                valueRange = 2f..10f,
                steps = 8,
                colors = SliderDefaults.colors(activeTrackColor = Color.White, thumbColor = Color.White)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    val rootKey = identityManager.rootKey
                    if (rootKey != null) {
                        isGenerating = true
                        resultText = com.phantomnet.core.PhantomCore.splitSecretSafe(
                            rootKey,
                            threshold.toInt(),
                            totalShards.toInt()
                        )
                        isGenerating = false
                    } else {
                        resultText = "Error: No root identity found to shard."
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Text("GENERATE SHARDS", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1C1F26),
                border = org.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Text(
                    text = resultText,
                    modifier = Modifier.padding(16.dp),
                    color = if (resultText.startsWith("Error")) Color.Red else Color(0xFF00E676),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            TextButton(onClick = onBackClick) {
                Text("CLOSE WIZARD", color = Color.Gray, fontWeight = FontWeight.Bold)
            }
        }
    }
}
