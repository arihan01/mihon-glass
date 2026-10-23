package eu.kanade.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.launch
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.material.TabText
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun TabbedScreen(
    titleRes: StringResource,
    tabs: List<TabContent>,
    state: PagerState = rememberPagerState { tabs.size },
    searchQuery: String? = null,
    onChangeSearchQuery: (String?) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            val tab = tabs[state.currentPage]
            val searchEnabled = tab.searchEnabled

            Column {
                SearchToolbar(
                    titleContent = { AppBarTitle(stringResource(titleRes)) },
                    searchEnabled = searchEnabled,
                    searchQuery = if (searchEnabled) searchQuery else null,
                    onChangeSearchQuery = onChangeSearchQuery,
                    actions = { AppBarActions(tab.actions) },
                )
                PrimaryTabRow(
                    selectedTabIndex = state.currentPage,
                    containerColor = Color.Transparent,
                    divider = {},
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = state.currentPage == index,
                            onClick = { scope.launch { state.animateScrollToPage(index) } },
                            text = { TabText(text = stringResource(tab.titleRes), badgeCount = tab.badgeNumber) },
                            unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { contentPadding ->
        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            state = state,
            verticalAlignment = Alignment.Top,
        ) { page ->
            tabs[page].content(contentPadding, snackbarHostState)
        }
    }
}

data class TabContent(
    val titleRes: StringResource,
    val badgeNumber: Int? = null,
    val searchEnabled: Boolean = false,
    val actions: List<AppBar.AppBarAction> = listOf(),
    val content: @Composable (contentPadding: PaddingValues, snackbarHostState: SnackbarHostState) -> Unit,
)
