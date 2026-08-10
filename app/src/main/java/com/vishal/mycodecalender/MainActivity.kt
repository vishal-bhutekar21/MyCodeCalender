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
import com.mycodecalendar.core.designsystem.MyCodeCalendarTheme
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

    private val repository = FakeRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyCodeCalendarTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val homeViewModel = remember { HomeViewModel(repository) }
                val homeUiState by homeViewModel.uiState.collectAsState()
                val contests by repository.getContests().collectAsState(initial = emptyList())
                val connectedAccounts by repository.getConnectedAccounts().collectAsState(initial = emptyList())
                val connectedStats by repository.getAllConnectedStats().collectAsState(initial = emptyList())
                val resources by repository.getResources().collectAsState(initial = emptyList())

                val bottomNavItems = listOf("home", "contests", "resources", "settings")
                val showBottomBar = currentRoute in bottomNavItems

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = currentRoute == "home",
                                    onClick = {
                                        navController.navigate("home") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Text("🏠") },
                                    label = { Text("Home") }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "contests",
                                    onClick = {
                                        navController.navigate("contests") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Text("🏆") },
                                    label = { Text("Contests") }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "resources",
                                    onClick = {
                                        navController.navigate("resources") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Text("📚") },
                                    label = { Text("Resources") }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "settings",
                                    onClick = {
                                        navController.navigate("settings") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Text("⚙️") },
                                    label = { Text("Settings") }
                                )
                            }
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
                                onComplete = { navController.navigate("home") { popUpTo("onboarding") { inclusive = true } } }
                            )
                        }

                        composable("home") {
                            HomeScreen(
                                uiState = homeUiState,
                                onAddPlatformClick = { navController.navigate("add_platform") },
                                onPlatformClick = { platform -> navController.navigate("platform_detail/${platform.name}") },
                                onContestClick = { contestId -> navController.navigate("contest_detail/$contestId") },
                                onViewAllContestsClick = { navController.navigate("contests") },
                                onResourceClick = { url -> openUrl(url) }
                            )
                        }

                        composable("contests") {
                            ContestsScreen(
                                contests = contests,
                                onContestClick = { contestId -> navController.navigate("contest_detail/$contestId") }
                            )
                        }

                        composable("contest_detail/{contestId}") { backStackEntry ->
                            val contestId = backStackEntry.arguments?.getString("contestId") ?: ""
                            val contest = contests.find { it.id == contestId } ?: contests.firstOrNull()

                            if (contest != null) {
                                ContestDetailScreen(
                                    contest = contest,
                                    onBackClick = { navController.popBackStack() },
                                    onJoinClick = { url -> openUrl(url) },
                                    onAddToCalendarClick = {
                                        Toast.makeText(this@MainActivity, "Added to device calendar!", Toast.LENGTH_SHORT).show()
                                    },
                                    onSetReminderClick = {
                                        Toast.makeText(this@MainActivity, "Reminder set for 15 minutes before contest!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }

                        composable("add_platform") {
                            AddPlatformScreen(
                                onAddPlatform = { platform, handle ->
                                    repository.addPlatformAccount(platform, handle)
                                    Toast.makeText(this@MainActivity, "Added @$handle on ${platform.name}", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                },
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable("platform_detail/{platformName}") { backStackEntry ->
                            val name = backStackEntry.arguments?.getString("platformName") ?: "CODEFORCES"
                            val platform = try { Platform.valueOf(name) } catch (e: Exception) { Platform.CODEFORCES }
                            val stat = connectedStats.find { it.platform == platform } ?: connectedStats.firstOrNull()
                            val history by repository.getRatingHistory(platform, stat?.username ?: "tourist").collectAsState(initial = emptyList())

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
                                onAddPlatformClick = { navController.navigate("add_platform") },
                                onManageAccountClick = { acc -> navController.navigate("platform_detail/${acc.platform.name}") }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Could not open link: $url", Toast.LENGTH_SHORT).show()
        }
    }
}