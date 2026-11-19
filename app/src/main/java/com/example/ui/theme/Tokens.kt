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
    val Brand = Color(0xFF7C3AED)
    val BrandDeep = Color(0xFF6D28D9)
    val BrandLight = Color(0xFFA855F7)
    val BrandSoft = Color(0xFFEDE9FE)
    val BrandSofter = Color(0xFFF5F3FF)

    // Neutrals (light theme)
    val Ink = Color(0xFF1B1830)
    val InkSecondary = Color(0xFF6B6580)
    val InkMuted = Color(0xFF8A83A6)
    val Background = Color(0xFFF7F5FF)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceAlt = Color(0xFFF1EDFF)
    val Border = Color(0xFFE4DCFF)

    // Semantic
    val Success = Color(0xFF22C55E)
    val SuccessSoft = Color(0xFFDCFCE7)
    val Warning = Color(0xFFF59E0B)
    val WarningSoft = Color(0xFFFEF3C7)
    val Danger = Color(0xFFEF4444)
    val DangerSoft = Color(0xFFFEE2E2)
    val Info = Color(0xFF6366F1)
    val InfoSoft = Color(0xFFE0E7FF)

    // Dark theme neutrals
    val DarkBackground = Color(0xFF0B0B1A)
    val DarkSurface = Color(0xFF151329)
    val DarkSurfaceAlt = Color(0xFF191532)
    val DarkBorder = Color(0xFF302653)
    val DarkInk = Color(0xFFFFFFFF)
    val DarkInkSecondary = Color(0xFFA7A2BD)

    // Gradients
    val BrandGradient = Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFFA855F7)))
    val SunriseGradient = Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFF6D28D9)))
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
    val button = 14.dp
    val md = 16.dp
    val card = 18.dp
    val lg = 20.dp
    val xl = 24.dp
    val xxl = 28.dp
    val pill = 999.dp
}
