package com.mycodecalendar.feature.platforms

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycodecalendar.core.designsystem.GlassmorphismBackground
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.core.designsystem.components.GlassBackButton
import com.mycodecalendar.core.designsystem.components.GlassCard
import com.mycodecalendar.core.designsystem.components.getBrandColor
import com.mycodecalendar.core.designsystem.components.getDisplayName
import com.mycodecalendar.domain.model.Platform
import com.mycodecalendar.domain.model.PlatformAccount
import kotlinx.coroutines.launch

/**
 * AddPlatformScreen - premium minimal redesign.
 * - No tick icons anywhere
 * - Selection shown by brand-color left accent bar + gradient highlight
 * - Separate Update / Disconnect buttons for already-connected platforms
 * - Clean floating-label style input
 */
@Composable
fun AddPlatformScreen(
    connectedAccounts: List<PlatformAccount>,
    onAddPlatform: (Platform, String) -> Unit,
    onValidateHandle: suspend (Platform, String) -> String?,
    onRemovePlatform: (Platform) -> Unit,
    onBackClick: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var selectedPlatform by remember { mutableStateOf(Platform.CODEFORCES) }
    var username by remember { mutableStateOf("") }
    var isValidating by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(selectedPlatform) {
        username = connectedAccounts.firstOrNull { it.platform == selectedPlatform }?.username ?: ""
        validationError = null
        isSuccess = false
        isValidating = false
    }

    val isAlreadyConnected = connectedAccounts.any { it.platform == selectedPlatform }
    val brandColor = selectedPlatform.getBrandColor()

    val placeholderHint = when (selectedPlatform) {
        Platform.CODEFORCES    -> "e.g. tourist, Petr"
        Platform.LEETCODE      -> "e.g. neal_wu, lee215"
        Platform.CODECHEF      -> "e.g. gennady.korotkevich"
        Platform.ATCODER       -> "e.g. tourist, rng_58"
        Platform.GITHUB        -> "e.g. torvalds, google"
        Platform.GEEKSFORGEEKS -> "e.g. yourhandle"
    }

    GlassmorphismBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── TOP BAR ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassBackButton(onClick = onBackClick)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        "Connect Platform",
                        style = Typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Link your handle to track ratings and contests",
                        style = Typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── CONNECTED ACCOUNTS (minimal list) ─────────────────────────────
            if (connectedAccounts.isNotEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        "Connected (${connectedAccounts.size})",
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                        modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                    )
                    GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
                        Column {
                            connectedAccounts.forEachIndexed { idx, acc ->
                                val acColor = acc.platform.getBrandColor()
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Brand color dot
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(acColor, CircleShape)
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "@${acc.username}",
                                                style = Typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    acc.platform.getDisplayName(),
                                                    style = Typography.labelSmall,
                                                    color = acColor.copy(alpha = 0.80f),
                                                    fontSize = 10.sp
                                                )
                                                val syncLabel = when (acc.syncStatus) {
                                                    "SYNCED"  -> "· Synced"
                                                    "SYNCING" -> "· Syncing..."
                                                    "ERROR"   -> "· Sync failed"
                                                    else      -> ""
                                                }
                                                val syncColor = when (acc.syncStatus) {
                                                    "SYNCED"  -> Color(0xFF10B981)
                                                    "SYNCING" -> MaterialTheme.colorScheme.primary
                                                    "ERROR"   -> Color(0xFFF87171)
                                                    else      -> MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                                if (syncLabel.isNotBlank()) {
                                                    Text(syncLabel, style = Typography.labelSmall, color = syncColor, fontSize = 10.sp)
                                                }
                                            }
                                        }
                                    }
                                    IconButton(
                                        onClick = { onRemovePlatform(acc.platform) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.DeleteOutline, "Disconnect",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                                        )
                                    }
                                }
                                if (idx < connectedAccounts.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 14.dp),
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // ── PLATFORM SELECTOR ─────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    "Select Platform",
                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier.padding(bottom = 10.dp, start = 2.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Platform.values().forEach { platform ->
                        val isSelected = platform == selectedPlatform
                        val color = platform.getBrandColor()
                        val alreadyLinked = connectedAccounts.any { it.platform == platform }

                        val bgBrush = if (isSelected) Brush.horizontalGradient(
                            listOf(color.copy(alpha = 0.22f), color.copy(alpha = 0.06f))
                        ) else null

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .then(
                                    if (bgBrush != null) Modifier.background(bgBrush)
                                    else Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.55f))
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.6.dp,
                                    color = if (isSelected) color.copy(alpha = 0.70f)
                                            else color.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    selectedPlatform = platform
                                    validationError = null
                                    isSuccess = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 0.dp, vertical = 0.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left accent bar
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(52.dp)
                                        .background(
                                            if (isSelected) color else Color.Transparent,
                                            RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)
                                        )
                                )
                                Spacer(Modifier.width(14.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(end = 14.dp, top = 12.dp, bottom = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        // Brand color indicator dot
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(color, CircleShape)
                                        )
                                        Text(
                                            text = platform.getDisplayName(),
                                            style = Typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = if (isSelected) color
                                                    else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    // Connected badge (no tick)
                                    if (alreadyLinked) {
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(color.copy(alpha = 0.12f))
                                                .border(1.dp, color.copy(alpha = 0.35f), CircleShape)
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                "● Connected",
                                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = color,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── HANDLE INPUT ──────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    if (isAlreadyConnected) "Update Handle" else "Your Handle",
                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        validationError = null
                        isSuccess = false
                    },
                    placeholder = {
                        Text(
                            placeholderHint,
                            style = Typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f)
                        )
                    },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(brandColor.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Person, null,
                                modifier = Modifier.size(16.dp),
                                tint = brandColor
                            )
                        }
                    },
                    trailingIcon = when {
                        isValidating -> { { CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = brandColor) } }
                        validationError != null -> { { Icon(Icons.Rounded.ErrorOutline, "Error", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error) } }
                        else -> null
                    },
                    isError = validationError != null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    textStyle = Typography.bodyMedium,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
                        focusedBorderColor = if (validationError != null) MaterialTheme.colorScheme.error else brandColor,
                        unfocusedBorderColor = if (validationError != null) MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                        cursorColor = brandColor
                    )
                )

                AnimatedVisibility(visible = validationError != null, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                    validationError?.let { err ->
                        Row(modifier = Modifier.padding(top = 6.dp, start = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Rounded.Info, null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.error)
                            Text(err, style = Typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                AnimatedVisibility(visible = isValidating, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                    Text(
                        "Checking if @${username.trim()} exists on ${selectedPlatform.getDisplayName()}…",
                        modifier = Modifier.padding(top = 6.dp, start = 4.dp),
                        style = Typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                AnimatedVisibility(visible = isSuccess, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                    Row(modifier = Modifier.padding(top = 6.dp, start = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Box(modifier = Modifier.size(7.dp).background(Color(0xFF10B981), CircleShape))
                        Text(
                            "Handle verified — tap below to save",
                            style = Typography.labelSmall,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── ACTION BUTTONS ────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

                // Primary: Verify & Connect / Update
                Button(
                    onClick = {
                        if (username.isNotBlank() && !isValidating) {
                            scope.launch {
                                isValidating = true
                                validationError = null
                                isSuccess = false
                                val error = onValidateHandle(selectedPlatform, username.trim())
                                isValidating = false
                                if (error != null) {
                                    validationError = error
                                } else {
                                    isSuccess = true
                                    kotlinx.coroutines.delay(350)
                                    onAddPlatform(selectedPlatform, username.trim())
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = username.isNotBlank() && !isValidating,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = brandColor,
                        disabledContainerColor = brandColor.copy(alpha = 0.30f)
                    )
                ) {
                    if (isValidating) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                        Text("Validating…", style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    } else {
                        Text(
                            text = when {
                                isAlreadyConnected -> "Update Handle"
                                else               -> "Verify & Connect"
                            },
                            style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                // Secondary (when already connected): Save without validation
                if (!isValidating && username.isNotBlank()) {
                    OutlinedButton(
                        onClick = { onAddPlatform(selectedPlatform, username.trim()) },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, brandColor.copy(alpha = 0.40f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = brandColor
                        )
                    ) {
                        Text(
                            "Save without verification",
                            style = Typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                            color = brandColor.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(100.dp))
        }
    }
}