package eu.kanade.presentation.more.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import eu.kanade.domain.ui.model.setAppCompatDelegateThemeMode
import eu.kanade.presentation.more.settings.widget.AppThemeModePreferenceWidget
import mihon.app.di.appGraph
import tachiyomi.presentation.core.util.collectAsState

internal class ThemeStep : OnboardingStep {

    override val isComplete: Boolean = true

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val uiPreferences = remember { context.appGraph.uiPreferences }
        val themeModePref = uiPreferences.themeMode
        val themeMode by themeModePref.collectAsState()

        AppThemeModePreferenceWidget(
            value = themeMode,
            onItemClick = {
                themeModePref.set(it)
                setAppCompatDelegateThemeMode(it)
            },
        )
    }
}
