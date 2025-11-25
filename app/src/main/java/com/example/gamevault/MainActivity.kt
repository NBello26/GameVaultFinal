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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gamevault.data.NeonDBHelper
import com.example.gamevault.data.SharedPreferencesHelper
import com.example.gamevault.data.LoggedUser
import com.example.gamevault.ui.*
import com.example.gamevault.ui.theme.GameVaultTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            GameVaultTheme {

                val navController = rememberNavController()
                val prefs = remember { SharedPreferencesHelper(this) } // helper local
                val neonHelper = remember { NeonDBHelper(prefs) }

                // Estado de sesión
                var isLoggedIn by remember { mutableStateOf(LoggedUser.isLoggedIn() || prefs.getCurrentUser() != null) }

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

                        // PANTALLAS SIN NAVBAR
                        composable("start") {
                            StartScreen(navController)
                        }

                        composable("login") {
                            LoginScreen(navController) {
                                isLoggedIn = true
                            }
                        }

                        composable("register") {
                            RegistrationScreen(navController, prefs)
                        }

                        // PANTALLAS CON NAVBAR
                        composable("home") {
                            AnimeListScreen(navController) {
                                neonHelper.logout()
                                isLoggedIn = false
                                navController.navigate("start") { popUpTo("home") { inclusive = true } }
                            }
                        }

                        composable("search") {
                            AnimeSearchScreen(navController) {
                                neonHelper.logout()
                                isLoggedIn = false
                                navController.navigate("start") { popUpTo("home") { inclusive = true } }
                            }
                        }

                        composable("profile") {
                            ProfileScreen(
                                navController = navController,
                                prefs = prefs,
                                onLogout = {
                                    neonHelper.logout()
                                    isLoggedIn = false
                                    navController.navigate("start") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("detail/{animeId}") { backStackEntry ->
                            val animeId = backStackEntry.arguments?.getString("animeId")?.toIntOrNull()
                            animeId?.let {
                                AnimeDetailScreen(
                                    navController = navController,
                                    animeId = it,
                                    prefs = prefs,
                                    onLogout = {
                                        neonHelper.logout()
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
