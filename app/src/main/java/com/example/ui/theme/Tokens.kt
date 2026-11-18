package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Revision Space Design Tokens (v2)
 * ---------------------------------------------------------------
 * Single source of truth for the visual language of the app.
 * Screens should prefer these tokens over raw hex values so that
 * the whole product stays consistent when the brand evolves.
 */
object AppPalette {
    // Brand
    val Brand = Color(0xFF6C4DFF)
    val BrandDeep = Color(0xFF4B32D6)
    val BrandLight = Color(0xFF9B8CFF)
    val BrandSoft = Color(0xFFF1EEFF)
    val BrandSofter = Color(0xFFF8F6FF)

    // Neutrals (light theme)
    val Ink = Color(0xFF0F172A)
    val InkSecondary = Color(0xFF475569)
    val InkMuted = Color(0xFF8A94A6)
    val Background = Color(0xFFF6F7FB)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceAlt = Color(0xFFF1F3F9)
    val Border = Color(0xFFE7EAF3)

    // Semantic
    val Success = Color(0xFF10B981)
    val SuccessSoft = Color(0xFFE7F8F1)
    val Warning = Color(0xFFF59E0B)
    val WarningSoft = Color(0xFFFFF6E5)
    val Danger = Color(0xFFEF4444)
    val DangerSoft = Color(0xFFFEECEC)
    val Info = Color(0xFF3B82F6)
    val InfoSoft = Color(0xFFEAF2FF)

    // Dark theme neutrals
    val DarkBackground = Color(0xFF0B0E14)
    val DarkSurface = Color(0xFF141824)
    val DarkSurfaceAlt = Color(0xFF1B2130)
    val DarkBorder = Color(0xFF283040)
    val DarkInk = Color(0xFFEDF0F7)
    val DarkInkSecondary = Color(0xFFA8B0C0)

    // Gradients
    val BrandGradient = Brush.linearGradient(listOf(Color(0xFF6C4DFF), Color(0xFF9B8CFF)))
    val SunriseGradient = Brush.linearGradient(listOf(Color(0xFF6C4DFF), Color(0xFF4B32D6)))
    val MintGradient = Brush.linearGradient(listOf(Color(0xFF12C2A0), Color(0xFF0E9F8A)))
    val WarmGradient = Brush.linearGradient(listOf(Color(0xFFFFB020), Color(0xFFFF7A45)))
}

/** Spacing scale (4dp base grid). */
object AppSpacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
}

/** Corner radii used across cards, sheets and dialogs. */
object AppRadius {
    val sm = 12.dp
    val md = 16.dp
    val lg = 20.dp
    val xl = 24.dp
    val xxl = 28.dp
    val pill = 999.dp
}
