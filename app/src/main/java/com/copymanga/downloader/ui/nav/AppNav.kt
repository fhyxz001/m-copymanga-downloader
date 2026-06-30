package com.copymanga.downloader.ui.nav

import android.content.pm.PackageManager
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.copymanga.downloader.di.AppContainer
import com.copymanga.downloader.ui.components.CopyMangaTopAppBar
import com.copymanga.downloader.ui.components.TopBarMenuAction
import com.copymanga.downloader.ui.dialog.AboutDialog
import com.copymanga.downloader.ui.dialog.LoginDialog
import com.copymanga.downloader.ui.dialog.LogsDialog
import com.copymanga.downloader.ui.dialog.SettingsDialog
import com.copymanga.downloader.ui.screens.chapter.ChapterScreen
import com.copymanga.downloader.ui.screens.downloaded.DownloadedScreen
import com.copymanga.downloader.ui.screens.favorite.FavoriteScreen
import com.copymanga.downloader.ui.screens.progress.ProgressScreen
import com.copymanga.downloader.ui.screens.search.SearchScreen

private const val CHAPTER_ROUTE_PREFIX = "chapter"

@Composable
fun AppNav(container: AppContainer) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Destination.startRoute
    val context = LocalContext.current
    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (_: PackageManager.NameNotFoundException) {
            "0.1.0"
        }
    }

    var showLogin by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showLogs by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CopyMangaTopAppBar(
                title = currentRoute.toTitleRes()?.let { stringResource(it) }
                    ?: stringResource(com.copymanga.downloader.R.string.app_title),
                onMenuAction = { action ->
                    when (action) {
                        TopBarMenuAction.Login -> showLogin = true
                        TopBarMenuAction.Settings -> showSettings = true
                        TopBarMenuAction.Logs -> showLogs = true
                        TopBarMenuAction.About -> showAbout = true
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                Destination.entries.forEach { dest ->
                    val selected = when (dest) {
                        Destination.Chapter -> currentRoute?.startsWith(CHAPTER_ROUTE_PREFIX) == true
                        else -> currentRoute == dest.route
                    }
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = stringResource(dest.labelRes)) },
                        label = { Text(stringResource(dest.labelRes)) },
                    )
                }
            }
        },
    ) { inner ->
        NavHost(
            navController = navController,
            startDestination = Destination.startRoute,
            modifier = Modifier.padding(inner),
        ) {
            composable(Destination.Search.route) {
                SearchScreen(
                    container = container,
                    onComicClick = { pathWord ->
                        navController.navigate("$CHAPTER_ROUTE_PREFIX/$pathWord")
                    },
                )
            }
            composable(Destination.Favorite.route) {
                FavoriteScreen(
                    container = container,
                    onComicClick = { pathWord ->
                        navController.navigate("$CHAPTER_ROUTE_PREFIX/$pathWord")
                    },
                )
            }
            composable(Destination.Downloaded.route) {
                DownloadedScreen(
                    container = container,
                    onComicClick = { pathWord ->
                        navController.navigate("$CHAPTER_ROUTE_PREFIX/$pathWord")
                    },
                )
            }
            composable(
                route = Destination.Chapter.route,
                arguments = listOf(
                    navArgument("pathWord") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                ),
            ) { backStackEntry ->
                val pathWord = backStackEntry.arguments?.getString("pathWord") ?: ""
                ChapterScreen(container = container, pathWord = pathWord)
            }
            composable(Destination.Progress.route) {
                ProgressScreen(container = container)
            }
        }
    }

    if (showLogin) {
        LoginDialog(
            container = container,
            onDismiss = { showLogin = false },
        )
    }
    if (showSettings) {
        SettingsDialog(
            container = container,
            onDismiss = { showSettings = false },
        )
    }
    if (showLogs) {
        LogsDialog(
            container = container,
            onDismiss = { showLogs = false },
        )
    }
    if (showAbout) {
        AboutDialog(
            versionName = versionName,
            onDismiss = { showAbout = false },
        )
    }
}

private fun String?.toTitleRes(): Int? {
    if (this == null) return null
    return when {
        startsWith(Destination.Search.route) -> Destination.Search.labelRes
        startsWith(Destination.Favorite.route) -> Destination.Favorite.labelRes
        startsWith(Destination.Downloaded.route) -> Destination.Downloaded.labelRes
        startsWith(CHAPTER_ROUTE_PREFIX) -> Destination.Chapter.labelRes
        startsWith(Destination.Progress.route) -> Destination.Progress.labelRes
        else -> null
    }
}
