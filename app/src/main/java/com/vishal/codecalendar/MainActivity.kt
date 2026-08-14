package com.vishal.codecalendar

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.mycodecalendar.core.common.NetworkMonitor
import com.mycodecalendar.core.database.MyCodeCalendarDatabase
import com.mycodecalendar.core.designsystem.AppTheme
import com.mycodecalendar.core.designsystem.MyCodeCalendarTheme
import com.mycodecalendar.core.designsystem.components.FloatingBottomNavigation
import com.mycodecalendar.core.designsystem.components.GlassCard
import com.mycodecalendar.data.repository.FakeRepository
import com.mycodecalendar.domain.model.Contest
import com.mycodecalendar.domain.model.Platform
import com.mycodecalendar.domain.model.PlatformAccount
import com.mycodecalendar.feature.contestdetail.ContestDetailScreen
import com.mycodecalendar.feature.contests.ContestsScreen
import com.mycodecalendar.feature.home.HomeScreen
import com.mycodecalendar.feature.home.HomeViewModel
import com.mycodecalendar.feature.home.StreakScreen
import com.mycodecalendar.feature.onboarding.AuthScreen
import com.mycodecalendar.feature.onboarding.OnboardingScreen
import com.mycodecalendar.feature.onboarding.SplashScreen
import com.mycodecalendar.feature.platformdetail.PlatformDetailScreen
import com.mycodecalendar.feature.platforms.AddPlatformScreen
import com.mycodecalendar.feature.resources.ResourcesScreen
import com.mycodecalendar.feature.settings.SettingsScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant

class MainActivity : ComponentActivity() {

    private lateinit var database: MyCodeCalendarDatabase
    private lateinit var repository: FakeRepository
    private lateinit var homeViewModel: HomeViewModel
    private var networkMonitor: NetworkMonitor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = Room.databaseBuilder(
            applicationContext,
            MyCodeCalendarDatabase::class.java,
            "mycodecalendar.db"
        ).fallbackToDestructiveMigration().build()

        repository = FakeRepository(this, database)
        homeViewModel = HomeViewModel(repository)

        networkMonitor = NetworkMonitor(this)
        lifecycleScope.launch {
            networkMonitor?.isOnline?.collect { isOnline ->
                repository.onConnectivityChanged(isOnline)
            }
        }

        setContent {
            val isDarkTheme = isSystemInDarkTheme()
            var appTheme by remember { mutableStateOf(AppTheme.SYSTEM) }

            DisposableEffect(isDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        lightScrim = Color.Transparent.toArgb(),
                        darkScrim = Color.Transparent.toArgb(),
                        detectDarkMode = { isDarkTheme }
                    ),
                    navigationBarStyle = SystemBarStyle.auto(
                        lightScrim = Color.Transparent.toArgb(),
                        darkScrim = Color.Transparent.toArgb(),
                        detectDarkMode = { isDarkTheme }
                    )
                )
                onDispose {}
            }

            MyCodeCalendarTheme(appTheme = appTheme) {
                val authPrefs = remember { getSharedPreferences("app_auth_prefs", Context.MODE_PRIVATE) }
                var authUsername by remember { mutableStateOf(authPrefs.getString("auth_username", "Developer")) }
                var authMethod by remember { mutableStateOf(authPrefs.getString("auth_method", "Guest")) }
                val onboardingCompleted = remember { authPrefs.getBoolean("onboarding_completed", false) }
                val isLoggedIn = remember { authPrefs.getBoolean("is_logged_in", false) }
                val startDest = remember {
                    if (!onboardingCompleted) "onboarding"
                    else if (!isLoggedIn) "auth"
                    else "home"
                }

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val connectedAccounts by repository.getConnectedAccounts()
                    .collectAsState(initial = emptyList())

                val contests by repository.getContests()
                    .collectAsState(initial = emptyList())

                val resources by repository.getResources()
                    .collectAsState(initial = emptyList())

                val homeUiState by homeViewModel.uiState.collectAsState()
                val fetchError by homeViewModel.fetchError.collectAsState()
                val isRefreshing by homeViewModel.isRefreshing.collectAsState()

                var showErrorBanner by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf("") }

                val showBottomBar = currentRoute in listOf(
                    "home", "contests", "resources", "settings"
                )

                LaunchedEffect(fetchError) {
                    val err = fetchError
                    if (!err.isNullOrEmpty()) {
                        errorMessage = err ?: ""
                        showErrorBanner = true
                        delay(4000L)
                        showErrorBanner = false
                    }
                }

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            FloatingBottomNavigation(
                                currentRoute = currentRoute ?: "home",
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
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavHost(
                            navController = navController,
                            startDestination = "splash"
                        ) {
                            composable("splash") {
                                SplashScreen(
                                    onSplashFinished = {
                                        val target = if (!onboardingCompleted) "onboarding"
                                        else if (!isLoggedIn) "auth"
                                        else "home"
                                        navController.navigate(target) {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable("onboarding") {
                                OnboardingScreen(
                                    onComplete = {
                                        navController.navigate("auth") {
                                            popUpTo("onboarding") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable("auth") {
                                AuthScreen(
                                    onAuthSuccess = { user, method ->
                                        authPrefs.edit()
                                            .putBoolean("onboarding_completed", true)
                                            .putBoolean("is_logged_in", true)
                                            .putString("auth_username", user)
                                            .putString("auth_method", method)
                                            .apply()
                                        authUsername = user
                                        authMethod = method
                                        navController.navigate("home") {
                                            popUpTo("auth") { inclusive = true }
                                        }
                                    },
                                    onGuestBypass = {
                                        authPrefs.edit()
                                            .putBoolean("onboarding_completed", true)
                                            .putBoolean("is_logged_in", true)
                                            .putString("auth_username", "Guest Developer")
                                            .putString("auth_method", "Guest")
                                            .apply()
                                        authUsername = "Guest Developer"
                                        authMethod = "Guest"
                                        navController.navigate("home") {
                                            popUpTo("auth") { inclusive = true }
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
                                    onStreakClick = { navController.navigate("streak") },
                                    isRefreshing = isRefreshing,
                                    onRefresh = { homeViewModel.refresh() }
                                )
                            }

                            composable("contests") {
                                val pastContests by repository.getPastContestHistory()
                                    .collectAsState(initial = emptyList())
                                ContestsScreen(
                                    contests = contests,
                                    pastContests = pastContests,
                                    onContestClick = { id ->
                                        navController.navigate("contest_detail/$id")
                                    },
                                    onAddPlatformClick = { navController.navigate("add_platform") },
                                    onPastContestClick = { url -> openUrl(url) }
                                )
                            }

                            composable("contest_detail/{contestId}") { back ->
                                val id = back.arguments?.getString("contestId") ?: ""
                                val contest = contests.find { it.id == id }
                                    ?: contests.firstOrNull()
                                if (contest != null) {
                                    ContestDetailScreen(
                                        contest = contest,
                                        onBackClick = { navController.popBackStack() },
                                        onJoinClick = { url -> openUrl(url) },
                                        onAddToCalendarClick = { contestItem ->
                                            addToSystemCalendar(contestItem)
                                        },
                                        onSetReminderClick = { contestItem ->
                                            Toast.makeText(
                                                this@MainActivity,
                                                "✓ Reminder set 15 min before ${contestItem.name}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                            }

                            composable("add_platform") {
                                AddPlatformScreen(
                                    connectedAccounts = connectedAccounts,
                                    onAddPlatform = { platform, username ->
                                        repository.addPlatformAccount(platform, username)
                                    },
                                    onValidateHandle = { platform, username ->
                                        repository.validateHandle(platform, username)
                                    },
                                    onRemovePlatform = { platform ->
                                        repository.removePlatformAccount(platform)
                                    },
                                    onBackClick = { navController.popBackStack() }
                                )
                            }

                            composable("platform_detail/{platformName}") { back ->
                                val pName = back.arguments?.getString("platformName") ?: ""
                                val platform = runCatching { Platform.valueOf(pName) }.getOrDefault(Platform.CODEFORCES)
                                val account = connectedAccounts.find { it.platform == platform }
                                val username = account?.username ?: "guest"

                                val stats by repository.getPlatformStats(platform, username)
                                    .collectAsState(initial = null)
                                val ratingHistory by repository.getRatingHistory(platform, username)
                                    .collectAsState(initial = emptyList())

                                PlatformDetailScreen(
                                    stats = stats,
                                    ratingHistory = ratingHistory,
                                    gitHubStats = homeUiState.gitHubStats,
                                    onOpenUrl = { url -> openUrl(url) },
                                    onBackClick = { navController.popBackStack() }
                                )
                            }

                            composable("resources") {
                                ResourcesScreen(
                                    resources = resources,
                                    onResourceClick = { url -> openUrl(url) }
                                )
                            }

                            composable("streak") {
                                val streakInfo = homeUiState.streakInfo
                                if (streakInfo != null) {
                                    StreakScreen(
                                        streakInfo = streakInfo,
                                        onBackClick = { navController.popBackStack() },
                                        onShareStreak = {
                                            val streak = streakInfo.currentStreak
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(
                                                    Intent.EXTRA_SUBJECT,
                                                    "🔥 $streak-Day Coding Streak on Code Calendar!"
                                                )
                                                putExtra(
                                                    Intent.EXTRA_TEXT,
                                                    "🔥 I'm on a $streak-day coding streak on Code Calendar!\n\nTrack LeetCode, Codeforces, CodeChef & AtCoder contests, view real-time rating charts, and sync contests directly to your calendar.\n\n📲 Download: https://play.google.com/store/apps/dev?id=8656025420118431472"
                                                )
                                            }
                                            this@MainActivity.startActivity(Intent.createChooser(shareIntent, "Share Coding Streak via"))
                                        }
                                    )
                                } else {
                                    navController.popBackStack()
                                }
                            }

                            composable("settings") {
                                SettingsScreen(
                                    connectedAccounts = connectedAccounts,
                                    currentTheme = appTheme,
                                    onThemeChange = { appTheme = it },
                                    onAddPlatformClick = { navController.navigate("add_platform") },
                                    onManageAccountClick = { acc ->
                                        navController.navigate("platform_detail/${acc.platform.name}")
                                    },
                                    authUsername = authUsername,
                                    authMethod = authMethod,
                                    currentStreak = homeUiState.streakInfo?.currentStreak ?: 14,
                                    onSignOutClick = {
                                        authPrefs.edit().putBoolean("is_logged_in", false).apply()
                                        navController.navigate("auth") {
                                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                                        }
                                    },
                                    onReplayOnboardingClick = {
                                        navController.navigate("onboarding")
                                    },
                                    onShareApp = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(
                                                Intent.EXTRA_SUBJECT,
                                                "Download Code Calendar - Live Contest Radar & Developer Hub"
                                            )
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "⚡ Check out Code Calendar for Android!\n\nTrack competitive programming contests across LeetCode, Codeforces, CodeChef, and AtCoder in real-time, view live rating charts, and sync contests directly to your calendar.\n\nDownload: https://play.google.com/store/apps/dev?id=8656025420118431472"
                                            )
                                        }
                                        this@MainActivity.startActivity(Intent.createChooser(shareIntent, "Share Code Calendar via"))
                                    },
                                    onShareProfileText = { text ->
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(
                                                Intent.EXTRA_SUBJECT,
                                                "Code Calendar - Developer Profile & Streak Card"
                                            )
                                            putExtra(Intent.EXTRA_TEXT, text)
                                        }
                                        this@MainActivity.startActivity(Intent.createChooser(shareIntent, "Share Profile Card via"))
                                    },
                                    onOpenUrl = { url ->
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            this@MainActivity.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(this@MainActivity, "Could not open link", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        }

                        if (showErrorBanner) {
                            GlassCard(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                                    .fillMaxWidth(),
                                accentColor = Color(0xFFF43F5E),
                                cornerRadius = 14.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFF43F5E),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = errorMessage,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        networkMonitor?.unregister()
    }

    private fun openUrl(url: String) {
        if (url.isBlank()) {
            Toast.makeText(this, "No URL available for this contest", Toast.LENGTH_SHORT).show()
            return
        }
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }.onFailure {
            Toast.makeText(this, "Could not open URL: $url", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addToSystemCalendar(contest: Contest) {
        runCatching {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, contest.name)
                putExtra(CalendarContract.Events.DESCRIPTION, "Official Contest URL: ${contest.officialUrl}")
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, contest.startTimeUtc.toEpochMilli())
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, contest.endTimeUtc.toEpochMilli())
            }
            startActivity(intent)
        }.onFailure {
            Toast.makeText(this, "Calendar app not found", Toast.LENGTH_SHORT).show()
        }
    }
}