package com.matan.bar.ui

import android.app.Application
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.matan.bar.BarApp
import com.matan.bar.data.BarState
import com.matan.bar.data.Settings
import com.matan.bar.logic.Bar
import com.matan.bar.logic.Cocktail
import com.matan.bar.logic.Cocktails
import com.matan.bar.logic.Logic
import com.matan.bar.ui.screens.BarScreen
import com.matan.bar.ui.screens.BuilderScreen
import com.matan.bar.ui.screens.CatalogScreen
import com.matan.bar.ui.screens.CocktailScreen
import com.matan.bar.ui.screens.CustomEditScreen
import com.matan.bar.ui.screens.MakeScreen
import com.matan.bar.ui.screens.SettingsScreen
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val barApp = app as BarApp
    val repo = barApp.repo
    val settingsStore = barApp.settings
    val settings: StateFlow<Settings> = settingsStore.flow
    val state: StateFlow<BarState?> = repo.state().stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun all(st: BarState): List<Cocktail> = Cocktails.all + st.custom
    fun find(st: BarState, key: String) = all(st).find { it.key == key }
    fun bar(st: BarState) = Bar(st.stock.keys)

    /** Taste score: your liked styles + what you rated + favourites. */
    fun scorer(st: BarState, s: Settings): (Cocktail) -> Double {
        val all = all(st)
        val rated = st.meta.values.filter { it.rating > 0 }.mapNotNull { m -> all.find { it.key == m.key }?.let { it to m.rating } }
        val favs = st.meta.values.filter { it.favorite }.mapNotNull { m -> all.find { it.key == m.key } }
        return { c -> Logic.tasteScore(c, s.liked, rated, favs) + if (st.meta(c.key).favorite) 3.0 else 0.0 }
    }
}

object Routes {
    const val MAKE = "make"
    const val CATALOG = "catalog"
    const val BAR = "bar"
    const val BUILDER = "builder"
    const val SETTINGS = "settings"
    const val SHOPPING = "shopping"
    fun cocktail(key: String) = "cocktail/$key"
    fun custom(id: Long) = "custom/$id"
}

private data class TabItem(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    TabItem(Routes.MAKE, "להכין", Icons.Default.Home),
    TabItem(Routes.CATALOG, "קטלוג", Icons.AutoMirrored.Filled.List),
    TabItem(Routes.BAR, "הבר שלי", Icons.Default.ShoppingCart),
    TabItem(Routes.BUILDER, "בונה", Icons.Default.Build),
)

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val vm: AppViewModel = viewModel()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val onTab = tabs.any { it.route == route }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (onTab) NavigationBar {
                tabs.forEach { t ->
                    NavigationBarItem(
                        selected = route == t.route,
                        onClick = {
                            nav.navigate(t.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(t.icon, null) },
                        label = { Text(t.label) },
                    )
                }
            }
        },
    ) { pad ->
        NavHost(nav, startDestination = Routes.MAKE, modifier = Modifier.padding(pad)) {
            composable(Routes.MAKE) { MakeScreen(vm, nav) }
            composable(Routes.CATALOG) { CatalogScreen(vm, nav) }
            composable(Routes.BAR) { BarScreen(vm, nav) }
            composable(Routes.BUILDER) { BuilderScreen(vm, nav) }
            composable(Routes.SETTINGS) { SettingsScreen(vm, nav) }
            composable(Routes.SHOPPING) { com.matan.bar.ui.screens.ShoppingScreen(vm, nav) }
            composable("cocktail/{key}", listOf(navArgument("key") { type = NavType.StringType })) {
                CocktailScreen(vm, nav, it.arguments!!.getString("key")!!)
            }
            composable("custom/{id}", listOf(navArgument("id") { type = NavType.LongType })) {
                CustomEditScreen(vm, nav, it.arguments!!.getLong("id"))
            }
        }
    }
}

/** Screen with a top bar. [tab] screens sit above the bottom bar, so they skip the system bottom inset. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen(
    title: String,
    nav: NavController? = null,
    tab: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
    fab: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        contentWindowInsets = if (tab) WindowInsets(0) else ScaffoldDefaults.contentWindowInsets,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (nav != null) IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "חזרה") }
                },
                actions = actions,
            )
        },
        floatingActionButton = fab,
        content = content,
    )
}

