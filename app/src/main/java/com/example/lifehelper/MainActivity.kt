package com.example.lifehelper

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lifehelper.ui.course.CourseScreen
import com.example.lifehelper.ui.goal.GoalScreen
import com.example.lifehelper.ui.navigation.Destination
import com.example.lifehelper.ui.navigation.bottomDestinations
import com.example.lifehelper.ui.profile.ProfileScreen
import com.example.lifehelper.ui.theme.LifeHelperTheme
import com.example.lifehelper.ui.transaction.TransactionScreen
import com.example.lifehelper.ui.translate.TranslateScreen
import com.example.lifehelper.util.LocaleHelper

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        // 应用语言切换：根据存储偏好包装 Locale
        super.attachBaseContext(LocaleHelper.wrap(newBase, LocaleHelper.resolveLocale(newBase)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val app = application as LifeHelperApp

            // 主题模式：从本地读取，切换时即时更新
            var themeMode by remember {
                mutableStateOf(app.container.profileRepository.getThemeMode())
            }
            val darkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            // Android 13+ 通知权限申请
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { /* 忽略结果，由系统处理 */ }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            LifeHelperTheme(darkTheme = darkTheme) {
                LifeHelperAppRoot(
                    onThemeChanged = { mode ->
                        app.container.profileRepository.setThemeMode(mode)
                        themeMode = mode
                    }
                )
            }
        }
    }
}

@Composable
fun LifeHelperAppRoot(onThemeChanged: (String) -> Unit) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomDestinations.forEach { destination ->
                    val selected = currentDestination?.hierarchy
                        ?.any { it.route == destination.route } == true
                    val labelRes = when (destination) {
                        is Destination.Goal -> R.string.nav_goal
                        is Destination.Course -> R.string.nav_course
                        is Destination.Transaction -> R.string.nav_transaction
                        is Destination.Translate -> R.string.nav_translate
                        is Destination.Profile -> R.string.nav_profile
                    }
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = null) },
                        label = { Text(stringResource(labelRes)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Goal.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Destination.Goal.route) { GoalScreen() }
            composable(Destination.Course.route) { CourseScreen() }
            composable(Destination.Transaction.route) { TransactionScreen() }
            composable(Destination.Translate.route) { TranslateScreen() }
            composable(Destination.Profile.route) { ProfileScreen(onThemeChanged = onThemeChanged) }
        }
    }
}
