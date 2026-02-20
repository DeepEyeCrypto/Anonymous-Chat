package com.phantomnet.app.ui.privacy

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.phantomnet.app.ui.theme.DarkBackground
import com.phantomnet.app.ui.theme.HackerGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyDashboardScreen(
    viewModel: PrivacyDashboardViewModel = viewModel(),
    onBackClick: () -> Unit,
    onBackupClick: () -> Unit,
    onWipeComplete: () -> Unit
) {
    val auditState by viewModel.auditState.collectAsState()
    var showPanicDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PRIVACY AUDIT", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Security Score Gauge
            SecurityScoreGauge(state = auditState)

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { viewModel.runAudit() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = HackerGreen, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("START SCAN", fontWeight = FontWeight.Bold)
                }
                
                OutlinedButton(
                    onClick = onBackupClick,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, HackerGreen),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HackerGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("VAULT BACKUP")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Anon Sanity Checker (Threat Map)
            ThreatVectorMap(state = auditState)

            Spacer(modifier = Modifier.height(32.dp))

            // Vulnerability List
            AuditResultsSection(state = auditState)

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            // Panic Section
            PanicControlCard(onPanicClick = { showPanicDialog = true })
        }
    }

    if (showPanicDialog) {
        AlertDialog(
            onDismissRequest = { showPanicDialog = false },
            title = { Text("CONFIRM TOTAL WIPE") },
            text = { Text("This will shred your identity, all keys, and all messages. This action is irreversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.triggerPanicAction(onComplete = onWipeComplete)
                        showPanicDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("WIPE EVERYTHING", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPanicDialog = false }) {
                    Text("CANCEL")
                }
            },
            containerColor = Color(0xFF1C1F26),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }
}

@Composable
fun SecurityScoreGauge(state: AuditState) {
    val score = when (state) {
        is AuditState.Success -> state.report.risk_score
        else -> 0
    }
    
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
        // Background track
        Canvas(modifier = Modifier.size(180.dp)) {
            drawArc(
                color = Color.DarkGray.copy(alpha = 0.3f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Active track
        val progressColor = when {
            animatedScore > 80 -> Color(0xFF00C853)
            animatedScore > 50 -> Color(0xFFFFD600)
            else -> Color(0xFFFF3D00)
        }

        Canvas(modifier = Modifier.size(180.dp)) {
            drawArc(
                color = progressColor,
                startAngle = 135f,
                sweepAngle = (animatedScore / 100f) * 270f,
                useCenter = false,
                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (state is AuditState.Scanning) "..." else "$animatedScore",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "SECURITY SCORE",
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AuditResultsSection(state: AuditState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1C1F26),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("ENGINE STATUS", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            when (state) {
                is AuditState.Idle -> Text("Ready for scan.", color = Color.White)
                is AuditState.Scanning -> Text("Analyzing network fingerprints...", color = HackerGreen)
                is AuditState.Success -> {
                    Text(state.report.note ?: "Audit complete.", color = Color.White)
                    if (state.report.vulnerabilities.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        state.report.vulnerabilities.forEach { v ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFFD600), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(v, color = Color.LightGray, fontSize = 14.sp)
                            }
                        }
                    } else {
                        Text("No critical vulnerabilities detected.", color = HackerGreen, modifier = Modifier.padding(top = 12.dp))
                    }
                }
                is AuditState.Error -> Text("Engine Error: ${state.message}", color = Color.Red)
            }
        }
    }
}

@Composable
fun ThreatVectorMap(state: AuditState) {
    val report = (state as? AuditState.Success)?.report
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "ANON SANITY CHECKER",
                color = HackerGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            ThreatAxis(
                label = "TRANSPORT",
                value = if (report != null) 0.8f else 0.3f,
                description = "Onion + Mixnet (Paranoia)"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ThreatAxis(
                label = "IDENTITY",
                value = if (report != null) 0.9f else 0.5f,
                description = "PQC-Kyber + SSS Sharding"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ThreatAxis(
                label = "METADATA",
                value = if (report != null) 0.7f else 0.2f,
                description = "PSI Discovery + Zero-Sum"
            )
        }
    }
}

@Composable
private fun ThreatAxis(label: String, value: Float, description: String) {
    val animatedValue by animateFloatAsState(targetValue = value, animationSpec = tween(1000))
    
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(description, color = Color.Gray, fontSize = 10.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedValue)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(HackerGreen.copy(alpha = 0.5f), HackerGreen)
                        )
                    )
            )
        }
    }
}

@Composable
fun PanicControlCard(onPanicClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF330B0B),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF661111))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("PANIC PROTOCOL", color = Color.Red, fontWeight = FontWeight.Bold)
                Text("Instant identity shredding", color = Color.LightGray, fontSize = 12.sp)
            }
            Button(
                onClick = onPanicClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("ACTIVATE", fontWeight = FontWeight.Bold)
            }
        }
    }
}
