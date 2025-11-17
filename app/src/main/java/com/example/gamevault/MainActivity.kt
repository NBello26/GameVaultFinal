package com.example.gamevault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gamevault.data.SharedPreferencesHelper
import com.example.gamevault.ui.*
import com.example.gamevault.ui.theme.GameVaultTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GameVaultTheme {

                val navController = rememberNavController()
                val prefs = remember { SharedPreferencesHelper(this) }

                var isLoggedIn by remember { mutableStateOf(prefs.isLoggedIn()) }

                Scaffold(
                    bottomBar = {
                        if (isLoggedIn) {
                            NavigationBar {

                                NavigationBarItem(
                                    selected = navController.currentBackStackEntry?.destination?.route == "home",
                                    onClick = { navController.navigate("home") },
                                    label = { Text("Inicio") },
                                    icon = { Icon(Icons.Filled.Home, "Inicio") }
                                )

                                NavigationBarItem(
                                    selected = navController.currentBackStackEntry?.destination?.route == "search",
                                    onClick = { navController.navigate("search") },
                                    label = { Text("Buscar") },
                                    icon = { Icon(Icons.Filled.Search, "Buscar") }
                                )

                                NavigationBarItem(
                                    selected = navController.currentBackStackEntry?.destination?.route == "profile",
                                    onClick = { navController.navigate("profile") },
                                    label = { Text("Perfil") },
                                    icon = { Icon(Icons.Filled.AccountCircle, "Perfil") }
                                )
                            }
                        }
                    }
                ) { innerPadding ->

                    NavHost(
                        navController = navController,
                        startDestination = if (isLoggedIn) "home" else "start",
                        modifier = Modifier.padding(innerPadding)
                    ) {

                        // Pantalla inicial (Start)
                        composable("start") {
                            StartScreen(navController)
                        }

                        // Login
                        composable("login") {
                            LoginScreen(navController) {
                                isLoggedIn = true
                            }
                        }

                        // Registro
                        composable("register") {
                            RegistrationScreen(navController)
                        }

                        // Home (lista de animes)
                        composable("home") {
                            AnimeListScreen(navController) {
                                isLoggedIn = false
                            }
                        }

                        // Buscador
                        composable("search") {
                            AnimeSearchScreen(navController) {
                                isLoggedIn = false
                            }
                        }

                        // Perfil
                        composable("profile") {
                            ProfileScreen(
                                navController = navController,
                                prefs = prefs,
                                onLogout = {
                                    prefs.logout()
                                    isLoggedIn = false
                                    navController.navigate("start") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Detalle anime
                        composable("detail/{animeId}") { backStackEntry ->
                            val animeId = backStackEntry.arguments?.getString("animeId")?.toIntOrNull()

                            animeId?.let {
                                AnimeDetailScreen(
                                    navController = navController,
                                    animeId = it,
                                    prefs = prefs,
                                    onLogout = {
                                        prefs.logout()
                                        isLoggedIn = false
                                        navController.navigate("start") {
                                            popUpTo("home") { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
