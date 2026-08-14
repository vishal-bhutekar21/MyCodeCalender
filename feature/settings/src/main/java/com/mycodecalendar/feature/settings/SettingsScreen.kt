package com.mycodecalendar.feature.settings

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycodecalendar.core.designsystem.AppTheme
import com.mycodecalendar.core.designsystem.BrandPrimaryOrange
import com.mycodecalendar.core.designsystem.BrandPurpleAccent
import com.mycodecalendar.core.designsystem.GlassmorphismBackground
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.core.designsystem.components.GlassCard
import com.mycodecalendar.core.designsystem.components.PlatformBadge
import com.mycodecalendar.core.designsystem.components.SectionHeader
import com.mycodecalendar.domain.model.PlatformAccount

/**
 * SettingsScreen — Clean, Unified, Cohesive Settings & Developer Showcase.
 *
 * Design Improvements:
 * - Removed rainbow color clutter (replaced disparate pastel chips with unified dark slate glass chips).
 * - Standardized all form controls, switches, radio buttons, and buttons to BrandPrimaryOrange (#FF6B00).
 * - Cohesive obsidian cards with clean 1px borders.
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
    currentStreak: Int = 14,
    onSignOutClick: () -> Unit = {},
    onReplayOnboardingClick: () -> Unit = {},
    onShareApp: () -> Unit = {},
    onShareProfileText: (String) -> Unit = {},
    onOpenUrl: (String) -> Unit = {}
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var calendarSyncEnabled by remember { mutableStateOf(true) }
    var showShareModal by remember { mutableStateOf(false) }

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
                .verticalScroll(rememberScrollState())
        ) {
            // ── PAGE HEADER ─────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp, bottom = 6.dp)
            ) {
                Text(
                    text = "Settings",
                    style = Typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Accounts, preferences, and developer info",
                    style = Typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── DEVELOPER ACCOUNT & SESSION ─────────────────────────────────────────
            SectionHeader(title = "Account & Profile", modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(modifier = Modifier.height(8.dp))

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                cornerRadius = 18.dp,
                accentColor = BrandPrimaryOrange
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                    .size(44.dp)
                                    .background(
                                        BrandPrimaryOrange.copy(alpha = 0.15f),
                                        CircleShape
                                    )
                                    .border(1.dp, BrandPrimaryOrange.copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp),
                                    tint = BrandPrimaryOrange
                                )
                            }

                            Column {
                                Text(
                                    text = authUsername ?: "Guest Developer",
                                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Signed in via ${authMethod ?: "Guest Mode"}",
                                    style = Typography.labelSmall,
                                    color = BrandPrimaryOrange
                                )
                            }
                        }

                        TextButton(onClick = onSignOutClick) {
                            Text(
                                text = "Switch",
                                style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onReplayOnboardingClick)
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = BrandPrimaryOrange
                            )
                            Text(
                                text = "Replay App Tour & Onboarding",
                                style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = { showShareModal = true })
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Rounded.LocalFireDepartment,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = BrandPrimaryOrange
                            )
                            Text(
                                text = "Share Profile & Streak Card ($currentStreak Days)",
                                style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            Icons.Rounded.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = BrandPrimaryOrange
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── CONNECTED PLATFORMS ──────────────────────────────────────────────────
            SectionHeader(title = "Connected Platforms", modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(modifier = Modifier.height(8.dp))

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                cornerRadius = 16.dp
            ) {
                if (connectedAccounts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No platforms connected yet",
                            style = Typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    Column {
                        connectedAccounts.forEachIndexed { index, account ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onManageAccountClick(account) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    PlatformBadge(platform = account.platform)
                                    Column {
                                        Text(
                                            text = "@${account.username}",
                                            style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        account.displayName?.let { name ->
                                            Text(
                                                text = "$name  ·  ${account.platform.name}",
                                                style = Typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Rounded.ChevronRight,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                )
                            }
                            if (index < connectedAccounts.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Connect button with 1px border
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                accentColor = BrandPrimaryOrange,
                cornerRadius = 14.dp,
                onClick = onAddPlatformClick
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = BrandPrimaryOrange
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Connect a Platform",
                        style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = BrandPrimaryOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── SHARE & COMMUNITY ───────────────────────────────────────────────────
            SectionHeader(title = "Share & Community", modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(modifier = Modifier.height(8.dp))

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                cornerRadius = 18.dp,
                accentColor = BrandPrimaryOrange
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showShareModal = true },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(BrandPrimaryOrange.copy(alpha = 0.15f), CircleShape)
                                    .border(1.dp, BrandPrimaryOrange.copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.QrCode2,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = BrandPrimaryOrange
                                )
                            }

                            Column {
                                Text(
                                    text = "Share App & QR Contact Card",
                                    style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Generate instant QR code or share with friends",
                                    style = Typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                )
                            }
                        }

                        Icon(
                            Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = BrandPrimaryOrange
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenUrl(playDevPage) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(BrandPurpleAccent.copy(alpha = 0.15f), CircleShape)
                                    .border(1.dp, BrandPurpleAccent.copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.StarRate,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = BrandPurpleAccent
                                )
                            }

                            Column {
                                Text(
                                    text = "Google Play Developer Page",
                                    style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Discover more published apps & rate us",
                                    style = Typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                )
                            }
                        }

                        Icon(
                            Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = BrandPurpleAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── MEET THE CREATOR / DEVELOPER SHOWCASE CARD ──────────────────────────
            SectionHeader(title = "Meet the Creator", modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(modifier = Modifier.height(8.dp))

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                cornerRadius = 20.dp,
                accentColor = BrandPrimaryOrange
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                    .size(46.dp)
                                    .background(BrandPrimaryOrange.copy(alpha = 0.15f), CircleShape)
                                    .border(1.dp, BrandPrimaryOrange.copy(alpha = 0.40f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Terminal,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = BrandPrimaryOrange
                                )
                            }

                            Column {
                                Text(
                                    text = "Vishal Bhutekar",
                                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Android & Full-Stack Engineer",
                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = BrandPrimaryOrange
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BrandPrimaryOrange.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, BrandPrimaryOrange.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = "Verified Dev",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = BrandPrimaryOrange
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Passionate mobile engineer crafting modern, fluid, and state-of-the-art Android experiences for developers worldwide.",
                        style = Typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Social buttons grid — Unified Slate Cards with Brand Styling
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            UnifiedDeveloperLinkChip("Portfolio", Icons.Rounded.Language, Modifier.weight(1f)) {
                                onOpenUrl(portfolioLink)
                            }
                            UnifiedDeveloperLinkChip("LinkedIn", Icons.Rounded.Work, Modifier.weight(1f)) {
                                onOpenUrl(linkedInLink)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            UnifiedDeveloperLinkChip("Instagram", Icons.Rounded.CameraAlt, Modifier.weight(1f)) {
                                onOpenUrl(instagramLink)
                            }
                            UnifiedDeveloperLinkChip("GitHub", Icons.Rounded.Code, Modifier.weight(1f)) {
                                onOpenUrl(githubLink)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            UnifiedDeveloperLinkChip("JustU Launcher", Icons.Rounded.Shop, Modifier.weight(1f)) {
                                onOpenUrl(playStoreApp)
                            }
                            UnifiedDeveloperLinkChip("Email Me", Icons.Rounded.Email, Modifier.weight(1f)) {
                                onOpenUrl(emailLink)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── NOTIFICATIONS ────────────────────────────────────────────────────────
            SectionHeader(title = "Notifications", modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(modifier = Modifier.height(8.dp))

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                cornerRadius = 16.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingSwitchRow(
                        title = "Contest Alarms & Reminders",
                        subtitle = "Alert 15 minutes before tracked contests start",
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f))
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingSwitchRow(
                        title = "Calendar Auto-Sync",
                        subtitle = "Automatically export registered contests to device calendar",
                        checked = calendarSyncEnabled,
                        onCheckedChange = { calendarSyncEnabled = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── APPEARANCE ───────────────────────────────────────────────────────────
            SectionHeader(title = "Appearance", modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(modifier = Modifier.height(8.dp))

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                cornerRadius = 16.dp
            ) {
                Column {
                    ThemeRow(
                        label = "Dark Mode",
                        subtitle = "OLED Obsidian high-contrast theme",
                        icon = Icons.Rounded.DarkMode,
                        selected = currentTheme == AppTheme.DARK,
                        onClick = { onThemeChange(AppTheme.DARK) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
                    )
                    ThemeRow(
                        label = "Light Mode",
                        subtitle = "Clean high-luminance theme",
                        icon = Icons.Rounded.LightMode,
                        selected = currentTheme == AppTheme.LIGHT,
                        onClick = { onThemeChange(AppTheme.LIGHT) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
                    )
                    ThemeRow(
                        label = "System Default",
                        subtitle = "Follow device setting",
                        icon = Icons.Rounded.PhoneAndroid,
                        selected = currentTheme == AppTheme.SYSTEM,
                        onClick = { onThemeChange(AppTheme.SYSTEM) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "MyCodeCalendar  ·  v1.0.0",
                    style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
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
    }
}

@Composable
private fun ThemeRow(
    label: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (selected) BrandPrimaryOrange
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = label,
                    style = Typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                    color = if (selected) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                )
                Text(
                    text = subtitle,
                    style = Typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = BrandPrimaryOrange,
                unselectedColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
            )
        )
    }
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
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
        modifier = modifier.height(38.dp)
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
