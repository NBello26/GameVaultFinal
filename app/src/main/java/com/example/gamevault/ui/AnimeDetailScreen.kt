package com.example.gamevault.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import androidx.navigation.NavController
import com.example.gamevault.data.Comment
import com.example.gamevault.data.NeonDBHelper
import com.example.gamevault.data.SharedPreferencesHelper
import com.example.gamevault.network.Anime
import com.example.gamevault.network.RetrofitInstance
import com.example.gamevault.ui.components.TopBarSection
import kotlinx.coroutines.launch

@Composable
fun AnimeDetailScreen(
    navController: NavController,
    animeId: Int,
    prefs: SharedPreferencesHelper,
    onLogout: () -> Unit
) {
    var anime by remember { mutableStateOf<Anime?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    var showFullSynopsis by remember { mutableStateOf(false) }
    var liked by remember { mutableStateOf<Boolean?>(null) }
    var showCommentDialog by remember { mutableStateOf(false) }

    var comments by remember { mutableStateOf(listOf<Comment>()) }

    val coroutineScope = rememberCoroutineScope()
    val dbHelper = remember { NeonDBHelper(prefs) }

    // Cargar comentarios online
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            comments = dbHelper.loadCommentsByAnime(animeId)
        }
    }

    // Llamada API para los detalles del anime
    LaunchedEffect(animeId) {
        coroutineScope.launch {
            try {
                val response = RetrofitInstance.api.getAnimeById(animeId)
                anime = response.data
            } catch (e: Exception) {
                errorMessage = "Error al cargar detalles"
            } finally {
                isLoading = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        TopBarSection(
            navController = navController,
            title = "Detalles",
            prefs = prefs,
            onLogout = onLogout
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                errorMessage.isNotEmpty() -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { Text(errorMessage, color = MaterialTheme.colorScheme.error) }

                anime != null -> {
                    val currentAnime = anime!!

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF8F9FA))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            // Detalles del anime
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Image(
                                        painter = rememberAsyncImagePainter(currentAnime.image_url),
                                        contentDescription = currentAnime.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(250.dp)
                                    )

                                    Column(Modifier.padding(16.dp)) {
                                        Text(
                                            currentAnime.title,
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E88E5)
                                        )

                                        Spacer(Modifier.height(8.dp))

                                        val synopsis = currentAnime.synopsis ?: "Sin descripción"
                                        val shortText = if (synopsis.length > 200)
                                            synopsis.take(200) + "..."
                                        else synopsis

                                        Text(
                                            text = if (showFullSynopsis) synopsis else shortText,
                                            textAlign = TextAlign.Justify
                                        )

                                        if (synopsis.length > 200) {
                                            TextButton(onClick = { showFullSynopsis = !showFullSynopsis }) {
                                                Text(if (showFullSynopsis) "Ver menos" else "Ver más")
                                            }
                                        }

                                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                                        Text("📺 Tipo: ${currentAnime.type ?: "Desconocido"}")
                                        Text("📊 Estado: ${currentAnime.status}")
                                        Text("🎞 Episodios: ${currentAnime.episodes ?: "N/A"}")
                                        Text("🏷 Géneros: ${currentAnime.genres.joinToString { it.name }}")

                                        Spacer(Modifier.height(16.dp))

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        ) {
                                            IconButton(onClick = { liked = if (liked == true) null else true }) {
                                                Icon(
                                                    Icons.Outlined.ThumbUp,
                                                    contentDescription = "Like",
                                                    tint = if (liked == true) Color(0xFF4CAF50) else Color.Gray
                                                )
                                            }

                                            IconButton(onClick = { liked = if (liked == false) null else false }) {
                                                Icon(
                                                    Icons.Filled.Close,
                                                    contentDescription = "Dislike",
                                                    tint = if (liked == false) Color(0xFFF44336) else Color.Gray
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            Text(
                                "💬 Comentarios",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(comments) { comment ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text("${comment.title} (by ${comment.username})", fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(4.dp))
                                    Text(comment.content, color = Color.DarkGray)
                                }
                            }
                        }
                    }

                    // Botón para agregar comentario
                    FloatingActionButton(
                        onClick = { showCommentDialog = true },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(24.dp),
                        containerColor = Color(0xFF1E88E5)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar", tint = Color.White)
                    }

                    // Diálogo para agregar comentario
                    if (showCommentDialog) {
                        var commentTitle by remember { mutableStateOf("") }
                        var commentContent by remember { mutableStateOf("") }

                        AlertDialog(
                            onDismissRequest = { showCommentDialog = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    if (commentTitle.isNotBlank() && commentContent.isNotBlank()) {
                                        coroutineScope.launch {
                                            val success = dbHelper.saveComment(animeId, commentTitle, commentContent)
                                            if (success) {
                                                comments = dbHelper.loadCommentsByAnime(animeId)
                                                showCommentDialog = false
                                            }
                                        }
                                    }
                                }) { Text("Agregar") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showCommentDialog = false }) { Text("Cancelar") }
                            },
                            title = { Text("Nuevo comentario") },
                            text = {
                                Column {
                                    OutlinedTextField(
                                        value = commentTitle,
                                        onValueChange = { commentTitle = it },
                                        label = { Text("Título") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = commentContent,
                                        onValueChange = { commentContent = it },
                                        label = { Text("Contenido") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
