package com.example.gamevault.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gamevault.data.SharedPreferencesHelper
import com.example.gamevault.network.RetrofitInstance
import com.example.gamevault.ui.components.TopBarSection
import java.io.ByteArrayOutputStream

@Composable
fun ProfileScreen(
    navController: NavController,
    prefs: SharedPreferencesHelper,
    onLogout: () -> Unit
) {
    val context = navController.context
    var profileBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // ------------------------------
    // Cargar foto de perfil
    // ------------------------------
    LaunchedEffect(Unit) {
        prefs.loadProfileImage()?.let { base64 ->
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            profileBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val stream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(stream)
        profileBitmap = bitmap

        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        prefs.saveProfileImage(Base64.encodeToString(output.toByteArray(), Base64.DEFAULT))
    }

    // ------------------------------
    // Comentarios del usuario
    // ------------------------------
    val currentUser = prefs.getCurrentUser() ?: ""
    var userComments by remember { mutableStateOf(prefs.loadAllUserComments(currentUser)) }

    val api = RetrofitInstance.api
    var animeTitles by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }

    // Cargar nombres desde API
    LaunchedEffect(userComments) {
        val ids = userComments.map { it.first }.distinct()
        val updated = animeTitles.toMutableMap()

        ids.forEach { id ->
            try {
                updated[id] = api.getAnimeById(id).data.title
            } catch (e: Exception) {
                updated[id] = "Anime $id"
            }
        }
        animeTitles = updated
    }

    // ------------------------------
    // Estado para editar comentario
    // ------------------------------
    var editDialogVisible by remember { mutableStateOf(false) }
    var commentToEdit by remember { mutableStateOf<Triple<Int, String, String>?>(null) }
    var editTitle by remember { mutableStateOf("") }
    var editContent by remember { mutableStateOf("") }

    // ------------------------------
    // Estado para confirmar eliminación
    // ------------------------------
    var deleteDialogVisible by remember { mutableStateOf(false) }
    var commentToDelete by remember { mutableStateOf<Triple<Int, String, String>?>(null) }

    // ------------------------------
    // UI
    // ------------------------------
    Column(modifier = Modifier.fillMaxSize()) {

        TopBarSection(
            navController = navController,
            title = "Perfil",
            prefs = prefs,
            onLogout = onLogout
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // FOTO DE PERFIL
            item {
                Card(
                    modifier = Modifier.size(130.dp),
                    shape = CircleShape
                ) {
                    Image(
                        bitmap = (profileBitmap
                            ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
                            .asImageBitmap(),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(Modifier.height(12.dp))

                Button(onClick = { pickImageLauncher.launch("image/*") }) {
                    Text("Cambiar foto de perfil")
                }

                Spacer(Modifier.height(20.dp))

                Text("Usuario: ${prefs.getUsername(currentUser) ?: currentUser}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text("Correo: $currentUser", style = MaterialTheme.typography.titleMedium)

                Spacer(Modifier.height(30.dp))

                Text("Mis Comentarios", style = MaterialTheme.typography.titleLarge)

                Spacer(Modifier.height(10.dp))
            }

            // SI NO HAY COMENTARIOS
            if (userComments.isEmpty()) {
                item {
                    Text(
                        "No has hecho ningún comentario.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 20.dp)
                    )
                }
            }

            // LISTA DE COMENTARIOS
            items(userComments) { (animeId, title, content) ->

                val animeTitle = animeTitles[animeId] ?: "Cargando..."

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {

                        Text(animeTitle, style = MaterialTheme.typography.titleMedium)

                        Text("Título: $title", style = MaterialTheme.typography.labelSmall)

                        Spacer(Modifier.height(6.dp))

                        Text(content)

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Ir al anime
                            OutlinedButton(onClick = {
                                navController.navigate("detail/$animeId")
                            }) {
                                Text("Ver anime")
                            }

                            // Editar
                            OutlinedButton(onClick = {
                                commentToEdit = Triple(animeId, title, content)
                                editTitle = title
                                editContent = content
                                editDialogVisible = true
                            }) {
                                Text("Editar")
                            }

                            // Eliminar
                            Button(
                                onClick = {
                                    commentToDelete = Triple(animeId, title, content)
                                    deleteDialogVisible = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Eliminar")
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    // -----------------------------------
    // DIALOGO PARA EDITAR COMENTARIO
    // -----------------------------------
    if (editDialogVisible) {
        AlertDialog(
            onDismissRequest = { editDialogVisible = false },
            title = { Text("Editar comentario") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Título") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editContent,
                        onValueChange = { editContent = it },
                        label = { Text("Comentario") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    commentToEdit?.let { c ->
                        prefs.updateUserComment(
                            currentUser,
                            animeId = c.first,
                            oldTitle = c.second,
                            oldContent = c.third,
                            newTitle = editTitle,
                            newContent = editContent
                        )
                        userComments = prefs.loadAllUserComments(currentUser)
                    }
                    editDialogVisible = false
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { editDialogVisible = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // -----------------------------------
    // CONFIRMAR BORRADO
    // -----------------------------------
    if (deleteDialogVisible) {
        AlertDialog(
            onDismissRequest = { deleteDialogVisible = false },
            title = { Text("Eliminar comentario") },
            text = { Text("¿Seguro que quieres eliminar este comentario?") },
            confirmButton = {
                TextButton(onClick = {
                    commentToDelete?.let { c ->
                        prefs.deleteUserComment(
                            currentUser,
                            c.first,
                            c.second,
                            c.third
                        )
                        userComments = prefs.loadAllUserComments(currentUser)
                    }
                    deleteDialogVisible = false
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialogVisible = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
