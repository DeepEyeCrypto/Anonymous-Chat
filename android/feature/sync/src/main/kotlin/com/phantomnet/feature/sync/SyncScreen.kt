package com.phantomnet.feature.sync

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

private val Emerald = Color(0xFF00E676)
private val Obsidian = Color(0xFF0B0E11)
private val SurfaceCard = Color(0xFF1C1F26)
private val TextGray = Color(0xFF8B949E)

@Composable
fun SyncScreen(
    state: SyncUiState,
    onStartExport: () -> Unit,
    onStartImport: () -> Unit,
    onImportScanned: (String) -> Unit,
    onSyncHistory: () -> Unit,
    onFinish: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "MULTI-DEVICE SYNC", 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.Bold, 
                        letterSpacing = 2.sp,
                        color = Emerald
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", color = Color.White, fontSize = 20.sp)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Obsidian
                )
            )
        },
        containerColor = Obsidian
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.isComplete) {
                SyncSuccessView(state, onSyncHistory, onFinish)
            } else {
                when (state.mode) {
                    SyncMode.Idle -> SyncIdleHome(onStartExport, onStartImport)
                    SyncMode.Exporting -> SyncExportView(state)
                    SyncMode.Importing -> SyncImportView(onImportScanned)
                }
            }
        }
    }
}

@Composable
private fun SyncIdleHome(onStartExport: () -> Unit, onStartImport: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "LINK NEW DEVICE",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Use your existing Phantom identity on another phone. No servers, no logs, pure P2P cloning.",
            fontSize = 14.sp,
            color = TextGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))

        SyncActionCard(
            title = "This is my PRIMARY device",
            subtitle = "Show QR code to link another phone",
            emoji = "📱",
            onClick = onStartExport
        )

        Spacer(modifier = Modifier.height(16.dp))

        SyncActionCard(
            title = "This is my NEW device",
            subtitle = "Scan QR code from your primary phone",
            emoji = "✨",
            onClick = onStartImport
        )
    }
}

@Composable
private fun SyncExportView(state: SyncUiState) {
    if (state.isProcessing) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Emerald)
        }
    } else if (state.exportBundle != null) {
        val qrBitmap = remember(state.exportBundle) {
            SyncQrGenerator.generateQrCode(state.exportBundle, 512)
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "SCAN ME",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Open Phantom Net on your new device and select 'This is my NEW device' to scan.",
                fontSize = 13.sp,
                color = TextGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier
                    .size(300.dp)
                    .clip(RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "Sync Bundle QR",
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "⚠️ Security Warning",
                color = Color(0xFFFFD54F),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                "This QR code contains your private identity keys. Never share it with anyone or show it in public.",
                color = TextGray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun SyncImportView(onImportScanned: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "SCAN PRIMARY DEVICE",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Live Scanner UI
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceCard),
            contentAlignment = Alignment.Center
        ) {
            CameraPreview(onBarcodeDetected = onImportScanned)
            
            // Corner brackets for the scanner
            ScannerBrackets()
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Point your camera at the QR code shown on your other device.",
            fontSize = 14.sp,
            color = TextGray,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SyncActionCard(
    title: String,
    subtitle: String,
    emoji: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard)
            .padding(1.dp), // Thin border effect
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 32.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(subtitle, color = TextGray, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun CameraPreview(onBarcodeDetected: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    
    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { previewView ->
            val cameraExecutor = Executors.newSingleThreadExecutor()
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val selector = CameraSelector.DEFAULT_BACK_CAMERA

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { barcode ->
                            onBarcodeDetected(barcode)
                        })
                    }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        selector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    // Handle camera binding error
                }
            }, ContextCompat.getMainExecutor(context))
        }
    )
}

@Composable
private fun ScannerBrackets() {
    // Basic graphic for scanner UI
    Box(Modifier.fillMaxSize()) {
        // Simple 4-corner brackets
        val color = Emerald.copy(alpha = 0.5f)
        val thickness = 4.dp
        val size = 40.dp
        
        // Top-Left
        Box(Modifier.align(Alignment.TopStart).padding(32.dp).size(size)) {
            Box(Modifier.fillMaxWidth().height(thickness).background(color))
            Box(Modifier.fillMaxHeight().width(thickness).background(color))
        }
        // Top-Right
        Box(Modifier.align(Alignment.TopEnd).padding(32.dp).size(size)) {
            Box(Modifier.fillMaxWidth().height(thickness).background(color).align(Alignment.TopEnd))
            Box(Modifier.fillMaxHeight().width(thickness).background(color).align(Alignment.TopEnd))
        }
        // Bottom-Left
        Box(Modifier.align(Alignment.BottomStart).padding(32.dp).size(size)) {
            Box(Modifier.fillMaxWidth().height(thickness).background(color).align(Alignment.BottomStart))
            Box(Modifier.fillMaxHeight().width(thickness).background(color).align(Alignment.BottomStart))
        }
        // Bottom-Right
        Box(Modifier.align(Alignment.BottomEnd).padding(32.dp).size(size)) {
            Box(Modifier.fillMaxWidth().height(thickness).background(color).align(Alignment.BottomEnd))
            Box(Modifier.fillMaxHeight().width(thickness).background(color).align(Alignment.BottomEnd))
        }
    }
}
@Composable
private fun SyncSuccessView(
    state: SyncUiState,
    onSyncHistory: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("✅", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "IDENTITY MIRRORED",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Your Phantom persona has been safely cloned to this device. You can now choose to sync your message history or start fresh.",
            fontSize = 14.sp,
            color = TextGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        if (state.isHistorySyncing) {
            CircularProgressIndicator(color = Emerald)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Syncing history...", color = TextGray, fontSize = 14.sp)
        } else if (state.isHistoryComplete) {
            Text("History Synced!", color = Emerald, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = Obsidian),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Finalize & Restart", fontWeight = FontWeight.Bold)
            }
        } else {
            Button(
                onClick = onSyncHistory,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = Obsidian),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Sync Message History", fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = onFinish) {
                Text("Skip & Start Fresh", color = TextGray)
            }
        }
    }
}
