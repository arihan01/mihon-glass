package eu.kanade.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.theme.colorscheme.TachiyomiColorScheme

@Composable
fun TachiyomiTheme(content: @Composable () -> Unit) {
    MaterialExpressiveTheme(
        colorScheme = if (isSystemInDarkTheme()) TachiyomiColorScheme.darkScheme else TachiyomiColorScheme.lightScheme,
        shapes = CupertinoShapes,
        content = content,
    )
}

@Composable
fun TachiyomiPreviewTheme(content: @Composable () -> Unit) = TachiyomiTheme(content)

// iOS 26 radii: grouped cells/cards ~12-18pt, sheets and alerts ~34pt
private val CupertinoShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(34.dp),
)
