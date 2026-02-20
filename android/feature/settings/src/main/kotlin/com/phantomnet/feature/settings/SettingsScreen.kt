package com.phantomnet.feature.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Emerald = Color(0xFF00E676)
private val Obsidian = Color(0xFF0B0E11)
private val SurfaceCard = Color(0xFF1C1F26)
private val TextGray = Color(0xFF8B949E)
private val DangerRed = Color(0xFFFF5252)
private val DividerColor = Color(0xFF30363D)

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onPrivacyDashboardClick: () -> Unit,
    onSecureBackupClick: () -> Unit,
    onLinkDeviceClick: () -> Unit,
    onStealthModeChange: (Int) -> Unit,
    onMixnetChange: (Boolean) -> Unit,
    onParanoiaChange: (Boolean) -> Unit,
    onWipeConfirmed: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var showCopied by remember { mutableStateOf(false) }
    var showWipeDialog by remember { mutableStateOf(false) }
    var showStealthDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Text(
            "SETTINGS",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Emerald,
            letterSpacing = 3.sp,
            modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
        )

        // ── IDENTITY CARD ──
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        clipboardManager.setText(AnnotatedString(state.fingerprint))
                        showCopied = true
                    }
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "MY IDENTITY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextGray,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Fingerprint grid
                val parts = state.fingerprint.split(" ")
                if (parts.size >= 8) {
                    Text(
                        parts.take(4).joinToString("  "),
                        fontSize = 22.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Emerald,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        parts.drop(4).take(4).joinToString("  "),
                        fontSize = 22.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Emerald,
                        letterSpacing = 2.sp
                    )
                } else {
                    Text(
                        state.fingerprint,
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Emerald,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                AnimatedVisibility(
                    visible = showCopied,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        "✓ Copied to clipboard",
                        fontSize = 12.sp,
                        color = Emerald.copy(alpha = 0.7f)
                    )
                    LaunchedEffect(showCopied) {
                        if (showCopied) {
                            kotlinx.coroutines.delay(2000)
                            showCopied = false
                        }
                    }
                }

                if (!showCopied) {
                    Text(
                        "Tap to copy",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── SECURITY SECTION ──
        SectionHeader("SECURITY")
        SettingsItem("Privacy Dashboard", "Audit your privacy configuration") {
            onPrivacyDashboardClick()
        }
        SettingsDivider()
        SettingsItem("Stealth Mode & Appearance", "Hide or disguise the application") {
            showStealthDialog = true
        }
        SettingsDivider()
        SettingsItem("Secure Backup (SSS)", "Shard your recovery key") {
            onSecureBackupClick()
        }
        SettingsDivider()
        SettingsItem("Link New Device", "Sync this identity to another phone") {
            onLinkDeviceClick()
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── TRANSPORT SECTION (Phase 7) ──
        SectionHeader("TRANSPORT")
        SettingsToggleItem(
            title = "Untraceable Mixnet",
            subtitle = "Route traffic through multi-hop mixnet",
            checked = state.mixnetEnabled,
            onCheckedChange = onMixnetChange
        )
        SettingsDivider()
        SettingsToggleItem(
            title = "Paranoia Mode (Cover Traffic)",
            subtitle = "Constant-throughput dummy packets",
            checked = state.paranoiaMode,
            onCheckedChange = onParanoiaChange
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── APPLICATION SECTION ──
        SectionHeader("APPLICATION")
        SettingsInfoRow("App Version", state.appVersion)
        SettingsDivider()
        SettingsInfoRow(
            "Rust Core",
            if (state.coreAvailable) "✓ Loaded" else "✗ Missing"
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ── DANGER ZONE ──
        SectionHeader("DANGER ZONE", color = DangerRed)

        Button(
            onClick = { showWipeDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DangerRed.copy(alpha = 0.1f),
                contentColor = DangerRed
            ),
            shape = RoundedCornerShape(16.dp),
            enabled = !state.isWiping
        ) {
            if (state.isWiping) {
                CircularProgressIndicator(
                    color = DangerRed,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Wiping...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            } else {
                Text("⚠  WIPE IDENTITY", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // ── WIPE CONFIRMATION DIALOG ──
    if (showWipeDialog) {
        WipeConfirmationDialog(
            onConfirm = {
                showWipeDialog = false
                onWipeConfirmed()
            },
            onDismiss = { showWipeDialog = false }
        )
    }

    // ── STEALTH SETTINGS DIALOG ──
    if (showStealthDialog) {
        StealthSettingsDialog(
            currentMode = state.stealthMode,
            onModeChange = { mode ->
                onStealthModeChange(mode)
            },
            onDismiss = { showStealthDialog = false }
        )
    }
}

@Composable
private fun StealthSettingsDialog(
    currentMode: Int,
    onModeChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var realPin by remember { mutableStateOf("") }
    var decoyPin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCard,
        title = { Text("STEALTH & DISGUISE", color = Emerald, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "Choose how Phantom Net appears on your device. Disguised modes hide the app behind a decoy interface.",
                    color = TextGray,
                    fontSize = 14.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                // Mode Selection
                StealthOption("Normal", "Standard icon and entry", currentMode == 0) { onModeChange(0) }
                StealthOption("Disguised", "Hide behind Calculator", currentMode == 1) { onModeChange(1) }
                StealthOption("System", "Disguise as System Config", currentMode == 2) { onModeChange(2) }

                if (currentMode != 0) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("PIN CONFIGURATION", color = Emerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = realPin,
                        onValueChange = { realPin = it },
                        label = { Text("Real Master PIN") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Emerald, unfocusedBorderColor = DividerColor)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = decoyPin,
                        onValueChange = { decoyPin = it },
                        label = { Text("Decoy Safety PIN") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Emerald, unfocusedBorderColor = DividerColor)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Entering the Decoy PIN will open a fresh, benign session to satisfy inspectors.",
                        color = Color.Yellow.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    // In a real implementation, we'd save PINs here via ViewModel
                    // But for this demo, we'll just close
                    onDismiss() 
                },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = Obsidian)
            ) {
                Text("SAVE", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun StealthOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(if (selected) Emerald.copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = Emerald)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(title, color = if (selected) Emerald else Color.White, fontWeight = FontWeight.Bold)
            Text(subtitle, color = TextGray, fontSize = 12.sp)
        }
    }
}

@Composable
private fun WipeConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var confirmText by remember { mutableStateOf("") }
    val isValid = confirmText.equals("DELETE", ignoreCase = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCard,
        title = {
            Text(
                "Destroy Identity?",
                color = DangerRed,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "This will permanently destroy your Phantom identity, " +
                            "all messages, and all conversations. This cannot be undone.",
                    color = TextGray,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Type DELETE to confirm:",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmText,
                    onValueChange = { confirmText = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DangerRed,
                        unfocusedBorderColor = DividerColor,
                        cursorColor = DangerRed,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    placeholder = {
                        Text("DELETE", color = TextGray.copy(alpha = 0.3f))
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DangerRed,
                    contentColor = Color.White,
                    disabledContainerColor = DangerRed.copy(alpha = 0.2f)
                )
            ) {
                Text("Destroy", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        }
    )
}

@Composable
private fun SectionHeader(title: String, color: Color = TextGray) {
    Text(
        title,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = color,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun SettingsItem(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 13.sp, color = TextGray)
        }
        Text("→", fontSize = 18.sp, color = TextGray)
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 13.sp, color = TextGray)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Emerald,
                checkedTrackColor = Emerald.copy(alpha = 0.5f),
                uncheckedThumbColor = TextGray,
                uncheckedTrackColor = SurfaceCard
            )
        )
    }
}

@Composable
private fun SettingsInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 16.sp, color = Color.White)
        Text(value, fontSize = 14.sp, color = TextGray, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        color = DividerColor,
        thickness = 0.5.dp
    )
}
