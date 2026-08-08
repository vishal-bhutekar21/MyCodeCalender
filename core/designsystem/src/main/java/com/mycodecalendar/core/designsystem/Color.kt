package com.mycodecalendar.core.designsystem

import androidx.compose.ui.graphics.Color

// ── PLATFORM BRAND COLORS ─────────────────────────────────────────────────────
// Used in PlatformBadge dots, card accents, chart lines.

val BrandCodeforces    = Color(0xFF3B82F6)   // blue-500
val BrandLeetCode      = Color(0xFFF59E0B)   // amber-500
val BrandCodeChef      = Color(0xFF8B5CF6)   // violet-500
val BrandAtCoder       = Color(0xFF64748B)   // slate-500
val BrandGitHub        = Color(0xFF22C55E)   // green-500
val BrandGeeksforGeeks = Color(0xFF16A34A)   // green-600

// ── GLASSMORPHISM COLOR TOKENS ────────────────────────────────────────────────
// Used by GlassCard and glassmorphism background composables.
// All colors use ARGB format: 0xAARRGGBB

/** Frosted glass card fill — 10% white overlay on dark surfaces */
val GlassSurfaceDark   = Color(0x1AFFFFFF)

/** Frosted glass card fill — 55% white overlay on light surfaces */
val GlassSurfaceLight  = Color(0x8CFFFFFF)

/** Frosted glass card border — 20% white for dark theme */
val GlassBorderDark    = Color(0x33FFFFFF)

/** Frosted glass card border — 60% white for light theme */
val GlassBorderLight   = Color(0x99FFFFFF)

/** Top-edge inner highlight — creates the "beveled glass" depth illusion */
val GlassHighlightDark = Color(0x0DFFFFFF)  // 5% white
val GlassHighlightLight= Color(0x1AFFFFFF)  // 10% white

// ── GRADIENT MESH OVERLAY COLORS ──────────────────────────────────────────────
// Used in animated background mesh (Canvas-drawn radial gradients).

/** Violet mesh node — primary accent glow for dark mode */
val MeshVioletDark  = Color(0x337C3AED)  // violet-600 @ 20%
val MeshBlueDark    = Color(0x263B82F6)  // blue-500   @ 15%
val MeshGreenDark   = Color(0x1A10B981)  // emerald-500 @ 10%
val MeshAmberDark   = Color(0x1AF59E0B)  // amber-500  @ 10%

/** Light mode mesh nodes — softer, more pastel */
val MeshVioletLight = Color(0x1A7C3AED)  // violet-600 @ 10%
val MeshBlueLight   = Color(0x1A3B82F6)  // blue-500   @ 10%
val MeshGreenLight  = Color(0x1A059669)  // emerald-600 @ 10%

// ── GLOW COLORS ───────────────────────────────────────────────────────────────
// Used for card border glow effects on interactive elements.

/** Primary indigo glow — for selected states and primary CTAs */
val GlowPrimary    = Color(0x404F46E5)   // indigo-600 @ 25%

/** Emerald glow — for LIVE contest badges and streak indicators */
val GlowLive       = Color(0x4010B981)   // emerald-500 @ 25%

/** Amber glow — for streak fire icons */
val GlowStreak     = Color(0x40F59E0B)   // amber-500 @ 25%

/** Error/danger glow — for network error states */
val GlowError      = Color(0x40DC2626)   // red-600 @ 25%

// ── COUNTDOWN TIMER COLORS ────────────────────────────────────────────────────
/** Pulsing color when countdown is < 1 hour */
val CountdownUrgent = Color(0xFFF43F5E)  // rose-500
val CountdownNormal = Color(0xFF818CF8)  // indigo-400

// ── DIFFICULTY COLORS ─────────────────────────────────────────────────────────
val DifficultyEasy   = Color(0xFF10B981) // emerald-500
val DifficultyMedium = Color(0xFFF59E0B) // amber-500
val DifficultyHard   = Color(0xFFF43F5E) // rose-500

// ── TEXT COLOR TOKENS ────────────────────────────────────────────────────────
val TextPrimaryDark    = Color(0xFFFFFFFF)  // pure snow white
val TextSecondaryDark  = Color(0xFFCBD5E1)  // luminous slate-300
val TextMutedDark      = Color(0xFF94A3B8)  // slate-400

val TextPrimaryLight   = Color(0xFF0F172A)  // deep charcoal slate
val TextSecondaryLight = Color(0xFF334155)  // slate-700
val TextMutedLight     = Color(0xFF64748B)  // slate-500

