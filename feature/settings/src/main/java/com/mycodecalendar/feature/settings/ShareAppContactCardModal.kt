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
 * ShareAppContactCardModal — Interactive modal dialog for sharing Developer Profile & Streak Card,
 * generating App Download QR codes, and displaying Creator digital contact links.
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
    val linkedInLink = "https://www.linkedin.com/in/vishal-bhutekar21/"
    val instagramLink = "https://www.instagram.com/unexplored_vish_2.0/"
    val githubLink = "https://github.com/vishal-bhutekar21"
    val playStoreApp = "https://play.google.com/store/apps/details?id=com.justu.launcher"
    val emailLink = "mailto:vishal.bhutekar1@gmail.com"

    val brandIndigo = Color(0xFF818CF8)
    val brandViolet = Color(0xFFA78BFA)
    val brandCyan = Color(0xFF38BDF8)
    val brandEmerald = Color(0xFF10F07B)
    val brandAmber = Color(0xFFF59E0B)

    val profileShareText = """
🔥 Coding Streak on Code Calendar: $currentStreak Days!
👨‍💻 Developer: ${username ?: "Vishal Bhutekar"}

⚡ App Features:
• 📅 Live Contest Radar (LeetCode, Codeforces, CodeChef, AtCoder)
• 📈 Real-Time Rating Curves & Progression Charts
• ⏰ Instant Calendar Sync & 15m Smart Alarms
• 🐙 GitHub Contributions Heatmap

📲 Download Code Calendar on Android:
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
                cornerRadius = 28.dp,
                accentColor = brandIndigo
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
                                    .background(brandIndigo.copy(alpha = 0.18f), CircleShape)
                                    .border(1.dp, brandIndigo.copy(alpha = 0.4f), CircleShape),
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
                                    tint = if (selectedTab == 0) brandAmber else brandIndigo
                                )
                            }

                            Text(
                                text = when (selectedTab) {
                                    0 -> "Profile & Streak Card"
                                    1 -> "App Download QR"
                                    else -> "Creator Contact"
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
                        TabPill("Streak Card", 0, selectedTab) { selectedTab = 0 }
                        TabPill("App QR", 1, selectedTab) { selectedTab = 1 }
                        TabPill("Creator", 2, selectedTab) { selectedTab = 2 }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── TAB 0: PROFILE & STREAK SHARE CARD ──────────────────
                    if (selectedTab == 0) {
                        // Visual Share Card Graphic Box
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 20.dp,
                            accentColor = brandAmber
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
                                                .size(46.dp)
                                                .background(
                                                    Brush.radialGradient(listOf(brandIndigo, brandViolet)),
                                                    CircleShape
                                                )
                                                .border(1.5.dp, brandCyan.copy(alpha = 0.7f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Rounded.Terminal,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = Color.White
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = username ?: "Vishal Bhutekar",
                                                style = Typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Code Calendar Pioneer",
                                                style = Typography.labelSmall.copy(color = brandIndigo, fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = brandAmber.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, brandAmber.copy(alpha = 0.40f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Rounded.LocalFireDepartment, null, modifier = Modifier.size(14.dp), tint = brandAmber)
                                            Text(
                                                text = "$currentStreak Days",
                                                style = Typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Black),
                                                color = brandAmber
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                                Spacer(modifier = Modifier.height(12.dp))

                                // Feature Checklist Pills
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FeatureHighlightRow("📅 Live Contest Radar", "LeetCode, Codeforces, CodeChef, AtCoder", brandCyan)
                                    FeatureHighlightRow("📈 Rating Progression", "Unified rating curves & solved analytics", brandViolet)
                                    FeatureHighlightRow("⏰ Smart Alarms", "15m notifications & Calendar Auto-Sync", brandEmerald)
                                    FeatureHighlightRow("🐙 Activity Matrix", "GitHub contribution heatmap & sync", brandIndigo)
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Mini QR & Download Link Strip
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFF0F172A).copy(alpha = 0.8f))
                                        .border(1.dp, brandIndigo.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    QrCodeView(
                                        data = appShareLink,
                                        size = 64.dp,
                                        primaryColor = brandCyan,
                                        backgroundColor = Color(0xFF1E1B4B)
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Scan to Install App",
                                            style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Available on Google Play",
                                            style = Typography.labelSmall,
                                            color = brandEmerald
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
                                colors = ButtonDefaults.buttonColors(containerColor = brandIndigo)
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
                                    tint = if (isCopied) brandEmerald else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isCopied) "Copied!" else "Copy Card",
                                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isCopied) brandEmerald else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // ── TAB 1: APP SHARE QR ─────────────────────────────────
                    if (selectedTab == 1) {
                        QrCodeView(
                            data = appShareLink,
                            size = 190.dp,
                            primaryColor = brandCyan,
                            centerIcon = {
                                Icon(
                                    Icons.Rounded.Terminal,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = brandCyan
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Code Calendar for Android",
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Scan QR to get live competitive programming contest alerts & rating charts.",
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
                                colors = ButtonDefaults.buttonColors(containerColor = brandIndigo)
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
                                    tint = if (isCopied) brandEmerald else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isCopied) "Copied!" else "Copy Link",
                                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isCopied) brandEmerald else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // ── TAB 2: CREATOR / DEVELOPER CONTACT CARD ─────────────
                    if (selectedTab == 2) {
                        QrCodeView(
                            data = portfolioLink,
                            size = 170.dp,
                            primaryColor = brandViolet,
                            centerIcon = {
                                Icon(
                                    Icons.Rounded.Code,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = brandViolet
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
                            style = Typography.labelMedium.copy(color = brandIndigo, fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Social Links Grid
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SocialPillButton("Portfolio", Icons.Rounded.Language, Color(0xFF06B6D4), Modifier.weight(1f)) {
                                    onOpenUrl(portfolioLink)
                                }
                                SocialPillButton("LinkedIn", Icons.Rounded.Work, Color(0xFF0A66C2), Modifier.weight(1f)) {
                                    onOpenUrl(linkedInLink)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SocialPillButton("Instagram", Icons.Rounded.CameraAlt, Color(0xFFE1306C), Modifier.weight(1f)) {
                                    onOpenUrl(instagramLink)
                                }
                                SocialPillButton("GitHub", Icons.Rounded.Code, Color(0xFF10F07B), Modifier.weight(1f)) {
                                    onOpenUrl(githubLink)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SocialPillButton("Play Store Apps", Icons.Rounded.Shop, Color(0xFF00E676), Modifier.weight(1f)) {
                                    onOpenUrl(playStoreApp)
                                }
                                SocialPillButton("Email Me", Icons.Rounded.Email, Color(0xFFF59E0B), Modifier.weight(1f)) {
                                    onOpenUrl(emailLink)
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
    onClick: () -> Unit
) {
    val brandIndigo = Color(0xFF818CF8)
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(9.dp))
            .then(
                if (selectedTab == tabIndex) Modifier.background(brandIndigo)
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
    dotColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(dotColor, CircleShape)
        )
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
        color = color.copy(alpha = 0.12f),
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
                color = color
            )
        }
    }
}
