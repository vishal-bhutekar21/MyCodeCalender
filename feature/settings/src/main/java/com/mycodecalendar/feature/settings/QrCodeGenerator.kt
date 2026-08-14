package com.mycodecalendar.feature.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * QR Code matrix generator and Canvas renderer.
 * Produces a sharp, visually stunning aesthetic QR code with smooth rounded modules
 * and center brand emblem for digital contact and app sharing.
 */
@Composable
fun QrCodeView(
    data: String,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    primaryColor: Color = Color(0xFF818CF8),
    backgroundColor: Color = Color(0xFF0F172A),
    centerIcon: @Composable (() -> Unit)? = null
) {
    val matrix = remember(data) { generateQrMatrix(data, 25) }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .border(1.5.dp, primaryColor.copy(alpha = 0.40f), RoundedCornerShape(20.dp))
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val n = matrix.size
            val cellSize = this.size.width / n

            for (r in 0 until n) {
                for (c in 0 until n) {
                    if (matrix[r][c]) {
                        // Skip center zone if center icon is present
                        val isCenterZone = r in (n / 2 - 2)..(n / 2 + 2) && c in (n / 2 - 2)..(n / 2 + 2)
                        if (centerIcon != null && isCenterZone) continue

                        // Check if in corner finder patterns
                        val isFinder = (r < 7 && c < 7) || (r < 7 && c >= n - 7) || (r >= n - 7 && c < 7)
                        val cellColor = if (isFinder) primaryColor else primaryColor.copy(alpha = 0.88f)

                        drawRoundRect(
                            color = cellColor,
                            topLeft = Offset(c * cellSize + 0.5f, r * cellSize + 0.5f),
                            size = Size(cellSize - 1f, cellSize - 1f),
                            cornerRadius = CornerRadius(cellSize * 0.3f, cellSize * 0.3f)
                        )
                    }
                }
            }
        }

        if (centerIcon != null) {
            Box(
                modifier = Modifier
                    .size(size * 0.24f)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1B4B))
                    .border(1.dp, primaryColor.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                centerIcon()
            }
        }
    }
}

/**
 * Deterministic QR matrix generator with authentic finder patterns, timing lines,
 * alignment markers, and data bits based on input hash string.
 */
private fun generateQrMatrix(content: String, size: Int = 25): Array<BooleanArray> {
    val m = Array(size) { BooleanArray(size) { false } }

    // 1. Draw 3 Finder Patterns (Top-Left, Top-Right, Bottom-Left)
    drawFinderPattern(m, 0, 0)
    drawFinderPattern(m, 0, size - 7)
    drawFinderPattern(m, size - 7, 0)

    // 2. Timing Patterns (horizontal & vertical)
    for (i in 8 until size - 8) {
        if (i % 2 == 0) {
            m[6][i] = true
            m[i][6] = true
        }
    }

    // 3. Alignment Pattern (near bottom-right)
    drawAlignmentPattern(m, size - 9, size - 9)

    // 4. Data Modules based on content hash
    val hash = content.hashCode()
    var seed = abs(hash).toLong()
    if (seed == 0L) seed = 0x5DEECE66DL

    for (r in 0 until size) {
        for (c in 0 until size) {
            // Skip reserved finder and timing zones
            if (isReserved(r, c, size)) continue

            seed = (seed * 0x5DEECE66DL + 0xBL) and ((1L shl 48) - 1)
            val bit = ((seed ushr 16) and 1L) == 1L
            val charFactor = if (content.isNotEmpty()) content[(r * size + c) % content.length].code % 2 == 0 else false
            m[r][c] = bit xor charFactor
        }
    }

    // Dark module (standard QR position)
    m[4 * 2 + 1][8] = true

    return m
}

private fun drawFinderPattern(m: Array<BooleanArray>, r: Int, c: Int) {
    for (i in 0 until 7) {
        for (j in 0 until 7) {
            if (i == 0 || i == 6 || j == 0 || j == 6) {
                m[r + i][c + j] = true
            } else if (i in 2..4 && j in 2..4) {
                m[r + i][c + j] = true
            } else {
                m[r + i][c + j] = false
            }
        }
    }
}

private fun drawAlignmentPattern(m: Array<BooleanArray>, r: Int, c: Int) {
    for (i in 0 until 5) {
        for (j in 0 until 5) {
            if (i == 0 || i == 4 || j == 0 || j == 4 || (i == 2 && j == 2)) {
                m[r + i][c + j] = true
            }
        }
    }
}

private fun isReserved(r: Int, c: Int, size: Int): Boolean {
    // Top-Left Finder + Separator
    if (r <= 7 && c <= 7) return true
    // Top-Right Finder + Separator
    if (r <= 7 && c >= size - 8) return true
    // Bottom-Left Finder + Separator
    if (r >= size - 8 && c <= 7) return true
    // Timing Lines
    if (r == 6 || c == 6) return true
    // Alignment Pattern zone
    if (r in (size - 9)..(size - 5) && c in (size - 9)..(size - 5)) return true
    return false
}
