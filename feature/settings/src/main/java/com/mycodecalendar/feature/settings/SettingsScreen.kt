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
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    currentStreak: Int = 14,
    onSignOutClick: () -> Unit = {},
    onDeleteAccountClick: () -> Unit = {},
    onReplayOnboardingClick: () -> Unit = {},
    onShareApp: () -> Unit = {},
    onShareProfileText: (String) -> Unit = {},
    onOpenUrl: (String) -> Unit = {}
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var calendarSyncEnabled by remember { mutableStateOf(true) }
    var showShareModal by remember { mutableStateOf(false) }
    var showSignOutConfirmModal by remember { mutableStateOf(false) }
    var showDeleteAccountConfirmModal by remember { mutableStateOf(false) }
    val isDark = isAppInDarkTheme

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
                    .padding(horizontal = 22.dp)
                    .padding(top = 24.dp, bottom = 8.dp)
            ) {
                Text(
                    text = "Settings",
                    style = Typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Accounts, preferences, and developer profile",
                    style = Typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── DEVELOPER ACCOUNT & SESSION ─────────────────────────────────────────
            SectionHeader(title = "Account & Profile", modifier = Modifier.padding(horizontal = 22.dp))
            Spacer(modifier = Modifier.height(10.dp))

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                cornerRadius = 20.dp,
                accentColor = BrandPrimaryOrange
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    BrandPrimaryOrange.copy(alpha = 0.15f),
                                    CircleShape
                                )
                                .border(1.dp, BrandPrimaryOrange.copy(alpha = 0.35f), CircleShape),
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
                                    modifier = Modifier.size(22.dp),
                                    tint = BrandPrimaryOrange
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = authUsername ?: "Guest Developer",
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (!authEmail.isNullOrBlank()) {
                                Text(
                                    text = authEmail,
                                    style = Typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
                                )
                            }
                            Text(
                                text = "Signed in via ${authMethod ?: "Guest Mode"}",
                                style = Typography.labelSmall,
                                color = BrandPrimaryOrange
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(10.dp))

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
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                        )
                    }

                    if (authMethod != "Guest" && authUsername != "Guest Developer" && !authMethod.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.06f))
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDeleteAccountConfirmModal = true }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.DeleteForever,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = "Request Account & Data Deletion",
                                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.90f)
                                )
                            }
                            Icon(
                                Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.45f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── CONNECTED PLATFORMS ──────────────────────────────────────────────────
            SectionHeader(title = "Connected Platforms", modifier = Modifier.padding(horizontal = 22.dp))
            Spacer(modifier = Modifier.height(10.dp))

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                cornerRadius = 18.dp
            ) {
                if (connectedAccounts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
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
                                    .padding(horizontal = 18.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
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
                                    modifier = Modifier.padding(horizontal = 18.dp),
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Connect button with 1px border
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                accentColor = BrandPrimaryOrange,
                cornerRadius = 16.dp,
                onClick = onAddPlatformClick
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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

            Spacer(modifier = Modifier.height(28.dp))

            // ── SHARE & COMMUNITY ───────────────────────────────────────────────────
            SectionHeader(title = "Share & Community", modifier = Modifier.padding(horizontal = 22.dp))
            Spacer(modifier = Modifier.height(10.dp))

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                cornerRadius = 20.dp,
                accentColor = BrandPrimaryOrange
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showShareModal = true },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
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

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenUrl(playDevPage) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(Color(0xFF10B981).copy(alpha = 0.15f), CircleShape)
                                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Shop,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = Color(0xFF10B981)
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
                            tint = Color(0xFF10B981)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── MEET THE CREATOR / DEVELOPER SHOWCASE CARD ──────────────────────────
            SectionHeader(title = "Meet the Creator", modifier = Modifier.padding(horizontal = 22.dp))
            Spacer(modifier = Modifier.height(10.dp))

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                cornerRadius = 22.dp,
                accentColor = BrandPrimaryOrange
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
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

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = "Vishal Bhutekar",
                                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                // Verified Developer Blue Tick Badge (Authentic Platform Verified Badge)
                                Icon(
                                    imageVector = Icons.Rounded.Verified,
                                    contentDescription = "Verified Developer",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFF1D9BF0) // Verified Blue
                                )
                            }
                            Text(
                                text = "Android & Full-Stack Engineer",
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = BrandPrimaryOrange
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Passionate mobile engineer crafting modern, fluid, and state-of-the-art Android experiences for developers worldwide.",
                        style = Typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── CREATOR QUICK LINKS (Instagram, Google Play Store, Portfolio, JustU Launcher) ──
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Row 1: Instagram & Google Play Store
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Instagram Card
                            CreatorLinkTile(
                                title = "Instagram",
                                subtitle = "@unexplored_vish_2.0",
                                isVerified = true,
                                icon = {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFFCB045))
                                                ),
                                                RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.CameraAlt,
                                            contentDescription = "Instagram",
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.White
                                        )
                                    }
                                },
                                onClick = { onOpenUrl(instagramLink) },
                                modifier = Modifier.weight(1f)
                            )

                            // Google Play Developer Card
                            CreatorLinkTile(
                                title = "Play Store",
                                subtitle = "Developer Apps",
                                isVerified = false,
                                icon = {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFF00C9FF), Color(0xFF92FE9D))
                                                ),
                                                RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Shop,
                                            contentDescription = "Play Store",
                                            modifier = Modifier.size(16.dp),
                                            tint = Color(0xFF064E3B)
                                        )
                                    }
                                },
                                onClick = { onOpenUrl(playDevPage) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Row 2: Portfolio & JustU Launcher
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Portfolio Card
                            CreatorLinkTile(
                                title = "Portfolio",
                                subtitle = "Web & Projects",
                                isVerified = false,
                                icon = {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(BrandPrimaryOrange.copy(alpha = 0.18f), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Language,
                                            contentDescription = "Portfolio",
                                            modifier = Modifier.size(16.dp),
                                            tint = BrandPrimaryOrange
                                        )
                                    }
                                },
                                onClick = { onOpenUrl(portfolioLink) },
                                modifier = Modifier.weight(1f)
                            )

                            // JustU Launcher Card
                            CreatorLinkTile(
                                title = "JustU Launcher",
                                subtitle = "Minimalist App",
                                isVerified = false,
                                icon = {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(BrandPurpleAccent.copy(alpha = 0.18f), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Smartphone,
                                            contentDescription = "JustU Launcher",
                                            modifier = Modifier.size(16.dp),
                                            tint = BrandPurpleAccent
                                        )
                                    }
                                },
                                onClick = { onOpenUrl(playStoreApp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── PRIVACY POLICY & TERMS CARD ─────────────────────────────────────────
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                cornerRadius = 16.dp,
                accentColor = BrandPrimaryOrange,
                onClick = { onOpenUrl("https://vishalbhutekar.netlify.app/myapps/codecalendar/privacy") }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(BrandPrimaryOrange.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Security,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = BrandPrimaryOrange
                            )
                        }

                        Column {
                            Text(
                                text = "Privacy Policy & Terms",
                                style = Typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Click to open",
                                style = Typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── COPYRIGHT & EDUCATIONAL DISCLAIMER CARD (0.3px Red Border) ────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(0.1.dp, Color(0xFFEF4444).copy(alpha = 0.50f), RoundedCornerShape(16.dp))
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFEF4444).copy(alpha = 0.12f), CircleShape)
                                .border(0.1.dp, Color(0xFFEF4444).copy(alpha = 0.35f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Copyright,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFFEF4444)
                            )
                        }

                        Column {
                            Text(
                                text = "Educational Purpose & Copyright",
                                style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "All contest schedules, logos, and practice sheets belong to their respective copyright holders. This application operates strictly as a non-commercial educational aggregator under fair use.\n\nFor content removal inquiries, contact: vishal.bhutekar1@gmail.com",
                                style = Typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── NOTIFICATIONS ────────────────────────────────────────────────────────
            SectionHeader(title = "Notifications", modifier = Modifier.padding(horizontal = 22.dp))
            Spacer(modifier = Modifier.height(10.dp))

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                cornerRadius = 18.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    SettingSwitchRow(
                        title = "Contest Alarms & Reminders",
                        subtitle = "Alert 15 minutes before tracked contests start",
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(10.dp))
                    SettingSwitchRow(
                        title = "Calendar Auto-Sync",
                        subtitle = "Automatically export registered contests to device calendar",
                        checked = calendarSyncEnabled,
                        onCheckedChange = { calendarSyncEnabled = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── APPEARANCE (COMPACT ROUNDED SEGMENTED THEME SWITCHER) ────────────────────
            SectionHeader(title = "Appearance", modifier = Modifier.padding(horizontal = 22.dp))
            Spacer(modifier = Modifier.height(10.dp))

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                cornerRadius = 18.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Theme Mode",
                            style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when (currentTheme) {
                                AppTheme.DARK   -> "Dark (OLED Obsidian)"
                                AppTheme.LIGHT  -> "Light (Daylight)"
                                AppTheme.SYSTEM -> "System Default"
                            },
                            style = Typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                        )
                    }

                    // Compact Segmented Rounded Control
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isAppInDarkTheme) 0.50f else 0.80f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ThemeSegmentOption(
                            icon = Icons.Rounded.DarkMode,
                            tooltip = "Dark Mode",
                            selected = currentTheme == AppTheme.DARK,
                            onClick = { onThemeChange(AppTheme.DARK) }
                        )
                        ThemeSegmentOption(
                            icon = Icons.Rounded.LightMode,
                            tooltip = "Light Mode",
                            selected = currentTheme == AppTheme.LIGHT,
                            onClick = { onThemeChange(AppTheme.LIGHT) }
                        )
                        ThemeSegmentOption(
                            icon = Icons.Rounded.PhoneAndroid,
                            tooltip = "System Default",
                            selected = currentTheme == AppTheme.SYSTEM,
                            onClick = { onThemeChange(AppTheme.SYSTEM) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            val isGuest = authMethod == "Guest" || authUsername == "Guest Developer" || authMethod.isNullOrBlank()

            if (!isGuest) {
                // ── DEDICATED SIGN OUT CARD (Logged In Users Only) ───────────────────
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    accentColor = MaterialTheme.colorScheme.error,
                    cornerRadius = 18.dp,
                    onClick = { showSignOutConfirmModal = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sign Out of Developer Session",
                            style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            } else {
                // ── SIGN IN / CREATE ACCOUNT CARD (Guest Mode) ───────────────────────
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    accentColor = BrandPrimaryOrange,
                    cornerRadius = 18.dp,
                    onClick = onSignOutClick
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Login,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = BrandPrimaryOrange
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sign In to Sync & Save Platforms",
                            style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = BrandPrimaryOrange
                        )
                    }
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
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f))
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "MyCodeCalendar  ·  v1.0.0",
                    style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                )
            }

            Spacer(modifier = Modifier.height(120.dp))
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
                            .border(
                                BorderStroke(
                                    0.1.dp,
                                    Brush.verticalGradient(
                                        if (isDark) listOf(Color(0xFFEF4444).copy(alpha = 0.55f), Color(0x22FFFFFF))
                                        else listOf(Color(0xFFEF4444).copy(alpha = 0.40f), Color(0xFFEF4444).copy(alpha = 0.15f))
                                    )
                                ),
                                RoundedCornerShape(24.dp)
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
                                    .background(Color(0xFFEF4444).copy(alpha = 0.14f), CircleShape)
                                    .border(1.2.dp, Color(0xFFEF4444).copy(alpha = 0.45f), CircleShape),
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
                                    border = BorderStroke(0.1.dp, if (isDark) Color(0x33FFFFFF) else MaterialTheme.colorScheme.outline.copy(alpha = 0.30f))
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
                            .border(
                                BorderStroke(
                                    0.1.dp,
                                    Brush.verticalGradient(
                                        if (isDark) listOf(Color(0xFFEF4444).copy(alpha = 0.65f), Color(0x22FFFFFF))
                                        else listOf(Color(0xFFEF4444).copy(alpha = 0.50f), Color(0xFFEF4444).copy(alpha = 0.20f))
                                    )
                                ),
                                RoundedCornerShape(24.dp)
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
                                    .background(Color(0xFFEF4444).copy(alpha = 0.16f), CircleShape)
                                    .border(1.2.dp, Color(0xFFEF4444).copy(alpha = 0.50f), CircleShape),
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
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
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

/**
 * Modern compact tile for Creator social / store links with custom icon & verified tick.
 */
@Composable
private fun CreatorLinkTile(
    title: String,
    subtitle: String,
    isVerified: Boolean,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(0.1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
        modifier = modifier.height(58.dp)
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
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.40f)
            )
        }
    }
}
