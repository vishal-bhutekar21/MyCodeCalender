package com.mycodecalendar.domain.model

import java.time.LocalDate
import java.time.YearMonth

enum class BadgeCategory {
    MILESTONE,
    MONTHLY
}

/**
 * StreakBadge — Represents an unlockable 3D trophy / badge (similar to LeetCode monthly badges & milestone medals).
 */
data class StreakBadge(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val category: BadgeCategory,
    val targetDays: Int,
    val targetMonth: Int? = null, // 1 for Jan, 2 for Feb, ... 12 for Dec
    val tierTitle: String,        // "Bronze", "Silver", "Gold", "Diamond", "Grandmaster"
    val isUnlocked: Boolean,
    val currentProgress: Int,
    val maxProgress: Int,
    val unlockedDateText: String? = null,
    val colorHexes: List<Long>   // Gradient hex values for 3D glow styling
)

object BadgeHelper {

    /**
     * Returns the coder rank title based on current streak count.
     */
    fun getCoderRank(streakDays: Int): String = when {
        streakDays >= 100 -> "Level 5: Grandmaster Streak"
        streakDays >= 50  -> "Level 4: Code Ninja"
        streakDays >= 30  -> "Level 3: Consistency Champion"
        streakDays >= 14  -> "Level 2: Algo Artisan"
        streakDays >= 7   -> "Level 1: Code Initiate"
        else              -> "Streak Apprentice"
    }

    /**
     * Computes all 17 badges (5 Milestones + 12 Monthly Badges) dynamically from [StreakInfo].
     */
    fun computeAllBadges(streakInfo: StreakInfo): List<StreakBadge> {
        val badges = mutableListOf<StreakBadge>()
        val currentStreak = streakInfo.currentStreak
        val activeDates = streakInfo.activeDates.mapNotNull {
            runCatching { LocalDate.parse(it) }.getOrNull()
        }.toSet()

        val currentYear = LocalDate.now().year

        // ── 5 STREAK MILESTONES ──────────────────────────────────────────────────
        // 1. 7-Day Sprint ("Code Initiate") - Bronze Flame
        val is7dUnlocked = currentStreak >= 7 || activeDates.size >= 7
        badges.add(
            StreakBadge(
                id = "badge_7d",
                title = "7-Day Sprint",
                subtitle = "Code Initiate",
                description = "Maintain an active 7-day coding streak. The first step toward legendary consistency.",
                category = BadgeCategory.MILESTONE,
                targetDays = 7,
                tierTitle = "Bronze Tier",
                isUnlocked = is7dUnlocked,
                currentProgress = currentStreak.coerceAtMost(7),
                maxProgress = 7,
                unlockedDateText = if (is7dUnlocked) "Earned" else null,
                colorHexes = listOf(0xFFCD7F32, 0xFFFF8C00)
            )
        )

        // 2. 14-Day Velocity ("Algo Artisan") - Silver Amethyst
        val is14dUnlocked = currentStreak >= 14 || activeDates.size >= 14
        badges.add(
            StreakBadge(
                id = "badge_14d",
                title = "14-Day Velocity",
                subtitle = "Algo Artisan",
                description = "Complete 2 consecutive weeks of active coding and contest tracking.",
                category = BadgeCategory.MILESTONE,
                targetDays = 14,
                tierTitle = "Silver Tier",
                isUnlocked = is14dUnlocked,
                currentProgress = currentStreak.coerceAtMost(14),
                maxProgress = 14,
                unlockedDateText = if (is14dUnlocked) "Earned" else null,
                colorHexes = listOf(0xFF818CF8, 0xFFA78BFA)
            )
        )

        // 3. 30-Day Master ("Consistency Champion") - Radiant Gold
        val is30dUnlocked = currentStreak >= 30 || activeDates.size >= 30
        badges.add(
            StreakBadge(
                id = "badge_30d",
                title = "30-Day Master",
                subtitle = "Consistency Champion",
                description = "A full month of non-stop daily coding momentum. You're among the top 5% of dedicated devs.",
                category = BadgeCategory.MILESTONE,
                targetDays = 30,
                tierTitle = "Gold Tier",
                isUnlocked = is30dUnlocked,
                currentProgress = currentStreak.coerceAtMost(30),
                maxProgress = 30,
                unlockedDateText = if (is30dUnlocked) "Earned" else null,
                colorHexes = listOf(0xFFFFB800, 0xFFFF6B00)
            )
        )

        // 4. 50-Day Elite ("Code Ninja") - Emerald Cyber
        val is50dUnlocked = currentStreak >= 50 || activeDates.size >= 50
        badges.add(
            StreakBadge(
                id = "badge_50d",
                title = "50-Day Elite",
                subtitle = "Code Ninja",
                description = "50 days of unyielding focus. Algorithmic problem-solving is now your second nature.",
                category = BadgeCategory.MILESTONE,
                targetDays = 50,
                tierTitle = "Diamond Tier",
                isUnlocked = is50dUnlocked,
                currentProgress = currentStreak.coerceAtMost(50),
                maxProgress = 50,
                unlockedDateText = if (is50dUnlocked) "Earned" else null,
                colorHexes = listOf(0xFF00F579, 0xFF06B6D4)
            )
        )

        // 5. 100-Day Centurion ("Grandmaster Streak") - Obsidian Ruby & Diamond
        val is100dUnlocked = currentStreak >= 100 || activeDates.size >= 100
        badges.add(
            StreakBadge(
                id = "badge_100d",
                title = "100-Day Centurion",
                subtitle = "Grandmaster Streak",
                description = "The ultimate test of coder discipline. Unlocked after 100 days of pure dedication.",
                category = BadgeCategory.MILESTONE,
                targetDays = 100,
                tierTitle = "Grandmaster Tier",
                isUnlocked = is100dUnlocked,
                currentProgress = currentStreak.coerceAtMost(100),
                maxProgress = 100,
                unlockedDateText = if (is100dUnlocked) "Earned" else null,
                colorHexes = listOf(0xFFE11D48, 0xFFFF007A)
            )
        )

        // ── 12 MONTHLY BADGES (Jan – Dec) ────────────────────────────────────────
        val monthNames = listOf(
            "January" to "Frost Code",
            "February" to "Ignite Algo",
            "March" to "Spring Sprint",
            "April" to "Cyber Bloom",
            "May" to "Matrix Matrix",
            "June" to "Solar Solver",
            "July" to "Mid-Year Titan",
            "August" to "Binary Blaze",
            "September" to "Quantum Wave",
            "October" to "Spooky Logic",
            "November" to "Neural Pulse",
            "December" to "Winter Grandmaster"
        )

        val monthColors = listOf(
            listOf(0xFF38BDF8, 0xFF0284C7), // Jan - Cyan
            listOf(0xFFFF5252, 0xFFFF7A00), // Feb - Fire
            listOf(0xFF10B981, 0xFF059669), // Mar - Emerald
            listOf(0xFFA855F7, 0xFF7C3AED), // Apr - Purple
            listOf(0xFFEC4899, 0xFFDB2777), // May - Pink
            listOf(0xFFF59E0B, 0xFFD97706), // Jun - Amber
            listOf(0xFF6366F1, 0xFF4F46E5), // Jul - Indigo
            listOf(0xFFFF6B00, 0xFFFF8F00), // Aug - Orange
            listOf(0xFF14B8A6, 0xFF0D9488), // Sep - Teal
            listOf(0xFFF97316, 0xFFEA580C), // Oct - Orange Flame
            listOf(0xFF8B5CF6, 0xFF6D28D9), // Nov - Violet
            listOf(0xFF06B6D4, 0xFF3B82F6)  // Dec - Ice Blue
        )

        val nowMonth = LocalDate.now().monthValue

        for (m in 1..12) {
            val (mName, mSubtitle) = monthNames[m - 1]
            val ym = YearMonth.of(currentYear, m)
            val monthDaysCount = activeDates.count { it.year == currentYear && it.monthValue == m }
            val requiredDays = 5.coerceAtMost(ym.lengthOfMonth()) // 5 active days in the month to unlock badge

            val isMonthUnlocked = monthDaysCount >= requiredDays || (m <= nowMonth && currentStreak >= 7 && monthDaysCount > 0)

            badges.add(
                StreakBadge(
                    id = "badge_month_$m",
                    title = "$mName $currentYear",
                    subtitle = mSubtitle,
                    description = "Participate in contests and solve problems in $mName to earn the official $mName badge.",
                    category = BadgeCategory.MONTHLY,
                    targetDays = requiredDays,
                    targetMonth = m,
                    tierTitle = "$mName Badge",
                    isUnlocked = isMonthUnlocked,
                    currentProgress = monthDaysCount.coerceAtMost(requiredDays),
                    maxProgress = requiredDays,
                    unlockedDateText = if (isMonthUnlocked) "$mName $currentYear" else null,
                    colorHexes = monthColors[m - 1]
                )
            )
        }

        return badges
    }
}
