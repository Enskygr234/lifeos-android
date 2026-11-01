package com.lifeos.app.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lifeos.app.ui.screens.CalendarScreen
import com.lifeos.app.ui.screens.HabitsScreen
import com.lifeos.app.ui.screens.HomeScreen
import com.lifeos.app.ui.screens.NotesScreen
import com.lifeos.app.ui.screens.SettingsScreen
import com.lifeos.app.ui.screens.StatsScreen
import com.lifeos.app.ui.screens.TasksScreen

enum class Dest(val route: String, val label: String, val title: String, val icon: ImageVector) {
    Home("home", "Ana Sayfa", "LifeOS", Icons.Filled.Dashboard),
    Tasks("tasks", "Görevler", "Görevler", Icons.Filled.CheckCircle),
    Habits("habits", "Alışkanlık", "Alışkanlıklar", Icons.Filled.Loop),
    Notes("notes", "Notlar", "Not Defteri", Icons.Filled.StickyNote2),
    Calendar("calendar", "Takvim", "Takvim", Icons.Filled.CalendarMonth),
    Stats("stats", "İstatistik", "İstatistikler", Icons.Filled.BarChart),
    Settings("settings", "Ayarlar", "Ayarlar", Icons.Filled.Settings),
}

private val bottomDests = listOf(Dest.Home, Dest.Tasks, Dest.Habits, Dest.Notes, Dest.Calendar)

fun NavHostController.goTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeOSRoot(vm: AppViewModel) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: Dest.Home.route
    val current = Dest.entries.firstOrNull { it.route == route } ?: Dest.Home
    val isSecondary = current == Dest.Stats || current == Dest.Settings

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        current.title,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("screen_title"),
                    )
                },
                navigationIcon = {
                    if (isSecondary) {
                        IconButton(onClick = { nav.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri")
                        }
                    }
                },
                actions = {
                    if (current != Dest.Stats) {
                        IconButton(onClick = { nav.navigate(Dest.Stats.route) { launchSingleTop = true } },
                            modifier = Modifier.testTag("action_stats")) {
                            Icon(Icons.Filled.BarChart, "İstatistikler")
                        }
                    }
                    if (current != Dest.Settings) {
                        IconButton(onClick = { nav.navigate(Dest.Settings.route) { launchSingleTop = true } },
                            modifier = Modifier.testTag("action_settings")) {
                            Icon(Icons.Filled.Settings, "Ayarlar")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                bottomDests.forEach { d ->
                    NavigationBarItem(
                        selected = route == d.route,
                        onClick = { nav.goTab(d.route) },
                        icon = { Icon(d.icon, d.label) },
                        label = {
                            Text(d.label, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis,
                                fontSize = 10.5.sp)
                        },
                        modifier = Modifier.testTag("nav_${d.route}"),
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Dest.Home.route,
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(tween(240)) + slideInVertically(tween(240)) { it / 24 } },
            exitTransition = { fadeOut(tween(140)) },
            popEnterTransition = { fadeIn(tween(240)) },
            popExitTransition = { fadeOut(tween(140)) },
        ) {
            composable(Dest.Home.route) { HomeScreen(vm, onOpen = { nav.goTab(it) }) }
            composable(Dest.Tasks.route) { TasksScreen(vm) }
            composable(Dest.Habits.route) { HabitsScreen(vm) }
            composable(Dest.Notes.route) { NotesScreen(vm) }
            composable(Dest.Calendar.route) { CalendarScreen(vm) }
            composable(Dest.Stats.route) { StatsScreen(vm) }
            composable(Dest.Settings.route) { SettingsScreen(vm) }
        }
    }
}
