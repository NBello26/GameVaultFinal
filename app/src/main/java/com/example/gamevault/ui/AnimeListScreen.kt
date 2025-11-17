package com.example.gamevault.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.gamevault.R
import com.example.gamevault.data.SharedPreferencesHelper
import com.example.gamevault.network.Anime
import com.example.gamevault.network.RetrofitInstance
import kotlinx.coroutines.launch
import com.example.gamevault.ui.components.TopBarSection

@Composable
fun AnimeListScreen(
    navController: NavController,
    onLogout: () -> Unit
) {
    val context = navController.context
    val prefs = remember { SharedPreferencesHelper(context) }

    var animeList by remember { mutableStateOf<List<Anime>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(true) {
        try {
            val response = RetrofitInstance.api.getTopAnimes()
            animeList = response.data.take(5)
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
            errorMessage = "Error al cargar los datos"
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF3F4F6)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // 🔵 NUEVO: TopBarSection reutilizable
            TopBarSection(
                navController = navController,
                title = "Animes Populares",
                prefs = prefs,
                onLogout = onLogout
            )

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
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




@Composable
fun AnimePostItem(
    anime: Anime,
    onViewClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            AsyncImage(
                model = anime.image_url,
                contentDescription = anime.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = anime.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E88E5)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val genreText = if (anime.genres.isNotEmpty()) {
                anime.genres.joinToString(", ") { it.name }
            } else {
                "Sin géneros disponibles"
            }

            Text("Géneros: $genreText", fontSize = 14.sp, color = Color.Gray)
            Text("Estado: ${anime.status}", fontSize = 14.sp, color = Color.Gray)

            val episodesText = anime.episodes?.toString() ?: "Desconocido"
            Text("Episodios: $episodesText", fontSize = 14.sp, color = Color.Gray)

            Text("Tipo: ${anime.type ?: "Tipo desconocido"}", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onViewClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFC107),
                    contentColor = Color.Black
                )
            ) {
                Text("Ver publicación")
            }
        }
    }
}
