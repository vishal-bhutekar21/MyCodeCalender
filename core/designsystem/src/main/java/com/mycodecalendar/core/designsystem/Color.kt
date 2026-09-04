package com.mycodecalendar.core.designsystem

import androidx.compose.ui.graphics.Color

// ── SIGNATURE MYCODECALENDAR BRAND PALETTE ────────────────────────────────────
// Refined Cyber Obsidian Palette: Electric Orange + Luminous Indigo & Vivid Crimson

val BrandPrimaryOrange       = Color(0xFFFF6F00)  // Signature Electric Vivid Amber-Orange (#FF6F00)
val BrandOrangeAccent        = Color(0xFFFF8800)  // High-luminance Amber-Orange
val BrandOrangeDeep          = Color(0xFFFF5722)  // Deep Coral-Orange
val BrandPurpleAccent        = Color(0xFF6366F1)  // Electric Indigo (#6366F1)
val BrandLavenderAccent      = Color(0xFF818CF8)  // Modern Slate Lavender (#818CF8)
val BrandIndigoAccent        = Color(0xFF4F46E5)  // Deep Indigo

// ── PLATFORM BRAND COLORS ─────────────────────────────────────────────────────
val BrandCodeforces    = Color(0xFF3B82F6)   // Electric Blue
val BrandLeetCode      = Color(0xFFFFA116)   // LeetCode signature Amber Gold
val BrandCodeChef      = Color(0xFF8B5CF6)   // CodeChef Violet
val BrandAtCoder       = Color(0xFF38BDF8)   // AtCoder Cool Sky Cyan
val BrandGitHub        = Color(0xFF10B981)   // GitHub Vivid Emerald
val BrandGeeksforGeeks = Color(0xFF22C55E)   // GFG Vivid Green

// ── GLASSMORPHISM COLOR TOKENS ────────────────────────────────────────────────
/** Frosted glass card fill — 12% white overlay on dark surfaces */
val GlassSurfaceDark   = Color(0x1AFFFFFF)

/** Frosted glass card fill — 70% white overlay on light surfaces */
val GlassSurfaceLight  = Color(0xB3FFFFFF)

/** Frosted glass card border — 20% white for dark theme */
val GlassBorderDark    = Color(0x30FFFFFF)

/** Frosted glass card border — 60% white for light theme */
val GlassBorderLight   = Color(0x99FFFFFF)

/** Top-edge inner highlight */
val GlassHighlightDark = Color(0x14FFFFFF)
val GlassHighlightLight= Color(0x26FFFFFF)

// ── GRADIENT MESH OVERLAY COLORS ──────────────────────────────────────────────
val MeshOrangeDark  = Color(0x20FF6F00)
val MeshVioletDark  = Color(0x206366F1)
val MeshBlueDark    = Color(0x183B82F6)
val MeshGreenDark   = Color(0x1410B981)
val MeshAmberDark   = Color(0x1CF59E0B)

val MeshOrangeLight = Color(0x10FF6F00)
val MeshVioletLight = Color(0x106366F1)
val MeshBlueLight   = Color(0x103B82F6)
val MeshGreenLight  = Color(0x10059669)

// ── HIGH-VISIBILITY LIVE & STATUS COLOR TOKENS ────────────────────────────────
val LiveRuby         = Color(0xFFFF1744)  // Ultra-vivid Electric Crimson (#FF1744)
val LiveRubyDarkBg   = Color(0xFF260810)  // Deep ruby obsidian glass fill
val LiveRubyBorder   = Color(0xFFFF1744).copy(alpha = 0.55f)
val GlowLiveRuby     = Color(0x66FF1744)  // Radiant ruby crimson glow

val CyberCyan        = Color(0xFF06B6D4)  // Electric Cyber Cyan
val CyberCyanBg      = Color(0xFF08222E)
val CyberCyanBorder  = Color(0xFF06B6D4).copy(alpha = 0.45f)

// ── GLOW COLORS ───────────────────────────────────────────────────────────────
val GlowPrimary    = Color(0x59FF6F00)
val GlowLive       = Color(0x66FF1744)
val GlowStreak     = Color(0x59FF6F00)
val GlowError      = Color(0x40DC2626)

// ── COUNTDOWN TIMER COLORS ────────────────────────────────────────────────────
val CountdownUrgent = Color(0xFFFF1744)
val CountdownNormal = Color(0xFFFF6F00)

// ── DIFFICULTY COLORS ─────────────────────────────────────────────────────────
val DifficultyEasy   = Color(0xFF06B6D4)
val DifficultyMedium = Color(0xFFFF9800)
val DifficultyHard   = Color(0xFFF43F5E)

// ── TEXT COLOR TOKENS ────────────────────────────────────────────────────────
val TextPrimaryDark    = Color(0xFFFFFFFF)  // pure snow white
val TextSecondaryDark  = Color(0xFFCBD5E1)  // luminous slate-300
val TextMutedDark      = Color(0xFF8896AB)  // cool slate-400
val TextAccentOrange   = Color(0xFFFF6F00)  // signature brand orange

val TextPrimaryLight   = Color(0xFF0F172A)  // deep charcoal slate
val TextSecondaryLight = Color(0xFF334155)  // slate-700
val TextMutedLight     = Color(0xFF64748B)  // slate-500

