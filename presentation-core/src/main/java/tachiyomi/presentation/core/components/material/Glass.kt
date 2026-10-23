package tachiyomi.presentation.core.components.material

import android.app.UiModeManager
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur

/**
 * Extra bottom space a floating glass bar takes over the content, consumed by [Scaffold] so lists
 * can scroll underneath it without their last item being hidden.
 */
val LocalFloatingBarPadding = compositionLocalOf { 0.dp }

/**
 * Android's counterpart to iOS "Increase Contrast" / "Reduce Transparency": the system contrast
 * level (Android 14+). When raised, glass is replaced by an opaque, outlined surface.
 */
@Composable
fun rememberPrefersSolidSurfaces(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return false
    val context = LocalContext.current
    val uiModeManager = remember(context) { context.getSystemService(UiModeManager::class.java) }
    var contrast by remember(uiModeManager) { mutableFloatStateOf(uiModeManager.contrast) }
    DisposableEffect(uiModeManager) {
        val listener = UiModeManager.ContrastChangeListener { contrast = it }
        uiModeManager.addContrastChangeListener(context.mainExecutor, listener)
        onDispose { uiModeManager.removeContrastChangeListener(listener) }
    }
    return contrast > 0f
}

/**
 * Neutral tint shared by every glass surface. Deliberately grey rather than the paper/brand hues so
 * the navigation layer reads as one material sitting above the content.
 */
@Composable
@ReadOnlyComposable
fun glassTint(): Color {
    val colors = MaterialTheme.colorScheme
    return if (colors.surface.luminance() > 0.5f) LightGlassTint else colors.surfaceContainer
}

/**
 * Floating Liquid Glass (tab bar, floating controls). Thinner than the header since it only carries
 * short labels; the tint still keeps text at AA contrast over the blurred content.
 */
@Composable
fun Modifier.glass(
    state: HazeState,
    shape: Shape = RectangleShape,
    borderWidth: Dp = 0.dp,
): Modifier {
    val tint = glassTint()
    if (rememberPrefersSolidSurfaces()) return solidGlass(shape, tint)
    val style = HazeBlurStyle {
        blurRadius(20.dp)
        backgroundColor(tint)
        val alpha = if (tint == LightGlassTint) LIGHT_FLOATING_TINT_ALPHA else FLOATING_TINT_ALPHA
        colorEffects(listOf(HazeColorEffect.tint(tint.copy(alpha = alpha))))
        fallbackColorEffect(HazeColorEffect.tint(tint.copy(alpha = FALLBACK_TINT_ALPHA)))
    }
    return this
        .clip(shape)
        .hazeBlur(HazeInput.Sources(state), style)
        .then(if (borderWidth > 0.dp) Modifier.border(borderWidth, rimColor(), shape) else Modifier)
}

/**
 * iOS 26 scroll edge effect for headers: frost is strongest at the top edge and eases off towards
 * the content, so the header dissolves into what scrolls beneath it instead of ending in a hard line.
 */
@Composable
fun Modifier.glassHeader(state: HazeState): Modifier {
    if (rememberPrefersSolidSurfaces()) return background(MaterialTheme.colorScheme.surfaceContainer)
    return hazeBlur(HazeInput.Sources(state), scrollEdgeStyle(towardsTop = true))
}

/**
 * The bottom scroll edge effect beneath floating bars: content fades into frost towards the screen
 * edge, keeping the system navigation buttons legible over whatever scrolls behind them.
 */
@Composable
fun Modifier.glassFooter(state: HazeState): Modifier {
    if (rememberPrefersSolidSurfaces()) return background(MaterialTheme.colorScheme.surfaceContainer)
    return hazeBlur(HazeInput.Sources(state), scrollEdgeStyle(towardsTop = false))
}

@Composable
private fun scrollEdgeStyle(towardsTop: Boolean): HazeBlurStyle {
    val tint = glassTint()
    // Header carries text over its whole height so it never fully clears; the footer only has to
    // be solid at the very bottom where the system navigation sits
    val (edgeAlpha, innerAlpha, innerIntensity) = if (towardsTop) {
        Triple(HEADER_EDGE_TINT_ALPHA, HEADER_INNER_TINT_ALPHA, 0.4f)
    } else {
        Triple(FOOTER_EDGE_TINT_ALPHA, 0f, 0f)
    }
    val edge = tint.copy(alpha = edgeAlpha)
    val inner = tint.copy(alpha = innerAlpha)
    return HazeBlurStyle {
        blurRadius(24.dp)
        backgroundColor(tint)
        colorEffects(
            listOf(
                HazeColorEffect.tint(
                    if (towardsTop) {
                        Brush.verticalGradient(
                            listOf(edge, inner),
                        )
                    } else {
                        Brush.verticalGradient(listOf(inner, edge))
                    },
                ),
            ),
        )
        progressive(
            HazeProgressive.verticalGradient(
                easing = LinearEasing,
                startIntensity = if (towardsTop) 1f else innerIntensity,
                endIntensity = if (towardsTop) innerIntensity else 1f,
            ),
        )
        fallbackColorEffect(HazeColorEffect.tint(tint.copy(alpha = FALLBACK_TINT_ALPHA)))
    }
}

/**
 * Glass for surfaces whose backdrop isn't Compose content (e.g. the reader's page views), where a
 * live blur can't be sampled: a dense frosted tint with a rim, matching the blurred glass elsewhere.
 */
@Composable
fun Modifier.frostedScrim(shape: Shape = RectangleShape): Modifier {
    val tint = glassTint()
    if (rememberPrefersSolidSurfaces()) return solidGlass(shape, tint)
    return this
        .clip(shape)
        .background(tint.copy(alpha = SCRIM_TINT_ALPHA), shape)
}

@Composable
private fun Modifier.solidGlass(shape: Shape, tint: Color): Modifier = this
    .clip(shape)
    .background(tint, shape)
    .border(1.dp, MaterialTheme.colorScheme.outline, shape)

@Composable
@ReadOnlyComposable
private fun rimColor(): Color {
    val colors = MaterialTheme.colorScheme
    // A light rim reads as the glass edge catching light; in dark mode a hairline of white does that
    return if (colors.surface.luminance() > 0.5f) colors.outlineVariant else Color.White.copy(alpha = 0.14f)
}

private val LightGlassTint = Color(0xFFF2F2F4)

private const val FLOATING_TINT_ALPHA = 0.6f

// Light glass lets more through: blurred content under a pale tint stays legible behind dark labels
private const val LIGHT_FLOATING_TINT_ALPHA = 0.45f
private const val HEADER_EDGE_TINT_ALPHA = 0.8f
private const val HEADER_INNER_TINT_ALPHA = 0.55f
private const val FOOTER_EDGE_TINT_ALPHA = 0.9f
private const val SCRIM_TINT_ALPHA = 0.88f
private const val FALLBACK_TINT_ALPHA = 0.94f
