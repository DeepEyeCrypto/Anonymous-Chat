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
    var threshold by remember { mutableStateOf(3f) }
    var totalShards by remember { mutableStateOf(5f) }
    var resultText by remember { mutableStateOf("No shards generated yet.") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Shard Wizard", color = Color.White) },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Secure Sharded Backup",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Split your Persona Root Key into multiple cryptographic shards. Distribute them to friends or hardware keys to prevent single points of failure.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Threshold Slider
            Text(
                text = "Required Shards (Threshold): ${threshold.toInt()}",
                color = Color(0xFF00E676),
                modifier = Modifier.align(Alignment.Start)
            )
            Slider(
                value = threshold,
                onValueChange = { threshold = it },
                valueRange = 1f..totalShards,
                steps = (totalShards - 1).toInt(),
                colors = SliderDefaults.colors(activeTrackColor = Color(0xFF00E676))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Total Slider
            Text(
                text = "Total Shards: ${totalShards.toInt()}",
                color = Color.White,
                modifier = Modifier.align(Alignment.Start)
            )
            Slider(
                value = totalShards,
                onValueChange = { 
                    totalShards = it
                    if (threshold > totalShards) threshold = totalShards
                },
                valueRange = 2f..10f,
                steps = 8,
                colors = SliderDefaults.colors(activeTrackColor = Color.White)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    resultText = PhantomCore.splitSecretSafe(
                        "PHANTOM_PRIVATE_KEY_PLAINTEXT_MOCK",
                        threshold.toInt(),
                        totalShards.toInt()
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Generate Shards", color = Color.Black)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1C1F26)
            ) {
                Text(
                    text = resultText,
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFF00E676),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            TextButton(onClick = onBackClick) {
                Text("Cancel", color = Color.Gray)
            }
        }
    }
}
