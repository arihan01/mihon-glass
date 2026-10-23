package eu.kanade.tachiyomi.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabNavigator
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import eu.kanade.presentation.util.Screen
import eu.kanade.presentation.util.isTabletUi
import eu.kanade.tachiyomi.ui.browse.BrowseTab
import eu.kanade.tachiyomi.ui.download.DownloadQueueScreen
import eu.kanade.tachiyomi.ui.history.HistoryTab
import eu.kanade.tachiyomi.ui.library.LibraryTab
import eu.kanade.tachiyomi.ui.manga.MangaScreen
import eu.kanade.tachiyomi.ui.more.MoreTab
import eu.kanade.tachiyomi.ui.updates.UpdatesTab
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import mihon.app.di.appGraph
import soup.compose.material.motion.animation.materialFadeThroughIn
import soup.compose.material.motion.animation.materialFadeThroughOut
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.LocalFloatingBarPadding
import tachiyomi.presentation.core.components.material.glass
import tachiyomi.presentation.core.components.material.glassFooter
import tachiyomi.presentation.core.i18n.pluralStringResource

object HomeScreen : Screen() {

    private val librarySearchEvent = Channel<String>()
    private val openTabEvent = Channel<Tab>()
    private val showBottomNavEvent = Channel<Boolean>()

    @Suppress("ConstPropertyName")
    private const val TabFadeDuration = 200

    @Suppress("ConstPropertyName")
    private const val TabNavigatorKey = "HomeTabs"

    private val FloatingTabBarHeight = 64.dp
    private val FloatingTabBarGap = 8.dp

    private val TABS = listOf(
        LibraryTab,
        UpdatesTab,
        HistoryTab,
        BrowseTab,
        MoreTab,
    )

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        TabNavigator(
            tab = LibraryTab,
            key = TabNavigatorKey,
        ) { tabNavigator ->
            // Provide usable navigator to content screen
            CompositionLocalProvider(LocalNavigator provides navigator) {
                val tabletUi = isTabletUi()
                val navigationSuiteState = rememberNavigationSuiteScaffoldState()
                var bottomBarVisible by remember { mutableStateOf(true) }
                LaunchedEffect(navigationSuiteState, tabletUi) {
                    if (tabletUi) navigationSuiteState.show()
                    showBottomNavEvent.receiveAsFlow().collectLatest { show ->
                        bottomBarVisible = show
                        if (tabletUi || show) {
                            navigationSuiteState.show()
                        } else {
                            navigationSuiteState.hide()
                        }
                    }
                }

                val tabContent = @Composable {
                    AnimatedContent(
                        targetState = tabNavigator.current,
                        transitionSpec = {
                            materialFadeThroughIn(
                                initialScale = 1f,
                                durationMillis = TabFadeDuration,
                            ) togetherWith materialFadeThroughOut(durationMillis = TabFadeDuration)
                        },
                        label = "tabContent",
                    ) {
                        tabNavigator.saveableState(key = "currentTab", it) {
                            it.Content()
                        }
                    }
                }

                if (tabletUi) {
                    NavigationSuiteScaffold(
                        navigationSuiteType = NavigationSuiteType.NavigationRail,
                        state = navigationSuiteState,
                        navigationSuiteColors = NavigationSuiteDefaults.colors(
                            navigationRailContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        ),
                        navigationItemVerticalArrangement = Arrangement.Center,
                        navigationItems = {
                            TABS.fastForEach { NavigationSuiteItem(it, NavigationSuiteType.NavigationRail) }
                        },
                        content = tabContent,
                    )
                } else {
                    FloatingTabBarLayout(visible = bottomBarVisible, content = tabContent)
                }
            }

            val goToLibraryTab = { tabNavigator.current = LibraryTab }

            BackHandler(enabled = tabNavigator.current != LibraryTab, onBack = goToLibraryTab)

            LaunchedEffect(Unit) {
                launch {
                    librarySearchEvent.receiveAsFlow().collectLatest {
                        goToLibraryTab()
                        LibraryTab.search(it)
                    }
                }
                launch {
                    openTabEvent.receiveAsFlow().collectLatest {
                        tabNavigator.current = when (it) {
                            is Tab.Library -> LibraryTab
                            Tab.Updates -> UpdatesTab
                            Tab.History -> HistoryTab
                            is Tab.Browse -> {
                                if (it.toExtensions) {
                                    BrowseTab.showExtension()
                                }
                                BrowseTab
                            }
                            is Tab.More -> MoreTab
                        }

                        if (it is Tab.Library && it.mangaIdToOpen != null) {
                            navigator.push(MangaScreen(it.mangaIdToOpen))
                        }
                        if (it is Tab.More && it.toDownloads) {
                            navigator.push(DownloadQueueScreen)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun NavigationSuiteItem(
        tab: eu.kanade.presentation.util.Tab,
        navigationSuiteType: NavigationSuiteType,
    ) {
        val (selected, onClick) = rememberTabSelection(tab)
        NavigationSuiteItem(
            navigationSuiteType = navigationSuiteType,
            selected = selected,
            onClick = onClick,
            icon = {
                Icon(
                    painter = tab.options.icon!!,
                    contentDescription = tab.options.title,
                )
            },
            label = {
                Text(
                    text = tab.options.title,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            badge = tabBadge(tab),
        )
    }

    @Composable
    private fun rememberTabSelection(tab: eu.kanade.presentation.util.Tab): Pair<Boolean, () -> Unit> {
        val tabNavigator = LocalTabNavigator.current
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val selected = tabNavigator.current::class == tab::class
        return selected to {
            if (!selected) {
                tabNavigator.current = tab
            } else {
                scope.launch { tab.onReselect(navigator) }
            }
        }
    }

    /**
     * iOS 26 style tab bar: a capsule of frosted glass floating over the tab content, which scrolls
     * underneath it. [LocalFloatingBarPadding] tells the tab's Scaffold how much to inset its content.
     */
    @Composable
    private fun FloatingTabBarLayout(
        visible: Boolean,
        content: @Composable () -> Unit,
    ) {
        val hazeState = rememberHazeState()
        val barPadding by animateDpAsState(
            targetValue = if (visible) FloatingTabBarHeight + FloatingTabBarGap * 2 else 0.dp,
            label = "floatingBarPadding",
        )
        Box(Modifier.fillMaxSize()) {
            CompositionLocalProvider(LocalFloatingBarPadding provides barPadding) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(hazeState),
                ) {
                    content()
                }
            }
            val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(navBarHeight + barPadding)
                    .glassFooter(hazeState),
            )
            AnimatedVisibility(
                visible = visible,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
            ) {
                val shape = CircleShape
                Row(
                    modifier = Modifier
                        .windowInsetsPadding(
                            WindowInsets.navigationBars
                                .union(WindowInsets.displayCutout)
                                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
                        )
                        .padding(horizontal = 16.dp)
                        .padding(bottom = FloatingTabBarGap)
                        .widthIn(max = 480.dp)
                        .fillMaxWidth()
                        .heightIn(min = FloatingTabBarHeight)
                        .height(IntrinsicSize.Min)
                        .shadow(elevation = 12.dp, shape = shape)
                        .glass(hazeState, shape, borderWidth = 0.5.dp)
                        .selectableGroup()
                        .padding(4.dp),
                ) {
                    TABS.fastForEach {
                        FloatingTabItem(
                            tab = it,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun FloatingTabItem(
        tab: eu.kanade.presentation.util.Tab,
        modifier: Modifier = Modifier,
    ) {
        val (selected, onClick) = rememberTabSelection(tab)
        val colors = MaterialTheme.colorScheme
        val tint = if (selected) colors.primary else colors.onSurface
        Column(
            modifier = modifier
                .clip(CircleShape)
                .background(if (selected) colors.onSurface.copy(alpha = 0.1f) else Color.Transparent)
                .selectable(selected = selected, role = Role.Tab, onClick = onClick)
                .padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            BadgedBox(badge = { tabBadge(tab)?.invoke() }) {
                // The label below already names the tab for screen readers
                Icon(painter = tab.options.icon!!, contentDescription = null, tint = tint)
            }
            Text(
                text = tab.options.title,
                color = tint,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }

    @Composable
    private fun tabBadge(tab: eu.kanade.presentation.util.Tab): (@Composable () -> Unit)? {
        val context = LocalContext.current
        val count by produceState(initialValue = 0, tab) {
            val graph = context.appGraph
            when (tab) {
                is UpdatesTab -> {
                    combine(
                        graph.libraryPreferences.newShowUpdatesCount.changes(),
                        graph.libraryPreferences.newUpdatesCount.changes(),
                    ) { show, count ->
                        if (show) count else 0
                    }
                        .collectLatest { value = it }
                }

                is BrowseTab -> {
                    graph.sourcePreferences.extensionUpdatesCount.changes()
                        .collectLatest { value = it }
                }

                else -> value = 0
            }
        }
        if (count <= 0) return null
        return {
            Badge(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
            ) {
                val desc = when (tab) {
                    is UpdatesTab -> pluralStringResource(
                        MR.plurals.notification_chapters_generic,
                        count = count,
                        count,
                    )

                    is BrowseTab -> pluralStringResource(
                        MR.plurals.update_check_notification_ext_updates,
                        count = count,
                        count,
                    )

                    else -> null
                }
                Text(
                    text = count.toString(),
                    modifier = Modifier.semantics {
                        if (desc != null) contentDescription = desc
                    },
                )
            }
        }
    }

    suspend fun search(query: String) {
        librarySearchEvent.send(query)
    }

    suspend fun openTab(tab: Tab) {
        openTabEvent.send(tab)
    }

    suspend fun showBottomNav(show: Boolean) {
        showBottomNavEvent.send(show)
    }

    sealed interface Tab {
        data class Library(val mangaIdToOpen: Long? = null) : Tab
        data object Updates : Tab
        data object History : Tab
        data class Browse(val toExtensions: Boolean = false) : Tab
        data class More(val toDownloads: Boolean) : Tab
    }
}
