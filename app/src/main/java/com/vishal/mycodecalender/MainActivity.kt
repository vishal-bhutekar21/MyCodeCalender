package com.vishal.mycodecalender

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mycodecalendar.core.designsystem.AppTheme
import com.mycodecalendar.core.designsystem.MyCodeCalendarTheme
import com.mycodecalendar.core.designsystem.components.FloatingBottomNavigation
import com.mycodecalendar.data.repository.FakeRepository
import com.mycodecalendar.domain.model.Platform
import com.mycodecalendar.feature.contestdetail.ContestDetailScreen
import com.mycodecalendar.feature.contests.ContestsScreen
import com.mycodecalendar.feature.home.HomeScreen
import com.mycodecalendar.feature.home.HomeViewModel
import com.mycodecalendar.feature.onboarding.OnboardingScreen
import com.mycodecalendar.feature.platformdetail.PlatformDetailScreen
import com.mycodecalendar.feature.platforms.AddPlatformScreen
import com.mycodecalendar.feature.resources.ResourcesScreen
import com.mycodecalendar.feature.settings.SettingsScreen

class MainActivity : ComponentActivity() {

    // Pass applicationContext so repository can access SharedPreferences
    private val repository by lazy { FakeRepository(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var appTheme by remember { mutableStateOf(AppTheme.SYSTEM) }

            MyCodeCalendarTheme(appTheme = appTheme) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: "home"

                val homeViewModel = remember { HomeViewModel(repository) }
                val homeUiState by homeViewModel.uiState.collectAsState()
                val isRefreshing by homeViewModel.isRefreshing.collectAsState()

                val contests by repository.getContests().collectAsState(initial = emptyList())
                val connectedAccounts by repository.getConnectedAccounts().collectAsState(initial = emptyList())
                val connectedStats by repository.getAllConnectedStats().collectAsState(initial = emptyList())
                val resources by repository.getResources().collectAsState(initial = emptyList())

                val bottomNavRoutes = setOf("home", "contests", "resources", "settings")
                val showBottomBar = currentRoute in bottomNavRoutes

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            FloatingBottomNavigation(
                                currentRoute = currentRoute,
                                onTabSelected = { route ->
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("onboarding") {
                            OnboardingScreen(
                                onComplete = {
                                    navController.navigate("home") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("home") {
                            HomeScreen(
                                uiState = homeUiState,
                                onAddPlatformClick = { navController.navigate("add_platform") },
                                onPlatformClick = { platform ->
                                    navController.navigate("platform_detail/${platform.name}")
                                },
                                onContestClick = { id ->
                                    navController.navigate("contest_detail/$id")
                                },
                                onViewAllContestsClick = { navController.navigate("contests") },
                                onResourceClick = { url -> openUrl(url) },
                                isRefreshing = isRefreshing,
                                onRefresh = { homeViewModel.refresh() }
                            )
                        }

                        composable("contests") {
                            ContestsScreen(
                                contests = contests,
                                onContestClick = { id ->
                                    navController.navigate("contest_detail/$id")
                                }
                            )
                        }

                        composable("contest_detail/{contestId}") { back ->
                            val id = back.arguments?.getString("contestId") ?: ""
                            val contest = contests.find { it.id == id } ?: contests.firstOrNull()
                            if (contest != null) {
                                ContestDetailScreen(
                                    contest = contest,
                                    onBackClick = { navController.popBackStack() },
                                    onJoinClick = { url -> openUrl(url) },
                                    onAddToCalendarClick = {
                                        Toast.makeText(this@MainActivity, "Added to calendar", Toast.LENGTH_SHORT).show()
                                    },
                                    onSetReminderClick = {
                                        Toast.makeText(this@MainActivity, "Reminder set — 15 min before", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }

                        composable("add_platform") {
                            AddPlatformScreen(
                                connectedAccounts = connectedAccounts,
                                onAddPlatform = { platform, handle ->
                                    repository.addPlatformAccount(platform, handle)
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Connected @$handle",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.popBackStack()
                                },
                                onRemovePlatform = { platform ->
                                    repository.removePlatformAccount(platform)
                                },
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable("platform_detail/{platformName}") { back ->
                            val name = back.arguments?.getString("platformName") ?: "CODEFORCES"
                            val platform = runCatching { Platform.valueOf(name) }.getOrDefault(Platform.CODEFORCES)
                            val acc = connectedAccounts.firstOrNull { it.platform == platform }
                            val stat = connectedStats.firstOrNull { it.platform == platform }
                            val history by repository.getRatingHistory(
                                platform, acc?.username ?: ""
                            ).collectAsState(initial = emptyList())

                            PlatformDetailScreen(
                                stats = stat,
                                ratingHistory = history,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable("resources") {
                            ResourcesScreen(
                                resources = resources,
                                onResourceClick = { url -> openUrl(url) }
                            )
                        }

                        composable("settings") {
                            SettingsScreen(
                                connectedAccounts = connectedAccounts,
                                currentTheme = appTheme,
                                onThemeChange = { appTheme = it },
                                onAddPlatformClick = { navController.navigate("add_platform") },
                                onManageAccountClick = { acc ->
                                    navController.navigate("platform_detail/${acc.platform.name}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun openUrl(url: String) {
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }.onFailure {
            Toast.makeText(this, "Could not open link", Toast.LENGTH_SHORT).show()
        }
    }
}