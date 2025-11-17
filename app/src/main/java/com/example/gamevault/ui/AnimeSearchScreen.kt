package com.example.gamevault.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gamevault.data.SharedPreferencesHelper
import com.example.gamevault.network.Anime
import com.example.gamevault.network.RetrofitInstance
import com.example.gamevault.ui.components.TopBarSection
import kotlinx.coroutines.launch

@Composable
fun AnimeSearchScreen(
    navController: NavController,
    onLogout: () -> Unit
) {
    // Prefs para el logout
    val context = navController.context
    val prefs = remember { SharedPreferencesHelper(context) }

    var query by remember { mutableStateOf("") }
    var animeList by remember { mutableStateOf<List<Anime>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // 🔵 TOP BAR -----------------------------
        TopBarSection(
            navController = navController,
            title = "Buscar Anime",
            prefs = prefs,
            onLogout = onLogout
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 🔍 CAMPO DE BÚSQUEDA -------------------
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Buscar anime") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 🔍 BOTÓN DE BUSCAR ---------------------
        Button(
            onClick = {
                if (query.isNotBlank()) {
                    isLoading = true
                    errorMessage = ""
                    coroutineScope.launch {
                        try {
                            val response = RetrofitInstance.api.searchAnimes(query)
                            animeList = response.data
                        } catch (e: Exception) {
                            errorMessage = "Error al buscar animes"
                        } finally {
                            isLoading = false
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Buscar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔄 LOADING / ERROR / LISTA -------------
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            errorMessage.isNotEmpty() -> {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }

            else -> {
                LazyColumn {
                    items(animeList) { anime ->
                        AnimePostItem(anime = anime) {
                            navController.navigate("detail/${anime.mal_id}")
                        }
                    }
                }
            }
        }
    }
}
