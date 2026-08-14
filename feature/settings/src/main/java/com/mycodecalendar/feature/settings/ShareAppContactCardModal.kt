package com.mycodecalendar.feature.settings

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.core.designsystem.components.GlassCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ShareAppContactCardModal — Clean, modern modal dialog for sharing Developer Profile & Streak Card,
 * generating App Download QR codes, and displaying Creator portfolio and app links without emoji clutter.
 */
@Composable
fun ShareAppContactCardModal(
    onDismiss: () -> Unit,
    onShareAppClick: () -> Unit,
    onOpenUrl: (String) -> Unit,
    username: String? = "Vishal Bhutekar",
    currentStreak: Int = 14,
    onShareProfileText: (String) -> Unit = {}
) {
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) } // 0 = Profile & Streak Card, 1 = App Share QR, 2 = Creator Links
    var isCopied by remember { mutableStateOf(false) }

    val appShareLink = "https://play.google.com/store/apps/dev?id=8656025420118431472"
    val portfolioLink = "https://vishalbhutekar.netlify.app/"
    val instagramLink = "https://www.instagram.com/unexplored_vish_2.0/"
    val githubLink = "https://github.com/vishal-bhutekar21"
    val playStoreApp = "https://play.google.com/store/apps/details?id=com.justu.launcher"

    val brandOrange = Color(0xFFFF6B00)
    val brandOrangeGrad = listOf(Color(0xFFFF7A00), Color(0xFFFF5200))
    val slateCardBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)

    val profileShareText = """
Coding Streak on Code Calendar: $currentStreak Days
Developer: ${username ?: "Vishal Bhutekar"}

Key Features:
- Live Contest Radar (LeetCode, Codeforces, CodeChef, AtCoder)
- Real-Time Rating Curves & Progression Charts
- Instant Calendar Sync & 15m Smart Alarms
- GitHub Contributions Activity Matrix

Download Code Calendar on Android:
$appShareLink
    """.trimIndent()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.80f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                cornerRadius = 24.dp,
                accentColor = brandOrange
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ── TOP HEADER ──────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(brandOrange.copy(alpha = 0.15f), CircleShape)
                                    .border(1.dp, brandOrange.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (selectedTab) {
                                        0 -> Icons.Rounded.LocalFireDepartment
                                        1 -> Icons.Rounded.QrCode2
                                        else -> Icons.Rounded.Person
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = brandOrange
                                )
                            }

                            Text(
                                text = when (selectedTab) {
                                    0 -> "Profile & Streak Card"
                                    1 -> "App Download QR"
                                    else -> "Creator Links"
                                },
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "Close",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ── 3-TAB SELECTOR ──────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(3.dp)
                    ) {
                        TabPill("Streak Card", 0, selectedTab, brandOrange) { selectedTab = 0 }
                        TabPill("App QR", 1, selectedTab, brandOrange) { selectedTab = 1 }
                        TabPill("Creator", 2, selectedTab, brandOrange) { selectedTab = 2 }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── TAB 0: PROFILE & STREAK SHARE CARD ──────────────────
                    if (selectedTab == 0) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 20.dp,
                            accentColor = brandOrange
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                // Profile Header Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(
                                                    Brush.radialGradient(listOf(brandOrange.copy(alpha = 0.25f), Color(0xFF1E2235))),
                                                    CircleShape
                                                )
                                                .border(1.5.dp, brandOrange.copy(alpha = 0.6f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Rounded.Terminal,
                                                contentDescription = null,
                                                modifier = Modifier.size(22.dp),
                                                tint = brandOrange
                                            )
                                        }

                                        val coderRank = remember(currentStreak) {
                                            com.mycodecalendar.domain.model.BadgeHelper.getCoderRank(currentStreak)
                                        }

                                        Column {
                                            Text(
                                                text = username ?: "Vishal Bhutekar",
                                                style = Typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = coderRank.split(":").getOrElse(1) { coderRank }.trim(),
                                                style = Typography.labelSmall.copy(color = brandOrange, fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = brandOrange.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, brandOrange.copy(alpha = 0.40f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Rounded.LocalFireDepartment, null, modifier = Modifier.size(14.dp), tint = brandOrange)
                                            Text(
                                                text = "$currentStreak Days",
                                                style = Typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Black),
                                                color = brandOrange
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                                Spacer(modifier = Modifier.height(12.dp))

                                // Feature Checklist Rows with Clean Icons (No Emojis)
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FeatureHighlightRow(
                                        title = "Live Contest Radar",
                                        subtitle = "LeetCode, Codeforces, CodeChef, AtCoder",
                                        icon = Icons.Rounded.Schedule,
                                        tint = brandOrange
                                    )
                                    FeatureHighlightRow(
                                        title = "Rating Progression",
                                        subtitle = "Unified rating curves & analytics",
                                        icon = Icons.Rounded.ShowChart,
                                        tint = brandOrange
                                    )
                                    FeatureHighlightRow(
                                        title = "Smart Alarms",
                                        subtitle = "15m alerts & Calendar Auto-Sync",
                                        icon = Icons.Rounded.NotificationsActive,
                                        tint = brandOrange
                                    )
                                    FeatureHighlightRow(
                                        title = "Activity Matrix",
                                        subtitle = "GitHub contribution heatmap & streak sync",
                                        icon = Icons.Rounded.Code,
                                        tint = brandOrange
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Mini QR & Download Link Strip
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                                        .border(1.dp, brandOrange.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    QrCodeView(
                                        data = appShareLink,
                                        size = 64.dp,
                                        primaryColor = brandOrange,
                                        backgroundColor = Color.Black
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Scan to Install App",
                                            style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Available on Google Play",
                                            style = Typography.labelSmall,
                                            color = brandOrange
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (onShareProfileText != {}) {
                                        onShareProfileText(profileShareText)
                                    } else {
                                        onShareAppClick()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = brandOrange)
                            ) {
                                Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share Card", style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }

                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(profileShareText))
                                    isCopied = true
                                    scope.launch {
                                        delay(2500)
                                        isCopied = false
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            ) {
                                Icon(
                                    if (isCopied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isCopied) brandOrange else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isCopied) "Copied!" else "Copy Card",
                                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isCopied) brandOrange else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // ── TAB 1: APP SHARE QR ─────────────────────────────────
                    if (selectedTab == 1) {
                        QrCodeView(
                            data = appShareLink,
                            size = 190.dp,
                            primaryColor = brandOrange,
                            centerIcon = {
                                Icon(
                                    Icons.Rounded.Code,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp),
                                    tint = brandOrange
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "MyCodeCalendar for Android",
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Scan QR to get live competitive programming contest alerts and rating charts.",
                            style = Typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onShareAppClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = brandOrange)
                            ) {
                                Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share App", style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }

                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(appShareLink))
                                    isCopied = true
                                    scope.launch {
                                        delay(2500)
                                        isCopied = false
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            ) {
                                Icon(
                                    if (isCopied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isCopied) brandOrange else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isCopied) "Copied!" else "Copy Link",
                                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isCopied) brandOrange else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // ── TAB 2: CREATOR / DEVELOPER CONTACT CARD ─────────────
                    if (selectedTab == 2) {
                        QrCodeView(
                            data = portfolioLink,
                            size = 170.dp,
                            primaryColor = brandOrange,
                            centerIcon = {
                                Icon(
                                    Icons.Rounded.Language,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp),
                                    tint = brandOrange
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Vishal Bhutekar",
                            style = Typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Android & Full-Stack Developer",
                            style = Typography.labelMedium.copy(color = brandOrange, fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Creator Links: Clean Slate Buttons (Portfolio, JustU Launcher, GitHub, Instagram)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SocialPillButton("Portfolio", Icons.Rounded.Language, brandOrange, Modifier.weight(1f)) {
                                    onOpenUrl(portfolioLink)
                                }
                                SocialPillButton("JustU Launcher", Icons.Rounded.Shop, brandOrange, Modifier.weight(1f)) {
                                    onOpenUrl(playStoreApp)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SocialPillButton("GitHub", Icons.Rounded.Code, brandOrange, Modifier.weight(1f)) {
                                    onOpenUrl(githubLink)
                                }
                                SocialPillButton("Instagram", Icons.Rounded.CameraAlt, brandOrange, Modifier.weight(1f)) {
                                    onOpenUrl(instagramLink)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.TabPill(
    title: String,
    tabIndex: Int,
    selectedTab: Int,
    brandColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(9.dp))
            .then(
                if (selectedTab == tabIndex) Modifier.background(brandColor)
                else Modifier.clickable(onClick = onClick)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
            color = if (selectedTab == tabIndex) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FeatureHighlightRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(tint.copy(alpha = 0.12f), CircleShape)
                .border(1.dp, tint.copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = tint
            )
        }
        Column {
            Text(
                text = title,
                style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = Typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SocialPillButton(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = modifier.height(42.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(16.dp),
                tint = color
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
