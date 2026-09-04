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
import androidx.compose.ui.draw.shadow
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
                .statusBarsPadding()
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
                        color = Color(0xFF0F172A)
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "Link your competitive profile for live tracking",
                        style = Typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── CONNECTED ACCOUNTS (minimal list) ─────────────────────────────
            if (connectedAccounts.isNotEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        "Linked Handles (${connectedAccounts.size})",
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(3.dp, RoundedCornerShape(16.dp), spotColor = Color(0x10000000))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                    ) {
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
                                                .size(10.dp)
                                                .background(acColor, CircleShape)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "@${acc.username}",
                                                style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color(0xFF0F172A)
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                Text(
                                                    acc.platform.getDisplayName(),
                                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                    color = acColor,
                                                    fontSize = 11.sp
                                                )
                                                Text("· Synced", style = Typography.labelSmall, color = Color(0xFF16A34A), fontSize = 11.sp)
                                            }
                                        }
                                    }
                                    IconButton(
                                        onClick = { onRemovePlatform(acc.platform) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.DeleteOutline, "Disconnect",
                                            modifier = Modifier.size(17.dp),
                                            tint = Color(0xFFEF4444).copy(alpha = 0.7f)
                                        )
                                    }
                                }
                                if (idx < connectedAccounts.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 14.dp),
                                        color = Color(0xFFF1F5F9)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // ── PLATFORM SELECTOR ─────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    "Select Platform to Connect",
                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(bottom = 10.dp, start = 2.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Platform.values().forEach { platform ->
                        val isSelected = platform == selectedPlatform
                        val color = platform.getBrandColor()
                        val alreadyLinked = connectedAccounts.any { it.platform == platform }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(if (isSelected) 4.dp else 1.dp, RoundedCornerShape(14.dp), spotColor = if (isSelected) color.copy(alpha = 0.25f) else Color(0x08000000))
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) color.copy(alpha = 0.06f) else Color.White)
                                .border(
                                    width = if (isSelected) 1.6.dp else 1.dp,
                                    color = if (isSelected) color else Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    selectedPlatform = platform
                                    validationError = null
                                    isSuccess = false
                                }
                                .padding(horizontal = 14.dp, vertical = 13.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(9.dp))
                                            .background(color.copy(alpha = 0.12f))
                                            .border(0.8.dp, color.copy(alpha = 0.25f), RoundedCornerShape(9.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when (platform) {
                                                Platform.LEETCODE -> "LC"
                                                Platform.CODEFORCES -> "CF"
                                                Platform.CODECHEF -> "CC"
                                                Platform.ATCODER -> "AC"
                                                Platform.GITHUB -> "GH"
                                                Platform.GEEKSFORGEEKS -> "GFG"
                                            },
                                            style = Typography.labelMedium.copy(fontWeight = FontWeight.Black, fontSize = 11.sp),
                                            color = color
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = platform.getDisplayName(),
                                            style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) color else Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = if (alreadyLinked) "Currently linked" else "Available to sync",
                                            style = Typography.bodySmall.copy(fontSize = 11.sp),
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                if (alreadyLinked) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFDCFCE7))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            "Linked",
                                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                            color = Color(0xFF15803D)
                                        )
                                    }
                                } else if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(color)
                                            .size(18.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(22.dp))

            // ── HANDLE INPUT ──────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    if (isAlreadyConnected) "Update Handle" else "Your Handle",
                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF64748B),
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
                            color = Color(0xFF94A3B8)
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
                    textStyle = Typography.bodyMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = if (validationError != null) MaterialTheme.colorScheme.error else brandColor,
                        unfocusedBorderColor = if (validationError != null) MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                            else Color(0xFFCBD5E1),
                        focusedTextColor = Color(0xFF0F172A),
                        unfocusedTextColor = Color(0xFF0F172A),
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