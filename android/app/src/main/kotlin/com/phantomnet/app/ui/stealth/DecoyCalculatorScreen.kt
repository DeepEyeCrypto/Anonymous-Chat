package com.phantomnet.app.ui.stealth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DecoyCalculatorScreen(
    onAttemptUnlock: (String) -> Boolean,
    onUnlockSuccess: () -> Unit
) {
    var display by remember { mutableStateOf("0") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                display,
                fontSize = 64.sp,
                color = Color.White,
                fontWeight = FontWeight.Light,
                maxLines = 1
            )
        }

        // Grid
        val buttons = listOf(
            listOf("C", "±", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "AC", "=")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                row.forEach { char ->
                    CalcButton(
                        text = char,
                        color = when {
                            char in "÷×-+=" -> Color(0xFFFF9F0A)
                            char in "C±%AC" -> Color(0xFFA5A5A5)
                            else -> Color(0xFF333333)
                        },
                        textColor = if (char in "C±%AC") Color.Black else Color.White,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            when (char) {
                                "AC", "C" -> display = "0"
                                "=" -> {
                                    if (onAttemptUnlock(display)) {
                                        onUnlockSuccess()
                                    } else {
                                        // Simulated calculation fallback for non-trigger inputs
                                        display = try {
                                            // Very basic dummy math to look real
                                            if (display.contains("+")) {
                                                val parts = display.split("+")
                                                (parts[0].toDouble() + parts[1].toDouble()).toString()
                                            } else "0"
                                        } catch (e: Exception) {
                                            "0"
                                        }
                                    }
                                }
                                in "0123456789." -> {
                                    if (display == "0") display = char
                                    else display += char
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CalcButton(
    text: String,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(color)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            fontSize = 24.sp,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}
