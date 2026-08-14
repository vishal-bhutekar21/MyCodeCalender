package com.mycodecalendar.feature.platformdetail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycodecalendar.core.designsystem.GlassmorphismBackground
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.core.designsystem.components.GlassBackButton
import com.mycodecalendar.core.designsystem.components.GlassCard
import com.mycodecalendar.core.designsystem.components.PlatformBadge
import com.mycodecalendar.core.designsystem.components.PlatformDetailSkeleton
import com.mycodecalendar.core.designsystem.components.SectionHeader
import com.mycodecalendar.core.designsystem.components.getBrandColor
import com.mycodecalendar.domain.model.GitHubRepo
import com.mycodecalendar.domain.model.GitHubStats
import com.mycodecalendar.domain.model.Platform
import com.mycodecalendar.domain.model.PlatformStats
import com.mycodecalendar.domain.model.RatingPoint

/**
 * PlatformDetailScreen — Displays platform stats, rating history or public GitHub repositories.
 */
@Composable
fun PlatformDetailScreen(
    stats: PlatformStats?,
    ratingHistory: List<RatingPoint>,
    gitHubStats: GitHubStats? = null,
    onOpenUrl: (String) -> Unit = {},
    onBackClick: () -> Unit
) {
    val brandColor = stats?.platform?.getBrandColor() ?: MaterialTheme.colorScheme.primary

    GlassmorphismBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Top Navigation Bar ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassBackButton(onClick = onBackClick)
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = stats?.platform?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Platform",
                        style = Typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (stats?.platform == Platform.GITHUB) "Repositories & Activity" else "Performance & Rating",
                        style = Typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            if (stats == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Platform account not connected.",
                        style = Typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            } else if (stats.platform == Platform.GITHUB) {
                // ── Dedicated GitHub Detail View ────────────────────────────
                GitHubDetailContent(
                    stats = stats,
                    gitHubStats = gitHubStats,
                    onOpenUrl = onOpenUrl
                )
            } else {
                // ── Competitive Programming Platform Detail View ────────────
                CpDetailContent(
                    stats = stats,
                    ratingHistory = ratingHistory,
                    brandColor = brandColor
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GITHUB DETAIL VIEW (Repositories & Contributions)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GitHubDetailContent(
    stats: PlatformStats,
    gitHubStats: GitHubStats?,
    onOpenUrl: (String) -> Unit
) {
    val gh = gitHubStats
    val brandColor = Platform.GITHUB.getBrandColor()

    // ── Hero Profile Card ───────────────────────────────────────────────────
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        accentColor = brandColor,
        cornerRadius = 24.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar circle
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(brandColor.copy(alpha = 0.25f), Color.Transparent)
                            ),
                            CircleShape
                        )
                        .border(1.5.dp, brandColor.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Code,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = brandColor
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = gh?.name ?: stats.username,
                        style = Typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "@${stats.username}",
                        style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = brandColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "GitHub Developer",
                        style = Typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(14.dp))

            // Stats row: Repos, Stars, Followers, Following
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                GitHubStatPill(
                    label = "Public Repos",
                    value = "${gh?.publicRepos ?: stats.solved ?: 0}",
                    icon = Icons.Rounded.FolderOpen
                )
                GitHubStatPill(
                    label = "Total Stars",
                    value = "${gh?.totalStars ?: 0}",
                    icon = Icons.Rounded.Star
                )
                GitHubStatPill(
                    label = "Followers",
                    value = "${gh?.followers ?: 0}",
                    icon = Icons.Rounded.People
                )
                GitHubStatPill(
                    label = "Following",
                    value = "${gh?.following ?: 0}",
                    icon = Icons.Rounded.PersonAdd
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // ── Contributions Summary ───────────────────────────────────────────────
    SectionHeader(
        title = "Contributions Activity",
        modifier = Modifier.padding(horizontal = 20.dp)
    )
    Spacer(modifier = Modifier.height(8.dp))

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        accentColor = Color(0xFF10F07B),
        cornerRadius = 18.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${gh?.totalContributionsThisYear ?: 0}",
                        style = Typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF10F07B), Color(0xFF06B6D4))
                            )
                        )
                    )
                    Text(
                        text = "Contributions this year",
                        style = Typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                gh?.currentContributionStreak?.let { streak ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Rounded.LocalFireDepartment,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFFF59E0B)
                            )
                            Text(
                                text = "$streak day streak",
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Mini Contribution Heatmap (last 20 weeks)
            val contribs = gh?.dailyContributions ?: emptyList()
            if (contribs.isNotEmpty()) {
                val lastDays = contribs.takeLast(140) // 20 weeks x 7 days
                val columns = lastDays.chunked(7)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    columns.forEach { col ->
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            col.forEach { day ->
                                val cellColor = when (day.level) {
                                    4 -> Color(0xFF00F579)
                                    3 -> Color(0xFF00C962)
                                    2 -> Color(0xFF006D35)
                                    1 -> Color(0xFF00381B)
                                    else -> Color(0xFF161B22)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(cellColor)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Less ",
                        style = Typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    listOf(
                        Color(0xFF161B22),
                        Color(0xFF00381B),
                        Color(0xFF006D35),
                        Color(0xFF00C962),
                        Color(0xFF00F579)
                    ).forEach { col ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 1.5.dp)
                                .size(8.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(col)
                        )
                    }
                    Text(
                        " More",
                        style = Typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // ── Top Languages ───────────────────────────────────────────────────────
    if (!gh?.topLanguages.isNullOrEmpty()) {
        SectionHeader(
            title = "Top Languages",
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            gh!!.topLanguages.forEach { lang ->
                val langColor = getLanguageColor(lang)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = langColor.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, langColor.copy(alpha = 0.35f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(langColor, CircleShape)
                        )
                        Text(
                            text = lang,
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }

    // ── Public Repositories List ────────────────────────────────────────────
    val repos = gh?.repos ?: emptyList()
    SectionHeader(
        title = "Public Repositories (${repos.size.coerceAtLeast(gh?.publicRepos ?: 0)})",
        modifier = Modifier.padding(horizontal = 20.dp)
    )
    Spacer(modifier = Modifier.height(8.dp))

    if (repos.isEmpty()) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            cornerRadius = 14.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No public repositories found.",
                    style = Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    } else {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            repos.forEach { repo ->
                GitHubRepoCard(repo = repo, onClick = { onOpenUrl(repo.url) })
            }
        }
    }
}

@Composable
private fun GitHubStatPill(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = Typography.titleMedium.copy(fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = Typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun GitHubRepoCard(
    repo: GitHubRepo,
    onClick: () -> Unit
) {
    val langColor = getLanguageColor(repo.language ?: "")

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Book,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = repo.name,
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "Public",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = Typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            val desc = repo.description
            if (!desc.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = desc,
                    style = Typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom row: language, stars, forks
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val lang = repo.language
                if (!lang.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(langColor, CircleShape)
                        )
                        Text(
                            text = lang,
                            style = Typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.StarOutline,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFFF59E0B)
                    )
                    Text(
                        text = "${repo.stars}",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                }

                if (repo.forks > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ForkRight,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "${repo.forks}",
                            style = Typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                    }
                }
            }
        }
    }
}

private fun getLanguageColor(lang: String): Color = when (lang.lowercase()) {
    "kotlin"     -> Color(0xFFA97BFF)
    "java"       -> Color(0xFFB07219)
    "python"     -> Color(0xFF3572A5)
    "c++", "cpp" -> Color(0xFFF34B7D)
    "c"          -> Color(0xFF555555)
    "javascript" -> Color(0xFFF1E05A)
    "typescript" -> Color(0xFF3178C6)
    "rust"       -> Color(0xFFDEA584)
    "go"         -> Color(0xFF00ADD8)
    "swift"      -> Color(0xFFF05138)
    else         -> Color(0xFF818CF8)
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPETITIVE PROGRAMMING PLATFORM DETAIL VIEW
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CpDetailContent(
    stats: PlatformStats,
    ratingHistory: List<RatingPoint>,
    brandColor: Color
) {
    // ── Hero Header Card ────────────────────────────────────────────────────
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        accentColor = brandColor,
        cornerRadius = 24.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(brandColor.copy(alpha = 0.16f), Color.Transparent)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlatformBadge(platform = stats.platform)
                    stats.rank?.let { rank ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = brandColor.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, brandColor.copy(alpha = 0.40f)
                            )
                        ) {
                            Text(
                                text = rank,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = brandColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "@${stats.username}",
                    style = Typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface
                )

                stats.rating?.let { rating ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$rating",
                        style = Typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 38.sp,
                            brush = Brush.horizontalGradient(
                                listOf(brandColor, brandColor.copy(alpha = 0.75f))
                            )
                        )
                    )
                    Text(
                        text = "Current Rating",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // ── Stats Grid (2x2) ────────────────────────────────────────────────────
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            icon = Icons.Rounded.BarChart,
            title = "Highest Rating",
            value = stats.highestRating?.toString() ?: "—",
            accentColor = brandColor,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            icon = Icons.Rounded.Language,
            title = "Global Rank",
            value = stats.globalRank?.let { "#$it" } ?: "—",
            accentColor = Color(0xFF06B6D4),
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            icon = Icons.Rounded.EmojiEvents,
            title = "Contests",
            value = "${stats.contestCount ?: ratingHistory.size}",
            accentColor = Color(0xFFF59E0B),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            icon = Icons.Rounded.CheckCircle,
            title = "Problems Solved",
            value = stats.solved?.toString() ?: "—",
            accentColor = Color(0xFF10B981),
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // ── Rating Progression Chart with Visible Numbers ───────────────────────
    SectionHeader(
        title = "Rating Progression",
        modifier = Modifier.padding(horizontal = 20.dp)
    )
    Spacer(modifier = Modifier.height(10.dp))

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        accentColor = brandColor,
        cornerRadius = 20.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (ratingHistory.isEmpty()) {
                Text(
                    text = "No rating history available for this platform.",
                    style = Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            } else {
                val ratings = ratingHistory.map { it.rating }
                val minR = ratings.minOrNull() ?: 0
                val maxR = ratings.maxOrNull() ?: 0
                val currR = ratings.lastOrNull() ?: 0

                // Quick stats summary row above chart
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Min: $minR",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = brandColor.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, brandColor.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "Now: $currR",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = brandColor
                        )
                    }
                    Text(
                        text = "Peak: $maxR",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                RatingLineChart(
                    points = ratingHistory,
                    lineColor = brandColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // ── Solved Problem Difficulty Breakdown ─────────────────────────────────
    if (stats.easySolved != null || stats.mediumSolved != null || stats.hardSolved != null) {
        SectionHeader(
            title = "Problem Solving Breakdown",
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            cornerRadius = 18.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Problems Solved",
                        style = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${stats.solved ?: 0}",
                        style = Typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stacked Segmented Bar
                val easy = (stats.easySolved ?: 0).toFloat()
                val medium = (stats.mediumSolved ?: 0).toFloat()
                val hard = (stats.hardSolved ?: 0).toFloat()
                val total = (easy + medium + hard).coerceAtLeast(1f)

                val easyPct = ((easy / total) * 100).toInt()
                val medPct = ((medium / total) * 100).toInt()
                val hardPct = ((hard / total) * 100).toInt()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        )
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        if (easy > 0) Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(easy / total)
                                .background(
                                    color = Color(0xFF10B981),
                                    shape = RoundedCornerShape(
                                        topStart = 6.dp, bottomStart = 6.dp,
                                        topEnd = if (medium == 0f && hard == 0f) 6.dp else 0.dp,
                                        bottomEnd = if (medium == 0f && hard == 0f) 6.dp else 0.dp
                                    )
                                )
                        )
                        if (medium > 0) Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(medium / total)
                                .background(color = Color(0xFFF59E0B))
                        )
                        if (hard > 0) Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(hard / total)
                                .background(
                                    color = Color(0xFFF43F5E),
                                    shape = RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Difficulty Detail Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DifficultyCard(
                        label = "Easy",
                        count = stats.easySolved ?: 0,
                        percentage = easyPct,
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    DifficultyCard(
                        label = "Medium",
                        count = stats.mediumSolved ?: 0,
                        percentage = medPct,
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )
                    DifficultyCard(
                        label = "Hard",
                        count = stats.hardSolved ?: 0,
                        percentage = hardPct,
                        color = Color(0xFFF43F5E),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// REUSABLE COMPONENTS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StatCard(
    icon: ImageVector,
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        accentColor = accentColor,
        cornerRadius = 16.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = accentColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = Typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = Typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DifficultyCard(
    label: String,
    count: Int,
    percentage: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(alpha = 0.30f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count.toString(),
                style = Typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$percentage%",
                style = Typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun RatingLineChart(
    points: List<RatingPoint>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    val fillColor = lineColor

    Canvas(modifier = modifier) {
        if (points.isEmpty()) return@Canvas

        val ratings = points.map { it.rating }
        val minRating = (ratings.minOrNull() ?: 1000) - 80
        val maxRating = (ratings.maxOrNull() ?: 2000) + 80
        val ratingRange = (maxRating - minRating).coerceAtLeast(1)

        val w = size.width
        val h = size.height
        val spacing = w / (points.size - 1).coerceAtLeast(1)

        val coords = points.mapIndexed { index, point ->
            val x = index * spacing
            val y = h - ((point.rating - minRating).toFloat() / ratingRange * (h - 30.dp.toPx())) - 15.dp.toPx()
            Offset(x, y)
        }

        // Draw horizontal guide lines
        val midY = h / 2f
        drawLine(
            color = lineColor.copy(alpha = 0.20f),
            start = Offset(0f, midY),
            end = Offset(w, midY),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )

        val strokePath = Path().apply {
            moveTo(coords.first().x, coords.first().y)
            for (i in 0 until coords.size - 1) {
                val p1 = coords[i]
                val p2 = coords[i + 1]
                val controlPoint1 = Offset(p1.x + (p2.x - p1.x) / 2f, p1.y)
                val controlPoint2 = Offset(p1.x + (p2.x - p1.x) / 2f, p2.y)
                cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p2.x, p2.y)
            }
        }

        val fillPath = Path().apply {
            addPath(strokePath)
            lineTo(coords.last().x, h)
            lineTo(coords.first().x, h)
            close()
        }

        // Gradient under the curve
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(fillColor.copy(alpha = 0.35f), Color.Transparent),
                startY = 0f,
                endY = h
            )
        )

        // Main Bézier stroke
        drawPath(
            path = strokePath,
            color = lineColor,
            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Rating Points
        coords.forEachIndexed { idx, offset ->
            val isLast = idx == coords.lastIndex
            if (isLast) {
                // Large glowing halo on latest rating
                drawCircle(
                    color = lineColor.copy(alpha = 0.30f),
                    radius = 12.dp.toPx(),
                    center = offset
                )
                drawCircle(
                    color = lineColor,
                    radius = 6.dp.toPx(),
                    center = offset
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = offset
                )
            } else {
                drawCircle(
                    color = lineColor.copy(alpha = 0.35f),
                    radius = 5.dp.toPx(),
                    center = offset
                )
                drawCircle(
                    color = lineColor,
                    radius = 3.dp.toPx(),
                    center = offset
                )
            }
        }
    }
}
