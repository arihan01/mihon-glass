package eu.kanade.presentation.theme.colorscheme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Brand: orange + paper. Brand hues only appear as trim (text, icons, indicators, badges); every
 * container is a neutral fill so content stays the focus, as Apple's HIG asks of tint color.
 *
 * Every text role is at least 4.5:1 against every surface it can sit on and outlines are at least
 * 3:1 (WCAG 2.2 AA), which is why the light-theme orange is a deeper alternate of the brand's light
 * orange. Verified by TachiyomiColorSchemeContrastTest.
 */
internal object TachiyomiColorScheme {

    val lightScheme = lightColorScheme(
        primary = Color(0xFF9A4708), // Brand orange: interactive trim (links, selection, progress)
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFF0EBE1),
        onPrimaryContainer = Color(0xFF9A4708),
        inversePrimary = Color(0xFFFFB26B),
        secondary = Color(0xFF6B5A43), // Brand paper/ink: highlight trim (unread badge)
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE8E2D6),
        onSecondaryContainer = Color(0xFF9A4708),
        tertiary = Color(0xFF1B6E30), // Downloaded badge
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFF0EBE1),
        onTertiaryContainer = Color(0xFF1B6E30),
        background = Color(0xFFFBF8F2), // Paper
        onBackground = Color(0xFF1B1814),
        surface = Color(0xFFFBF8F2),
        onSurface = Color(0xFF1B1814),
        surfaceVariant = Color(0xFFE8E2D6),
        onSurfaceVariant = Color(0xFF5C554C), // Secondary label
        surfaceTint = Color(0xFF8A8174), // Neutral so elevation never tints surfaces with brand color
        inverseSurface = Color(0xFF2A2723),
        inverseOnSurface = Color(0xFFF5F0E6),
        error = Color(0xFFC4000F),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFBE0DC),
        onErrorContainer = Color(0xFF5C0008),
        outline = Color(0xFF7F776B),
        outlineVariant = Color(0xFFD6CFC2), // Separator
        scrim = Color(0xFF000000),
        surfaceBright = Color(0xFFFFFDF8),
        surfaceDim = Color(0xFFE8E2D6),
        surfaceContainerLowest = Color(0xFFFFFDF8),
        surfaceContainerLow = Color(0xFFF5F1E8),
        surfaceContainer = Color(0xFFF3EEE4),
        surfaceContainerHigh = Color(0xFFF0EBE1),
        surfaceContainerHighest = Color(0xFFE8E2D6),
    )

    val darkScheme = darkColorScheme(
        primary = Color(0xFFFFB26B),
        onPrimary = Color(0xFF3A1C00),
        primaryContainer = Color(0xFF2C2C2E),
        onPrimaryContainer = Color(0xFFFFB26B),
        inversePrimary = Color(0xFF9A4708),
        secondary = Color(0xFFEFE6D2),
        onSecondary = Color(0xFF2A2318),
        secondaryContainer = Color(0xFF3A3A3C),
        onSecondaryContainer = Color(0xFFFFB26B),
        tertiary = Color(0xFF30D158),
        onTertiary = Color(0xFF002E10),
        tertiaryContainer = Color(0xFF2C2C2E),
        onTertiaryContainer = Color(0xFF30D158),
        background = Color(0xFF0B0B0C),
        onBackground = Color(0xFFF5F0E6),
        surface = Color(0xFF0B0B0C),
        onSurface = Color(0xFFF5F0E6),
        surfaceVariant = Color(0xFF2C2C2E),
        onSurfaceVariant = Color(0xFFAEAEB2),
        surfaceTint = Color(0xFF8E8E93),
        inverseSurface = Color(0xFFF5F0E6),
        inverseOnSurface = Color(0xFF1C1C1E),
        error = Color(0xFFFF7B73),
        onError = Color(0xFF3B0000),
        errorContainer = Color(0xFF5C1210),
        onErrorContainer = Color(0xFFFFDAD6),
        outline = Color(0xFF8E8E93),
        outlineVariant = Color(0xFF38383A),
        scrim = Color(0xFF000000),
        surfaceBright = Color(0xFF3A3A3C),
        surfaceDim = Color(0xFF0B0B0C),
        surfaceContainerLowest = Color(0xFF000000),
        surfaceContainerLow = Color(0xFF1C1C1E),
        surfaceContainer = Color(0xFF1C1C1E),
        surfaceContainerHigh = Color(0xFF2C2C2E),
        surfaceContainerHighest = Color(0xFF3A3A3C),
    )
}
