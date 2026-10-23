package eu.kanade.presentation.theme.colorscheme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import io.kotest.assertions.withClue
import io.kotest.matchers.doubles.shouldBeGreaterThanOrEqual
import org.junit.jupiter.api.Test

class TachiyomiColorSchemeContrastTest {

    @Test
    fun `light scheme meets WCAG AA`() = assertAccessible(TachiyomiColorScheme.lightScheme)

    @Test
    fun `dark scheme meets WCAG AA`() = assertAccessible(TachiyomiColorScheme.darkScheme)

    private fun assertAccessible(scheme: ColorScheme) = with(scheme) {
        val surfaces = mapOf(
            "background" to background,
            "surfaceContainerLowest" to surfaceContainerLowest,
            "surfaceContainerLow" to surfaceContainerLow,
            "surfaceContainer" to surfaceContainer,
            "surfaceContainerHigh" to surfaceContainerHigh,
            "surfaceContainerHighest" to surfaceContainerHighest,
        )
        val text = mapOf(
            "onSurface" to onSurface,
            "onSurfaceVariant" to onSurfaceVariant,
            "primary" to primary,
            "secondary" to secondary,
            "tertiary" to tertiary,
            "error" to error,
        )
        for ((surfaceName, surface) in surfaces) {
            for ((textName, color) in text) {
                withClue("$textName on $surfaceName") { contrast(color, surface) shouldBeGreaterThanOrEqual 4.5 }
            }
            withClue("outline on $surfaceName") { contrast(outline, surface) shouldBeGreaterThanOrEqual 3.0 }
        }
        listOf(
            "primary" to (onPrimary to primary),
            "secondary" to (onSecondary to secondary),
            "tertiary" to (onTertiary to tertiary),
            "error" to (onError to error),
            "primaryContainer" to (onPrimaryContainer to primaryContainer),
            "secondaryContainer" to (onSecondaryContainer to secondaryContainer),
            "tertiaryContainer" to (onTertiaryContainer to tertiaryContainer),
            "errorContainer" to (onErrorContainer to errorContainer),
            "inverseSurface" to (inverseOnSurface to inverseSurface),
        ).forEach { (name, pair) ->
            withClue("on-$name") { contrast(pair.first, pair.second) shouldBeGreaterThanOrEqual 4.5 }
        }
    }

    private fun contrast(a: Color, b: Color): Double {
        val la = a.luminance() + 0.05
        val lb = b.luminance() + 0.05
        return (maxOf(la, lb) / minOf(la, lb)).toDouble()
    }
}
