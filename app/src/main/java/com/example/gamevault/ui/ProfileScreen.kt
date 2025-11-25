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
import com.example.gamevault.data.Comment
import com.example.gamevault.data.NeonDBHelper
import com.example.gamevault.data.SharedPreferencesHelper
import com.example.gamevault.ui.components.TopBarSection
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@Composable
fun ProfileScreen(
    navController: NavController,
    prefs: SharedPreferencesHelper,
    onLogout: () -> Unit
) {
    val neonHelper = remember { NeonDBHelper(prefs) }
    val currentUserEmail = neonHelper.getCurrentUser() ?: ""
    val currentUsername = neonHelper.getCurrentUsername() ?: currentUserEmail
    val coroutineScope = rememberCoroutineScope()
    val context = navController.context

    var profileBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var userComments by remember { mutableStateOf<List<Comment>>(emptyList()) }

    // ------------------------------
    // Cargar foto de perfil local
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
    // Función para cargar comentarios del usuario
    // ------------------------------
    fun loadUserComments() {
        coroutineScope.launch {
            userComments = neonHelper.loadUserComments()
        }
    }

    LaunchedEffect(currentUserEmail) { loadUserComments() }

    // ------------------------------
    // Estados para edición y eliminación
    // ------------------------------
    var editDialogVisible by remember { mutableStateOf(false) }
    var commentToEdit by remember { mutableStateOf<Comment?>(null) }
    var editTitle by remember { mutableStateOf("") }
    var editContent by remember { mutableStateOf("") }

    var deleteDialogVisible by remember { mutableStateOf(false) }
    var commentToDelete by remember { mutableStateOf<Comment?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {

        // ------------------------------
        // TopBar con Logout
        // ------------------------------
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

            // FOTO Y DATOS DEL USUARIO
            item {
                Card(modifier = Modifier.size(130.dp), shape = CircleShape) {
                    Image(
                        bitmap = (profileBitmap ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
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
                Text("Usuario: $currentUsername", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text("Correo: $currentUserEmail", style = MaterialTheme.typography.titleMedium)

                Spacer(Modifier.height(30.dp))
                Text("Mis Comentarios", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(10.dp))
            }

            // MENSAJE SI NO HAY COMENTARIOS
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
            items(userComments, key = { it.id ?: 0 }) { comment ->
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
                        Text("Título: ${comment.title}", style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.height(6.dp))
                        Text(comment.content)
                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(onClick = { /* Navegar a anime si quieres */ }) {
                                Text("Ver anime")
                            }

                            OutlinedButton(onClick = {
                                commentToEdit = comment
                                editTitle = comment.title
                                editContent = comment.content
                                editDialogVisible = true
                            }) {
                                Text("Editar")
                            }

                            Button(
                                onClick = {
                                    commentToDelete = comment
                                    deleteDialogVisible = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
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

    // ------------------------------
    // Dialogo Editar Comentario
    // ------------------------------
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
                    val commentId = commentToEdit?.id ?: return@TextButton
                    coroutineScope.launch {
                        val success = neonHelper.updateComment(commentId, editTitle, editContent)
                        if (success) {
                            // Actualiza la lista localmente sin recargar
                            userComments = userComments.map {
                                if (it.id == commentId) it.copy(title = editTitle, content = editContent) else it
                            }
                        }
                    }
                    editDialogVisible = false
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { editDialogVisible = false }) { Text("Cancelar") }
            }
        )
    }

    // ------------------------------
    // Dialogo Eliminar Comentario
    // ------------------------------
    if (deleteDialogVisible) {
        AlertDialog(
            onDismissRequest = { deleteDialogVisible = false },
            title = { Text("Eliminar comentario") },
            text = { Text("¿Seguro que quieres eliminar este comentario?") },
            confirmButton = {
                TextButton(onClick = {
                    val commentId = commentToDelete?.id ?: return@TextButton
                    coroutineScope.launch {
                        val success = neonHelper.deleteComment(commentId)
                        if (success) {
                            userComments = userComments.filter { it.id != commentId }
                        }
                    }
                    deleteDialogVisible = false
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { deleteDialogVisible = false }) { Text("Cancelar") } }
        )
    }
}
