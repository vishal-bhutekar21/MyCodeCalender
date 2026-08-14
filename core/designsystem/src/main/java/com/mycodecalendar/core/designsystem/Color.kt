package com.mycodecalendar.core.designsystem

import androidx.compose.ui.graphics.Color

// ── SIGNATURE MYCODECALENDAR BRAND PALETTE ────────────────────────────────────
// Extracted from official reference design: Vibrant Orange + Lavender & Obsidian Dark

val BrandPrimaryOrange       = Color(0xFFFF6B00)  // Electric Vivid Orange (#FF6B00)
val BrandOrangeAccent        = Color(0xFFFF8A00)  // Warm Amber-Orange (#FF8A00)
val BrandOrangeDeep          = Color(0xFFFF5722)  // Deep Coral-Orange (#FF5722)
val BrandPurpleAccent        = Color(0xFF6C5CE7)  // Slate Lavender (#6C5CE7)
val BrandLavenderAccent      = Color(0xFF7C4DFF)  // Vivid Purple (#7C4DFF)
val BrandIndigoAccent        = Color(0xFF6366F1)  // Electric Indigo (#6366F1)

// ── PLATFORM BRAND COLORS ─────────────────────────────────────────────────────
// Used in PlatformBadge dots, card accents, chart lines.

val BrandCodeforces    = Color(0xFF3B82F6)   // blue-500
val BrandLeetCode      = Color(0xFFFFA116)   // leetcode signature orange/amber
val BrandCodeChef      = Color(0xFF8B5CF6)   // violet-500
val BrandAtCoder       = Color(0xFF64748B)   // slate-500
val BrandGitHub        = Color(0xFF22C55E)   // green-500
val BrandGeeksforGeeks = Color(0xFF16A34A)   // green-600

// ── GLASSMORPHISM COLOR TOKENS ────────────────────────────────────────────────
// Used by GlassCard and glassmorphism background composables.
// All colors use ARGB format: 0xAARRGGBB

/** Frosted glass card fill — 12% white overlay on dark surfaces */
val GlassSurfaceDark   = Color(0x1FFFFFFF)

/** Frosted glass card fill — 65% white overlay on light surfaces */
val GlassSurfaceLight  = Color(0xA6FFFFFF)

/** Frosted glass card border — 22% white for dark theme */
val GlassBorderDark    = Color(0x38FFFFFF)

/** Frosted glass card border — 60% white for light theme */
val GlassBorderLight   = Color(0x99FFFFFF)

/** Top-edge inner highlight — creates the "beveled glass" depth illusion */
val GlassHighlightDark = Color(0x14FFFFFF)  // 8% white
val GlassHighlightLight= Color(0x26FFFFFF)  // 15% white

// ── GRADIENT MESH OVERLAY COLORS ──────────────────────────────────────────────
// Used in animated background mesh (Canvas-drawn radial gradients).

/** Orange & Violet mesh nodes — primary ambient glow for dark mode */
val MeshOrangeDark  = Color(0x26FF6B00)  // orange-500 @ 15%
val MeshVioletDark  = Color(0x266C5CE7)  // violet-500 @ 15%
val MeshBlueDark    = Color(0x1A3B82F6)  // blue-500   @ 10%
val MeshGreenDark   = Color(0x1A10B981)  // emerald-500 @ 10%
val MeshAmberDark   = Color(0x22F59E0B)  // amber-500  @ 13%

/** Light mode mesh nodes — softer, pastel */
val MeshOrangeLight = Color(0x14FF6B00)  // orange-500 @ 8%
val MeshVioletLight = Color(0x146C5CE7)  // violet-500 @ 8%
val MeshBlueLight   = Color(0x143B82F6)  // blue-500   @ 8%
val MeshGreenLight  = Color(0x14059669)  // emerald-600 @ 8%

// ── GLOW COLORS ───────────────────────────────────────────────────────────────
// Used for card border glow effects on interactive elements.

/** Primary orange glow — for selected states, FAB, and primary CTAs */
val GlowPrimary    = Color(0x59FF6B00)   // orange-500 @ 35%

/** Emerald glow — for LIVE contest badges and streak indicators */
val GlowLive       = Color(0x4010B981)   // emerald-500 @ 25%

/** Amber glow — for streak fire icons */
val GlowStreak     = Color(0x59FF6B00)   // vivid orange @ 35%

/** Error/danger glow — for network error states */
val GlowError      = Color(0x40DC2626)   // red-600 @ 25%

// ── COUNTDOWN TIMER COLORS ────────────────────────────────────────────────────
/** Pulsing color when countdown is < 1 hour */
val CountdownUrgent = Color(0xFFFF5252)  // red-accent
val CountdownNormal = Color(0xFFFF6B00)  // signature orange

// ── DIFFICULTY COLORS ─────────────────────────────────────────────────────────
val DifficultyEasy   = Color(0xFF10B981) // emerald-500
val DifficultyMedium = Color(0xFFFF9800) // orange-500
val DifficultyHard   = Color(0xFFF43F5E) // rose-500

// ── TEXT COLOR TOKENS ────────────────────────────────────────────────────────
val TextPrimaryDark    = Color(0xFFFFFFFF)  // pure snow white
val TextSecondaryDark  = Color(0xFFCBD5E1)  // luminous slate-300
val TextMutedDark      = Color(0xFF94A3B8)  // slate-400
val TextAccentOrange   = Color(0xFFFF6B00)  // signature brand orange

val TextPrimaryLight   = Color(0xFF0F172A)  // deep charcoal slate
val TextSecondaryLight = Color(0xFF334155)  // slate-700
val TextMutedLight     = Color(0xFF64748B)  // slate-500

