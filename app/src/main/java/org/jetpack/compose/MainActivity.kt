package org.jetpack.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.*
import androidx.navigation.compose.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import org.jetpack.compose.ui.theme.MyAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        composable("home") { HomeScreen() }
        composable("search") { SearchScreen() }
        composable("settings") { SettingsScreen() }
    }
}

@Composable
fun HomeScreen() {
    Text("This is the Home Screen")
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
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentDestination == item,
                onClick = { navController.navigate(item) },
                label = { Text(item.capitalize()) },
                icon = { Icon(Icons.Default.Home, contentDescription = item) }
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