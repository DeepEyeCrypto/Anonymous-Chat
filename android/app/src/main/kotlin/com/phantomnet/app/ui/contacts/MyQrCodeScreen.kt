package com.phantomnet.app.ui.contacts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Emerald = Color(0xFF00E676)
private val Obsidian = Color(0xFF0B0E11)
private val SurfaceCard = Color(0xFF1C1F26)
private val TextGray = Color(0xFF8B949E)

@Composable
fun MyQrCodeScreen(
    identityFingerprint: String,
    identityPublicKey: String,
    onBackClick: () -> Unit
) {
    val qrBitmap = remember(identityPublicKey) {
        QrGenerator.generateQrCode(identityPublicKey, 512)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Text("←", color = Color.White, fontSize = 24.sp)
            }
            Text(
                "MY IDENTITY QR",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Emerald,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.weight(0.1f))

        // QR Card
        Card(
            modifier = Modifier
                .size(320.dp)
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "Identity QR Code",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Fingerprint Info
        Text(
            "FINGERPRINT",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextGray,
            letterSpacing = 2.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            identityFingerprint.chunked(2).joinToString(" ").uppercase(),
            fontSize = 18.sp,
            fontFamily = FontFamily.Monospace,
            color = Emerald,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Anyone who scans this QR code can see your public identity and send you encrypted messages.",
            fontSize = 14.sp,
            color = TextGray,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.weight(0.2f))

        Button(
            onClick = { /* Future: share intent */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Emerald,
                contentColor = Obsidian
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Share Identity Link", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}
