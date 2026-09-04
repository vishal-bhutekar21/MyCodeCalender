package com.mycodecalendar.feature.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import com.mycodecalendar.core.designsystem.AppTheme
import com.mycodecalendar.core.designsystem.BrandPrimaryOrange
import com.mycodecalendar.core.designsystem.BrandPurpleAccent
import com.mycodecalendar.core.designsystem.GlassmorphismBackground
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.core.designsystem.components.EmptyState
import com.mycodecalendar.core.designsystem.components.GlassCard
import com.mycodecalendar.core.designsystem.components.PlatformBadge
import com.mycodecalendar.core.designsystem.components.SectionHeader
import com.mycodecalendar.core.designsystem.isAppInDarkTheme
import com.mycodecalendar.domain.model.PlatformAccount

/**
 * SettingsScreen — Clean, Unified, Cohesive Settings & Developer Showcase.
 */
@Composable
fun SettingsScreen(
    connectedAccounts: List<PlatformAccount>,
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    onAddPlatformClick: () -> Unit,
    onManageAccountClick: (PlatformAccount) -> Unit,
    authUsername: String? = null,
    authMethod: String? = null,
    authEmail: String? = null,
    authAvatar: String? = null,
    currentStreak: Int = 1,
    onSignOutClick: () -> Unit = {},
    onDeleteAccountClick: () -> Unit = {},
    onReplayOnboardingClick: () -> Unit = {},
    onShareApp: () -> Unit = {},
    onShareProfileText: (String) -> Unit = {},
    onOpenUrl: (String) -> Unit = {},
    onSubmitFeedback: (type: String, title: String, description: String, email: String) -> Unit = { _, _, _, _ -> }
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var calendarSyncEnabled by remember { mutableStateOf(true) }
    var showShareModal by remember { mutableStateOf(false) }
    var showFeedbackModal by remember { mutableStateOf(false) }
    var initialFeedbackType by remember { mutableStateOf("FEATURE_REQUEST") }
    var showSignOutConfirmModal by remember { mutableStateOf(false) }
    var showDeleteAccountConfirmModal by remember { mutableStateOf(false) }
    var showNotificationPermDialog by remember { mutableStateOf(false) }
    val isDark = isAppInDarkTheme
    val context = LocalContext.current

    // Check if notification permission is already granted (Android 13+)
    val hasNotificationPermission: Boolean = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        notificationsEnabled = granted
        if (!granted) showNotificationPermDialog = false
    }

    val portfolioLink = "https://vishalbhutekar.netlify.app/"
    val linkedInLink = "https://www.linkedin.com/in/vishal-bhutekar21/"
    val instagramLink = "https://www.instagram.com/unexplored_vish_2.0/"
    val githubLink = "https://github.com/vishal-bhutekar21"
    val playStoreApp = "https://play.google.com/store/apps/details?id=com.justu.launcher"
    val playDevPage = "https://play.google.com/store/apps/dev?id=8656025420118431472"
    val emailLink = "mailto:vishal.bhutekar1@gmail.com"

    GlassmorphismBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
                // ── 1. HEADER ──────────────────────────────────────────────────────────
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Settings",
                            style = Typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Preferences & Profile",
                            style = Typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                        )
                    }
                    // Quick Action: Share Contact Card
                    IconButton(
                        onClick = { showShareModal = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(BrandPrimaryOrange.copy(alpha = 0.12f))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "Share Profile",
                            tint = BrandPrimaryOrange,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── 2. COMPACT PROFILE HERO CARD ───────────────────────────────────────
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    accentColor = BrandPrimaryOrange
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Glowing Avatar Container
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(BrandPrimaryOrange, Color(0xFFFF3D00))
                                        )
                                    )
                                    .padding(2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFF0F172A) else Color(0xFFFFF7ED)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!authUsername.isNullOrBlank() && authUsername != "Guest Developer") {
                                        Text(
                                            text = authUsername.take(1).uppercase(),
                                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                            color = BrandPrimaryOrange
                                        )
                                    } else {
                                        Icon(
                                            Icons.Rounded.Person,
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp),
                                            tint = BrandPrimaryOrange
                                        )
                                    }
                                }
                            }

                            // Identity & Auth Details
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(
                                        text = authUsername ?: "Guest Developer",
                                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (authMethod != "Guest" && !authMethod.isNullOrBlank()) {
                                        Icon(
                                            Icons.Rounded.Verified,
                                            contentDescription = "Verified",
                                            modifier = Modifier.size(15.dp),
                                            tint = Color(0xFF38BDF8)
                                        )
                                    }
                                }
                                Text(
                                    text = if (!authEmail.isNullOrBlank()) authEmail else (if (authMethod == "Guest") "Local guest mode" else "Google account sync"),
                                    style = Typography.bodySmall.copy(fontSize = 11.5.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Tour replay action
                            IconButton(
                                onClick = onReplayOnboardingClick,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                            ) {
                                Icon(
                                    Icons.Rounded.AutoAwesome,
                                    contentDescription = "Tour",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFF6366F1)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Compact 3-stat pill row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color(0xFF0F172A).copy(alpha = 0.55f) else Color(0xFFF1F5F9))
                                .padding(vertical = 9.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Streak
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Rounded.LocalFireDepartment, null, modifier = Modifier.size(15.dp), tint = BrandPrimaryOrange)
                                Text("$currentStreak d Streak", style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp))
                            }
                            Box(modifier = Modifier.height(16.dp).width(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)))
                            // Platforms
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Rounded.Code, null, modifier = Modifier.size(15.dp), tint = Color(0xFF6366F1))
                                Text("${connectedAccounts.size} Linked", style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp))
                            }
                            Box(modifier = Modifier.height(16.dp).width(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)))
                            // Sync
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(if (authMethod != "Guest") Icons.Rounded.CloudDone else Icons.Rounded.CloudOff, null, modifier = Modifier.size(15.dp), tint = if (authMethod != "Guest") Color(0xFF22C55E) else Color(0xFF94A3B8))
                                Text(if (authMethod != "Guest") "Cloud" else "Local", style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ── 3. GROUP: PREFERENCES ──────────────────────────────────────────────
                MinimalSectionLabel("PREFERENCES")
                MinimalCardGroup {
                    // Theme Row
                    MinimalSettingRow(
                        icon = if (isDark) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Appearance",
                        subtitle = when (currentTheme) {
                            AppTheme.DARK -> "Dark (Obsidian)"
                            AppTheme.LIGHT -> "Light (Daylight)"
                            AppTheme.SYSTEM -> "Follow System"
                        },
                        trailing = {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                    .padding(2.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                ThemeSegmentOption(Icons.Rounded.DarkMode, "Dark", currentTheme == AppTheme.DARK) { onThemeChange(AppTheme.DARK) }
                                ThemeSegmentOption(Icons.Rounded.LightMode, "Light", currentTheme == AppTheme.LIGHT) { onThemeChange(AppTheme.LIGHT) }
                                ThemeSegmentOption(Icons.Rounded.PhoneAndroid, "System", currentTheme == AppTheme.SYSTEM) { onThemeChange(AppTheme.SYSTEM) }
                            }
                        }
                    )
                    MinimalDivider()
                    // Contest Alerts
                    MinimalSettingRow(
                        icon = Icons.Rounded.NotificationsActive,
                        iconTint = Color(0xFFFF7A00),
                        title = "Contest Alerts",
                        subtitle = "15m heads-up before registered rounds",
                        trailing = {
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { isOn ->
                                    if (isOn && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                        showNotificationPermDialog = true
                                    } else {
                                        notificationsEnabled = isOn
                                    }
                                },
                                colors = minimalSwitchColors()
                            )
                        }
                    )
                    MinimalDivider()
                    // Daily Problem
                    var dailyProblemEnabled by remember { mutableStateOf(true) }
                    MinimalSettingRow(
                        icon = Icons.Rounded.Lightbulb,
                        iconTint = Color(0xFFFFA116),
                        title = "Daily Problem Reminder",
                        subtitle = "Morning reminder for Problem of the Day",
                        trailing = {
                            Switch(
                                checked = dailyProblemEnabled,
                                onCheckedChange = { dailyProblemEnabled = it },
                                colors = minimalSwitchColors()
                            )
                        }
                    )
                    MinimalDivider()
                    // Calendar Sync
                    MinimalSettingRow(
                        icon = Icons.Rounded.CalendarMonth,
                        iconTint = Color(0xFF22C55E),
                        title = "Calendar Auto-Sync",
                        subtitle = "Export registered contests to phone calendar",
                        trailing = {
                            Switch(
                                checked = calendarSyncEnabled,
                                onCheckedChange = { calendarSyncEnabled = it },
                                colors = minimalSwitchColors()
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ── 4. GROUP: CONNECTED PLATFORMS ──────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MinimalSectionLabel("CONNECTED PLATFORMS")
                    Text(
                        text = "+ Connect",
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = BrandPrimaryOrange,
                        modifier = Modifier.clickable(onClick = onAddPlatformClick)
                    )
                }
                MinimalCardGroup {
                    if (connectedAccounts.isEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onAddPlatformClick)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "No platforms connected",
                                    style = Typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Link LeetCode, Codeforces, CodeChef & GitHub",
                                    style = Typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                            Icon(
                                Icons.Rounded.AddCircleOutline,
                                contentDescription = null,
                                tint = BrandPrimaryOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        connectedAccounts.forEachIndexed { idx, acc ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onManageAccountClick(acc) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    PlatformBadge(platform = acc.platform)
                                    Column {
                                        Text(
                                            text = "@${acc.username}",
                                            style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = acc.platform.name,
                                            style = Typography.bodySmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                Icon(
                                    Icons.Rounded.ChevronRight,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                )
                            }
                            if (idx < connectedAccounts.lastIndex) MinimalDivider()
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ── 5. GROUP: CREATOR & COMMUNITY ──────────────────────────────────────
                MinimalSectionLabel("CREATOR & COMMUNITY")
                MinimalCardGroup {
                    // Creator profile row
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(BrandPrimaryOrange.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Terminal,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = BrandPrimaryOrange
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Vishal Bhutekar",
                                        style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Icon(
                                        Icons.Rounded.Verified,
                                        contentDescription = "Verified",
                                        modifier = Modifier.size(14.dp),
                                        tint = Color(0xFF1D9BF0)
                                    )
                                }
                                Text(
                                    text = "Android & Full-Stack Engineer",
                                    style = Typography.bodySmall.copy(fontSize = 11.sp),
                                    color = BrandPrimaryOrange
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        // 4 compact social pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CompactSocialPill("Instagram", Icons.Rounded.CameraAlt, Color(0xFFE1306C), Modifier.weight(1f)) { onOpenUrl(instagramLink) }
                            CompactSocialPill("Play Store", Icons.Rounded.Shop, Color(0xFF10B981), Modifier.weight(1f)) { onOpenUrl(playDevPage) }
                            CompactSocialPill("Portfolio", Icons.Rounded.Language, BrandPrimaryOrange, Modifier.weight(1f)) { onOpenUrl(portfolioLink) }
                            CompactSocialPill("JustU", Icons.Rounded.Smartphone, Color(0xFF6366F1), Modifier.weight(1f)) { onOpenUrl(playStoreApp) }
                        }
                    }
                    MinimalDivider()
                    // Share App Card
                    MinimalSettingRow(
                        icon = Icons.Rounded.QrCode2,
                        iconTint = BrandPrimaryOrange,
                        title = "Share App & QR Contact Card",
                        subtitle = "Instant QR code or share with friends",
                        onClick = { showShareModal = true },
                        trailing = {
                            Icon(Icons.Rounded.ChevronRight, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ── 6. GROUP: SUPPORT & LEGAL ──────────────────────────────────────────
                MinimalSectionLabel("SUPPORT & LEGAL")
                MinimalCardGroup {
                    // Feedback & Bug Report
                    MinimalSettingRow(
                        icon = Icons.Rounded.Feedback,
                        iconTint = Color(0xFFF59E0B),
                        title = "Send Feedback or Feature Request",
                        subtitle = "Suggest new coding platforms or tools",
                        onClick = {
                            initialFeedbackType = "FEATURE_REQUEST"
                            showFeedbackModal = true
                        },
                        trailing = {
                            Icon(Icons.Rounded.ChevronRight, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        }
                    )
                    MinimalDivider()
                    MinimalSettingRow(
                        icon = Icons.Rounded.BugReport,
                        iconTint = Color(0xFFEF4444),
                        title = "Report a Bug / Issue",
                        subtitle = "Report crashes, sync or timer problems",
                        onClick = {
                            initialFeedbackType = "BUG_REPORT"
                            showFeedbackModal = true
                        },
                        trailing = {
                            Icon(Icons.Rounded.ChevronRight, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        }
                    )
                    MinimalDivider()
                    // Privacy Policy
                    MinimalSettingRow(
                        icon = Icons.Rounded.Security,
                        iconTint = Color(0xFF38BDF8),
                        title = "Privacy Policy & Disclosures",
                        onClick = { onOpenUrl("https://vishalbhutekar.netlify.app/myapps/codecalendar/privacy") },
                        trailing = {
                            Icon(Icons.Rounded.ChevronRight, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        }
                    )
                    MinimalDivider()
                    // Account Deletion
                    MinimalSettingRow(
                        icon = Icons.Rounded.DeleteOutline,
                        iconTint = Color(0xFFE11D48),
                        title = "Request Account Deletion",
                        onClick = {
                            if (authMethod != "Guest" && authUsername != "Guest Developer" && !authMethod.isNullOrBlank()) {
                                showDeleteAccountConfirmModal = true
                            } else {
                                onOpenUrl("https://vishalbhutekar.netlify.app/myapps/codecalendar/delete-account")
                            }
                        },
                        trailing = {
                            Icon(Icons.Rounded.ChevronRight, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── 7. SESSION BUTTON ──────────────────────────────────────────────────
                val isGuest = authMethod == "Guest" || authUsername == "Guest Developer" || authMethod.isNullOrBlank()
                if (!isGuest) {
                    OutlinedButton(
                        onClick = { showSignOutConfirmModal = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.06f),
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Logout, null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Sign Out", style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                } else {
                    Button(
                        onClick = onSignOutClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryOrange)
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Login, null, modifier = Modifier.size(17.dp), tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Sign In with Google", style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // App version
                Text(
                    text = "MyCodeCalendar  ·  v1.1.0",
                    style = Typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.40f),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(100.dp))
            }

        // ── NOTIFICATION PERMISSION RATIONALE DIALOG ──────────────────────────
        if (showNotificationPermDialog) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showNotificationPermDialog = false },
                properties = androidx.compose.ui.window.DialogProperties(
                    usePlatformDefaultWidth = false
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.70f))
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.verticalGradient(
                                    if (isDark) listOf(Color(0xFF141824), Color(0xFF0C0F17))
                                    else listOf(Color(0xFFFAFAFC), Color(0xFFF0F0F5))
                                )
                            )
                            .padding(28.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // App Icon / Notification Icon
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        Brush.radialGradient(
                                            listOf(BrandPrimaryOrange.copy(alpha = 0.30f), BrandPrimaryOrange.copy(alpha = 0.08f))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.NotificationsActive,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = BrandPrimaryOrange
                                )
                            }

                            Spacer(Modifier.height(20.dp))

                            Text(
                                text = "Enable Contest Alerts",
                                style = Typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(10.dp))

                            Text(
                                text = "Get notified 15 minutes before your tracked contests begin — so you never miss a round on Codeforces, LeetCode, CodeChef, or HackerEarth.",
                                style = Typography.bodySmall.copy(lineHeight = 19.sp),
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(18.dp))

                            // Feature bullets
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(BrandPrimaryOrange.copy(alpha = if (isDark) 0.08f else 0.05f))
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "🏆  Contest start alerts (15 min early)",
                                    "📢  New hackathon & event announcements",
                                    "🧠  Daily problem of the day reminder",
                                    "✅  Completely ad-free, no spam ever"
                                ).forEach { bullet ->
                                    Text(
                                        text = bullet,
                                        style = Typography.labelSmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
                                        color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)
                                    )
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            // Primary CTA
                            Button(
                                onClick = {
                                    showNotificationPermDialog = false
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        notificationsEnabled = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryOrange)
                            ) {
                                Icon(
                                    Icons.Rounded.NotificationsActive,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.White
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Allow Notifications",
                                    style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }

                            Spacer(Modifier.height(10.dp))

                            // Secondary: Not Now
                            TextButton(
                                onClick = { showNotificationPermDialog = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Not Now",
                                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                    color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                                )
                            }

                            // Settings shortcut (only if permanently denied)
                            TextButton(
                                onClick = {
                                    showNotificationPermDialog = false
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Rounded.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    "Open App Settings",
                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── SIGN OUT CONFIRMATION MODAL (PREMIUM DEEP OBSIDIAN GLASS) ─────────
        if (showSignOutConfirmModal) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showSignOutConfirmModal = false },
                properties = androidx.compose.ui.window.DialogProperties(
                    usePlatformDefaultWidth = false
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f))
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.verticalGradient(
                                    if (isDark) listOf(Color(0xFF141824), Color(0xFF0C0F17))
                                    else listOf(Color(0xFFFAFAFC), Color(0xFFF0F0F5))
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(Color(0xFFEF4444).copy(alpha = 0.14f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.Logout,
                                    contentDescription = null,
                                    modifier = Modifier.size(26.dp),
                                    tint = Color(0xFFEF4444)
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            Text(
                                text = "Sign Out",
                                style = Typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = "Are you sure you want to log out of your session? Your connected platforms and contest alerts will be saved.",
                                style = Typography.bodySmall.copy(lineHeight = 18.sp),
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showSignOutConfirmModal = false },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = null,
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isDark) Color(0xFF1E2535) else Color(0xFFF1F5F9),
                                        contentColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Text(
                                        "Cancel",
                                        style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Button(
                                    onClick = {
                                        showSignOutConfirmModal = false
                                        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                                        onSignOutClick()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = Color(0xFFEF4444).copy(alpha = 0.45f)),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFEF4444)
                                    )
                                ) {
                                    Text(
                                        "Sign Out",
                                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── ACCOUNT DELETION CONFIRMATION MODAL ──────────────────────────────
        if (showDeleteAccountConfirmModal) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showDeleteAccountConfirmModal = false },
                properties = androidx.compose.ui.window.DialogProperties(
                    usePlatformDefaultWidth = false
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (isDark) Color.Black.copy(alpha = 0.80f) else Color.Black.copy(alpha = 0.45f))
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.verticalGradient(
                                    if (isDark) listOf(Color(0xFF181014), Color(0xFF10090D))
                                    else listOf(Color(0xFFFAFAFC), Color(0xFFF0F0F5))
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(Color(0xFFEF4444).copy(alpha = 0.16f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.DeleteForever,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = Color(0xFFEF4444)
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            Text(
                                text = "Request Account Deletion?",
                                style = Typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = "In compliance with Google Play data safety policies, requesting account deletion will queue all your linked profile handles, streak stats, and personal data for permanent removal.",
                                style = Typography.bodySmall.copy(lineHeight = 18.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showDeleteAccountConfirmModal = false },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = null,
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isDark) Color(0xFF1E2535) else Color(0xFFF1F5F9),
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Text(
                                        "Cancel",
                                        style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }

                                Button(
                                    onClick = {
                                        showDeleteAccountConfirmModal = false
                                        onDeleteAccountClick()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = Color(0xFFEF4444).copy(alpha = 0.45f)),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFEF4444)
                                    )
                                ) {
                                    Text(
                                        "Request Delete",
                                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showShareModal) {
            ShareAppContactCardModal(
                onDismiss = { showShareModal = false },
                onShareAppClick = {
                    showShareModal = false
                    onShareApp()
                },
                onOpenUrl = onOpenUrl,
                username = authUsername,
                currentStreak = currentStreak,
                onShareProfileText = { text ->
                    showShareModal = false
                    onShareProfileText(text)
                }
            )
        }

        if (showFeedbackModal) {
            FeedbackReportModal(
                initialType = initialFeedbackType,
                userEmail = authEmail,
                userName = authUsername,
                isDark = isDark,
                onDismiss = { showFeedbackModal = false },
                onSubmit = { type, title, desc, email ->
                    onSubmitFeedback(type, title, desc, email)
                },
                onSendEmailIntent = { subject, body ->
                    val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:vishal.bhutekar1@gmail.com")
                        putExtra(Intent.EXTRA_SUBJECT, subject as String)
                        putExtra(Intent.EXTRA_TEXT, body as String)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(Intent.createChooser(mailIntent, "Send Bug Report / Feature Request"))
                }
            )
        }
    }
}


@Composable
private fun ThemeSegmentOption(
    icon: ImageVector,
    tooltip: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgModifier = if (selected) {
        Modifier
            .background(
                Brush.horizontalGradient(listOf(Color(0xFFFF7A00), Color(0xFFFF5200))),
                RoundedCornerShape(9.dp)
            )
            .shadow(4.dp, RoundedCornerShape(9.dp), spotColor = BrandPrimaryOrange.copy(alpha = 0.4f))
    } else {
        Modifier.clickable(onClick = onClick)
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(9.dp))
            .then(bgModifier),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tooltip,
            modifier = Modifier.size(18.dp),
            tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
        )
    }
}

@Composable
private fun MinimalSectionLabel(text: String) {
    Text(
        text = text,
        style = Typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 0.8.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f),
        modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
    )
}

@Composable
private fun MinimalCardGroup(
    content: @Composable ColumnScope.() -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

@Composable
private fun MinimalSettingRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {}
) {
    val modifier = Modifier
        .fillMaxWidth()
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(horizontal = 16.dp, vertical = 13.dp)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f).padding(end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = iconTint
                )
            }
            Column {
                Text(
                    text = title,
                    style = Typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = subtitle,
                        style = Typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f)
                    )
                }
            }
        }
        trailing()
    }
}

@Composable
private fun MinimalDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
        thickness = 0.8.dp
    )
}

@Composable
private fun CompactSocialPill(
    label: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(tint.copy(alpha = 0.10f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(13.dp), tint = tint)
            Text(
                text = label,
                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.5.sp),
                color = tint
            )
        }
    }
}

@Composable
private fun minimalSwitchColors(): SwitchColors {
    return SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = BrandPrimaryOrange,
        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
        uncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    )
}

@Composable
fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(
                text = title,
                style = Typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = subtitle,
                style = Typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = BrandPrimaryOrange,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun UnifiedDeveloperLinkChip(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
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
                modifier = Modifier.size(15.dp),
                tint = BrandPrimaryOrange
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

@Composable
private fun CreatorLinkTile(
    title: String,
    subtitle: String,
    isVerified: Boolean,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f
    Box(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isDark) Color(0xFF131A26).copy(alpha = 0.85f)
                else Color(0xFFF1F5F9).copy(alpha = 0.85f)
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            icon()

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = subtitle,
                        style = Typography.labelSmall.copy(fontSize = 9.2.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                        maxLines = 1
                    )
                    if (isVerified) {
                        Icon(
                            imageVector = Icons.Rounded.Verified,
                            contentDescription = "Verified",
                            modifier = Modifier.size(11.dp),
                            tint = Color(0xFF1D9BF0)
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
            )
        }
    }
}

@Composable
private fun NotificationPrefCard(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isDark) {
        Color(0xFF131A26).copy(alpha = 0.75f)
    } else {
        Color(0xFFFFFFFF).copy(alpha = 0.85f)
    }
    val borderColor = if (checked) {
        iconTint.copy(alpha = 0.35f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = iconTint
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (checked) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(iconTint.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                style = Typography.labelSmall.copy(
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = iconTint
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = Typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = iconTint,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    uncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
private fun FeedbackReportModal(
    initialType: String,
    userEmail: String?,
    userName: String?,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (type: String, title: String, description: String, email: String) -> Unit,
    onSendEmailIntent: (subject: String, body: String) -> Unit
) {
    var selectedType by remember { mutableStateOf(initialType) }
    var titleText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf(userEmail ?: "") }
    var includeDeviceInfo by remember { mutableStateOf(true) }
    var isSubmitted by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    val accentColor = when (selectedType) {
        "BUG_REPORT" -> Color(0xFFEF4444)
        "FEATURE_REQUEST" -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }

    val typeLabel = when (selectedType) {
        "BUG_REPORT" -> "Bug Report"
        "FEATURE_REQUEST" -> "Feature Suggestion"
        else -> "General Feedback"
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(horizontal = 20.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        if (isDark) Color(0xFF111726) else Color(0xFFFAFAFC)
                    )
                    .border(
                        1.dp,
                        accentColor.copy(alpha = 0.35f),
                        RoundedCornerShape(26.dp)
                    )
                    .padding(22.dp)
            ) {
                if (isSubmitted) {
                    // Success View
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22C55E).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = Color(0xFF22C55E)
                            )
                        }

                        Text(
                            text = "Report Sent Successfully!",
                            style = Typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Thank you! Your feedback has been recorded and an email was prepared for developer Vishal Bhutekar. We appreciate you making CodeCalendar better!",
                            style = Typography.bodySmall.copy(lineHeight = 18.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(10.dp))

                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryOrange)
                        ) {
                            Text(
                                "Done",
                                style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                } else {
                    // Form View
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Header
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
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(11.dp))
                                        .background(accentColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (selectedType) {
                                            "BUG_REPORT" -> Icons.Rounded.BugReport
                                            "FEATURE_REQUEST" -> Icons.Rounded.Lightbulb
                                            else -> Icons.Rounded.Feedback
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = accentColor
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Send Feedback & Ideas",
                                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Direct to Vishal Bhutekar",
                                        style = Typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f)
                                    )
                                }
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Close,
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Type Selector Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "FEATURE_REQUEST" to "💡 Feature",
                                "BUG_REPORT" to "🐛 Bug",
                                "GENERAL_FEEDBACK" to "💬 Feedback"
                            ).forEach { (typeKey, label) ->
                                val selected = selectedType == typeKey
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (selected) accentColor.copy(alpha = 0.18f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                        )
                                        .border(
                                            1.dp,
                                            if (selected) accentColor else Color.Transparent,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { selectedType = typeKey }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = Typography.labelSmall.copy(
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp
                                        ),
                                        color = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Title field
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = if (selectedType == "BUG_REPORT") "Issue Title *" else "Feature / Topic *",
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            OutlinedTextField(
                                value = titleText,
                                onValueChange = {
                                    titleText = it
                                    if (showError && it.isNotBlank()) showError = false
                                },
                                placeholder = {
                                    Text(
                                        text = if (selectedType == "BUG_REPORT") "e.g. Timer displays wrong remaining time"
                                        else "e.g. Add AtCoder or Kaggle support",
                                        style = Typography.bodySmall.copy(fontSize = 12.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                isError = showError && titleText.isBlank()
                            )
                        }

                        // Description field
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Details & Description *",
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            OutlinedTextField(
                                value = descriptionText,
                                onValueChange = {
                                    descriptionText = it
                                    if (showError && it.isNotBlank()) showError = false
                                },
                                placeholder = {
                                    Text(
                                        text = if (selectedType == "BUG_REPORT") "What happened? Steps to reproduce the bug..."
                                        else "Describe how this feature would help you compete better...",
                                        style = Typography.bodySmall.copy(fontSize = 12.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                    )
                                },
                                minLines = 4,
                                maxLines = 7,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                isError = showError && descriptionText.isBlank()
                            )
                        }

                        // Email field
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Your Email (optional)",
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            OutlinedTextField(
                                value = contactEmail,
                                onValueChange = { contactEmail = it },
                                placeholder = {
                                    Text(
                                        text = "To receive updates on your request",
                                        style = Typography.bodySmall.copy(fontSize = 12.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Diagnostic info checkbox
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                .clickable { includeDeviceInfo = !includeDeviceInfo }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Checkbox(
                                checked = includeDeviceInfo,
                                onCheckedChange = { includeDeviceInfo = it },
                                colors = CheckboxDefaults.colors(checkedColor = accentColor)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Attach System Diagnostics",
                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${Build.MANUFACTURER} ${Build.MODEL} · Android ${Build.VERSION.RELEASE} · v1.1.0",
                                    style = Typography.bodySmall.copy(fontSize = 10.5.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f)
                                )
                            }
                        }

                        if (showError) {
                            Text(
                                text = "Please enter both a title and description.",
                                style = Typography.labelSmall,
                                color = Color(0xFFEF4444)
                            )
                        }

                        // Submit CTA
                        Button(
                            onClick = {
                                if (titleText.isBlank() || descriptionText.isBlank()) {
                                    showError = true
                                    return@Button
                                }
                                // 1. Cloud Firestore
                                onSubmit(selectedType, titleText.trim(), descriptionText.trim(), contactEmail.trim())

                                // 2. Direct email dispatch
                                val subject = "[MyCodeCalendar] [$typeLabel] ${titleText.trim()}"
                                val body = buildString {
                                    appendLine("=== CodeCalendar $typeLabel ===")
                                    appendLine("Submitted By: ${userName ?: "Developer"} (${contactEmail.ifBlank { "No email provided" }})")
                                    appendLine("Title: ${titleText.trim()}")
                                    appendLine()
                                    appendLine("--- Description ---")
                                    appendLine(descriptionText.trim())
                                    appendLine()
                                    if (includeDeviceInfo) {
                                        appendLine("--- System & Diagnostic Info ---")
                                        appendLine("App Version: 1.1.0 (Build 101)")
                                        appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
                                        appendLine("Android OS: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                                        appendLine("Timestamp: ${java.util.Date()}")
                                    }
                                }
                                onSendEmailIntent(subject, body)
                                isSubmitted = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(13.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.Send,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Submit & Send to Developer",
                                style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

