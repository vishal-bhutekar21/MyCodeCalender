package com.mycodecalendar.feature.home

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mycodecalendar.core.designsystem.BrandPrimaryOrange
import com.mycodecalendar.core.designsystem.GlassmorphismBackground
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.core.designsystem.components.GlassCard

/**
 * Dedicated, state-of-the-art Activity & Announcement detail page for Hackathons & Broadcasts.
 */
@Composable
fun BroadcastDetailScreen(
    broadcast: CloudBroadcastBanner?,
    onBackClick: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val item = broadcast ?: CloudBroadcastBanner(
        title = "Innovik 6.0 – International Hackathon 2026",
        subtitle = "₹2,00,000 Prizes · Vikrant Institute of Technology and Management, Indore",
        actionUrl = "https://unstop.com",
        badge = "HACKATHON",
        bannerImageUrl = "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?q=80&w=1000&auto=format&fit=crop",
        description = "Welcome to INNOVIK 6.0 – International Hackathon 2026!\n\nOrganized by Vikrant Institute of Technology and Management (VITM), Indore. This flagship international coding competition brings together brilliant student developers, problem solvers, and innovators across Applied AI, Agentic AI, Web3, Smart Cities, and Open Innovation.",
        prizePool = "₹ 2,00,000",
        location = "VITM Campus, Indore (A.B. Road)",
        teamSize = "2 - 4 Members",
        timeline = "06 Aug 2026 – 25 Aug 2026",
        tags = listOf("Applied AI", "Agentic AI", "Hackathon", "₹2,00,000 Prizes", "Unstop", "Offline Finale")
    )

    val onShareClick = {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, item.title)
            putExtra(
                Intent.EXTRA_TEXT,
                "🚀 ${item.title}\n\n${item.subtitle}\n\n🏆 Prizes: ${item.prizePool}\n📍 Location: ${item.location}\n📅 Timeline: ${item.timeline}\n\nRegister here: ${item.actionUrl.ifBlank { "https://unstop.com" }}"
            )
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Announcement"))
    }

    GlassmorphismBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 90.dp)
            ) {
                // ── TOP APP BAR ──────────────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassCard(
                        cornerRadius = 20.dp,
                        onClick = onBackClick
                    ) {
                        Box(
                            modifier = Modifier.size(42.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Text(
                        text = "Event & Broadcast",
                        style = Typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    GlassCard(
                        cornerRadius = 20.dp,
                        onClick = onShareClick
                    ) {
                        Box(
                            modifier = Modifier.size(42.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Share,
                                contentDescription = "Share",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // ── HERO POSTER / BANNER IMAGE ───────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 6.dp)
                ) {
                    if (item.bannerImageUrl.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .clip(RoundedCornerShape(26.dp))
                                .border(
                                    1.2.dp,
                                    BrandPrimaryOrange.copy(alpha = 0.35f),
                                    RoundedCornerShape(26.dp)
                                )
                                .shadow(16.dp, RoundedCornerShape(26.dp), spotColor = BrandPrimaryOrange.copy(alpha = 0.25f))
                        ) {
                            AsyncImage(
                                model = item.bannerImageUrl,
                                contentDescription = item.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Subtle gradient overlay for readability
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                                        )
                                    )
                            )
                            // Live Badge in Banner
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = BrandPrimaryOrange
                                ) {
                                    Text(
                                        text = item.badge.uppercase(),
                                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 10.sp),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Black.copy(alpha = 0.65f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(Color(0xFF10B981), CircleShape)
                                        )
                                        Text(
                                            text = "LIVE ANNOUNCEMENT",
                                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.5.sp),
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Cyberpunk Neon Fallback Hero Card
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 26.dp,
                            accentColor = BrandPrimaryOrange
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                BrandPrimaryOrange.copy(alpha = 0.18f),
                                                Color(0xFF1E2235),
                                                Color(0xFF0F121C)
                                            )
                                        )
                                    )
                                    .padding(20.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = BrandPrimaryOrange
                                    ) {
                                        Text(
                                            text = item.badge.uppercase(),
                                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 10.sp),
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                        )
                                    }
                                    Text(
                                        text = item.title,
                                        style = Typography.headlineSmall.copy(fontWeight = FontWeight.Black, fontSize = 20.sp),
                                        color = Color.White,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ── TITLE & SUBTITLE SECTION ─────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = item.title,
                        style = Typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 23.sp,
                            letterSpacing = (-0.4).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    if (item.subtitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = item.subtitle,
                            style = Typography.bodyMedium.copy(
                                fontSize = 13.5.sp,
                                lineHeight = 19.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── QUICK HIGHLIGHT METRICS GRID ─────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Prize Pool Card
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        cornerRadius = 18.dp,
                        accentColor = Color(0xFFF59E0B)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Rounded.EmojiEvents,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color(0xFFF59E0B)
                            )
                            Text(
                                text = "Prizes Worth",
                                style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                text = item.prizePool.ifBlank { "₹2,00,000" },
                                style = Typography.titleSmall.copy(fontWeight = FontWeight.Black, fontSize = 14.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Team Size Card
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        cornerRadius = 18.dp,
                        accentColor = Color(0xFF3B82F6)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Groups,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color(0xFF3B82F6)
                            )
                            Text(
                                text = "Team Size",
                                style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                text = item.teamSize.ifBlank { "2 - 4 Members" },
                                style = Typography.titleSmall.copy(fontWeight = FontWeight.Black, fontSize = 14.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Timeline Card
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        cornerRadius = 18.dp,
                        accentColor = BrandPrimaryOrange
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Rounded.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = BrandPrimaryOrange
                            )
                            Text(
                                text = "Key Dates",
                                style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                text = item.timeline.ifBlank { "Aug 2026" },
                                style = Typography.titleSmall.copy(fontWeight = FontWeight.Black, fontSize = 12.5.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Location Card
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        cornerRadius = 18.dp,
                        accentColor = Color(0xFF10B981)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Rounded.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color(0xFF10B981)
                            )
                            Text(
                                text = "Location / Mode",
                                style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                text = item.location.ifBlank { "Online + Campus" },
                                style = Typography.titleSmall.copy(fontWeight = FontWeight.Black, fontSize = 12.5.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── DETAILED DESCRIPTION CARD ────────────────────────────────────────
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                    cornerRadius = 22.dp
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Info,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = BrandPrimaryOrange
                            )
                            Text(
                                text = "About & Guidelines",
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = item.description.ifBlank {
                                "Innovik 6.0 is an International Hackathon organized by Vikrant Group of Institutions (VITM), Indore. Teams will submit a 10-slide solution PPT tackling real-world problems before heading into the grand finale."
                            },
                            style = Typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 20.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ── STAGES & TIMELINE BREAKDOWN ──────────────────────────────────────
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                    cornerRadius = 22.dp
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Timeline,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = BrandPrimaryOrange
                            )
                            Text(
                                text = "Stages & Deadlines",
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Round 1
                        TimelineStageItem(
                            stepNumber = "1",
                            title = "Team Registration & Idea PPT Submission",
                            timeline = "06 Aug 2026, 02:40 AM IST – 25 Aug 2026, 02:40 AM IST",
                            desc = "Online screening round on Unstop. Pick a theme statement and submit a 10-slide presentation."
                        )

                        // Round 2
                        TimelineStageItem(
                            stepNumber = "2",
                            title = "Shortlisting & Mentorship Round",
                            timeline = "August – September 2026",
                            desc = "Jury evaluation of submitted solutions and announcement of finalist teams."
                        )

                        // Round 3
                        TimelineStageItem(
                            stepNumber = "3",
                            title = "Grand Finale Offline Hackathon",
                            timeline = "VITM Indore Campus",
                            desc = "24-36 Hour intense prototyping, coding, prototype demo and live pitching for ₹2,00,000 prizes."
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ── TAGS / DOMAINS CLOUD ─────────────────────────────────────────────
                if (item.tags.isNotEmpty()) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp),
                        cornerRadius = 22.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Eligible Tracks & Themes",
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            OptInFlowRow(
                                tags = item.tags
                            )
                        }
                    }
                }
            }

            // ── FLOATING ACTION BAR AT BOTTOM ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Button(
                    onClick = {
                        val targetUrl = item.actionUrl.ifBlank { "https://unstop.com" }
                        onOpenUrl(targetUrl)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = BrandPrimaryOrange.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandPrimaryOrange,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        Icons.Rounded.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Register / Open Official Portal",
                        style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineStageItem(
    stepNumber: String,
    title: String,
    timeline: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(BrandPrimaryOrange.copy(alpha = 0.18f), CircleShape)
                .border(1.dp, BrandPrimaryOrange.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                style = Typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 12.sp),
                color = BrandPrimaryOrange
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = timeline,
                style = Typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                color = BrandPrimaryOrange
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                style = Typography.bodySmall.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
private fun OptInFlowRow(tags: List<String>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                tags.take(3).forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BrandPrimaryOrange.copy(alpha = 0.12f),
                        border = BorderStroke(0.8.dp, BrandPrimaryOrange.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = tag,
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                            color = BrandPrimaryOrange,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            if (tags.size > 3) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    tags.drop(3).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = tag,
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Medium, fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
