package org.jetpack.compose

import android.os.Bundle
import android.os.Build
import android.animation.ObjectAnimator
import android.animation.AnimatorListenerAdapter
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.jetpack.compose.ui.theme.JetpackComposeExampleTheme
import androidx.compose.material3.TopAppBar
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material3.*
import androidx.navigation.compose.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import org.jetpack.compose.ui.theme.screens.DetailsScreen
import org.jetpack.compose.ui.theme.MyAppTheme
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.FontWeight
import org.jetpack.compose.ui.theme.screens.HomeScreen

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { splashScreenView ->
                val animator = ObjectAnimator.ofFloat(splashScreenView.view, View.ALPHA, 1f, 0f)
                animator.duration = 0L
                animator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        splashScreenView.remove()
                    }
                })
                animator.start()
            }
        }
        enableEdgeToEdge()
        setContent {
            MyAppTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        topBar = { AppToolbar(navController) }, // Toolbar untuk setiap fragment
        bottomBar = { AppBottomNavigation(navController) }
    ) { innerPadding ->
        NavigationHost(navController, Modifier.padding(innerPadding))
    }
}

@Composable
fun NavigationHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = "home", modifier = modifier) {
        composable("home") { HomeScreen(navController) }
        composable("search") { DetailsScreen(navController) }
        composable("settings") { SettingsScreen() }
        composable("details") { DetailsScreen(navController) }
    }
}

@Composable
fun SearchScreen() {
    Text("This is the Search Screen")
}

@Composable
fun SettingsScreen() {
    Text("This is the Settings Screen")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppToolbar(navController: NavHostController) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route
    TopAppBar(
        title = {
            Text(text = when (currentDestination) {
                "home" -> "Home"
                "search" -> "Search"
                "settings" -> "Settings"
                else -> "Compose App"
            })
        }
    )
}

@Composable
fun AppBottomNavigation(navController: NavHostController) {
    val items = listOf("home", "search", "settings")
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route
    val density = LocalDensity.current
    val insets = WindowInsets.navigationBars
    val bottomPaddingDp = with(density) { insets.getBottom(density).toDp() }
    NavigationBar (
        modifier = Modifier
            .height(64.dp + bottomPaddingDp)
            .padding(top = 0.dp, bottom = 0.dp)
    ){
        items.forEach { item ->
            NavigationBarItem(
                modifier = Modifier.padding(top = 6.dp, bottom = 0.dp).align(Alignment.Bottom),
                selected = currentDestination == item,
                onClick = { if (currentDestination != item) {
                    navController.navigate(item) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                } },
                label = {
                    Text(
                        text = item.capitalize(),
                        fontWeight = if (currentDestination == item) FontWeight.Bold else FontWeight.Normal,
                        color = if (currentDestination == item) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                },
                icon = { Icon(Icons.Default.Home,
                    contentDescription = item,
                    modifier = Modifier.size(24.dp).padding(bottom = 0.dp).align(Alignment.Bottom)
                )}
            )
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name! How are you",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    JetpackComposeExampleTheme {
        Greeting("Android")
    }
}