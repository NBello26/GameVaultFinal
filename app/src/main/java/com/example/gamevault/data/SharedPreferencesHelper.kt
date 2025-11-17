package com.example.gamevault.data

import android.content.Context

class SharedPreferencesHelper(context: Context) {

    private val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    /** Registro de usuario con email, contraseña y nombre de usuario */
    fun registerUser(email: String, password: String, username: String): Boolean {
        if (prefs.contains("user_$email")) return false
        prefs.edit()
            .putString("user_$email", password)
            .putString("username_$email", username)
            .apply()
        return true
    }

    /** Login con email y contraseña */
    fun login(email: String, password: String): Boolean {
        val storedPassword = prefs.getString("user_$email", null)
        if (storedPassword == password) {
            prefs.edit()
                .putString("logged_user", email)
                .putString("current_username", getUsername(email)) // Guardar username del usuario logueado
                .apply()
            return true
        }
        return false
    }

    /** Logout */
    fun logout() {
        prefs.edit().remove("logged_user").remove("current_username").apply()
    }

    /** Verificar si hay usuario logueado */
    fun isLoggedIn(): Boolean = prefs.getString("logged_user", null) != null

    /** Obtener email del usuario logueado */
    fun getCurrentUser(): String? = prefs.getString("logged_user", null)

    /** Obtener username del usuario logueado */
    fun getCurrentUsername(): String? = prefs.getString("current_username", null)

    /** Obtener username por email */
    fun getUsername(email: String): String? = prefs.getString("username_$email", null)

    /** Guardar comentario (global y por usuario) */
    fun saveComment(animeId: Int, title: String, content: String) {
        val email = getCurrentUser() ?: return
        val username = getCurrentUsername() ?: email

        val userKey = "comments_${email}_$animeId"
        val globalKey = "comments_global_$animeId"

        val newComment = "$title%%$content%%$username;;"

        // Guardar comentario GLOBAL
        val oldGlobal = prefs.getString(globalKey, "") ?: ""
        prefs.edit().putString(globalKey, oldGlobal + newComment).apply()

        // Guardar comentario del usuario
        val oldUser = prefs.getString(userKey, "") ?: ""
        prefs.edit().putString(userKey, oldUser + newComment).apply()
    }

    /** Cargar comentarios globales de un anime */
    fun loadGlobalComments(animeId: Int): List<Triple<String, String, String>> {
        val key = "comments_global_$animeId"
        val raw = prefs.getString(key, "") ?: ""
        return raw.split(";;")
            .filter { it.contains("%%") }
            .map {
                val parts = it.split("%%")
                Triple(parts[0], parts[1], parts[2]) // title, content, username
            }
    }

    /** Cargar comentarios SOLO del usuario logueado */
    fun loadUserComments(animeId: Int): List<Pair<String, String>> {
        val email = getCurrentUser() ?: return emptyList()
        val key = "comments_${email}_$animeId"

        val raw = prefs.getString(key, "") ?: ""
        return raw.split(";;")
            .filter { it.contains("%%") }
            .map {
                val parts = it.split("%%")
                parts[0] to parts[1] // title - content
            }
    }

    /** Cargar todos los comentarios de un usuario (para perfil) */
    fun loadAllUserComments(email: String): List<Triple<Int, String, String>> {
        val all = prefs.all
        return all.mapNotNull { (key, value) ->
            if (key.startsWith("comments_${email}_")) {
                val animeId = key.removePrefix("comments_${email}_").toIntOrNull() ?: return@mapNotNull null
                val raw = value as? String ?: return@mapNotNull null

                raw.split(";;")
                    .filter { it.contains("%%") }
                    .map {
                        val parts = it.split("%%")
                        Triple(animeId, parts[0], parts[1]) // animeId, title, content
                    }
            } else null
        }.flatten()
    }

    /** Guardar imagen de perfil */
    fun saveProfileImage(base64: String) {
        val email = getCurrentUser() ?: return
        prefs.edit().putString("profile_img_$email", base64).apply()
    }

    /** Cargar imagen de perfil */
    fun loadProfileImage(): String? {
        val email = getCurrentUser() ?: return null
        return prefs.getString("profile_img_$email", null)
    }

    /** Eliminar comentario (global y usuario) */
    fun deleteCommentForAll(email: String, animeId: Int, title: String, content: String) {
        val username = getUsername(email) ?: email
        val target = "$title%%$content%%$username;;"

        val globalKey = "comments_global_$animeId"
        val userKey = "comments_${email}_$animeId"

        val rawGlobal = prefs.getString(globalKey, "") ?: ""
        prefs.edit().putString(globalKey, rawGlobal.replace(target, "")).apply()

        val rawUser = prefs.getString(userKey, "") ?: ""
        prefs.edit().putString(userKey, rawUser.replace(target, "")).apply()
    }

    /** Eliminar comentario del usuario */
    fun deleteUserComment(email: String, animeId: Int, title: String, content: String) {
        deleteCommentForAll(email, animeId, title, content)
    }

    /** Actualizar comentario de usuario */
    fun updateUserComment(
        email: String,
        animeId: Int,
        oldTitle: String,
        oldContent: String,
        newTitle: String,
        newContent: String
    ) {
        val username = getUsername(email) ?: email
        val old = "$oldTitle%%$oldContent%%$username;;"
        val new = "$newTitle%%$newContent%%$username;;"

        val globalKey = "comments_global_$animeId"
        val userKey = "comments_${email}_$animeId"

        val rawGlobal = prefs.getString(globalKey, "") ?: ""
        prefs.edit().putString(globalKey, rawGlobal.replace(old, new)).apply()

        val rawUser = prefs.getString(userKey, "") ?: ""
        prefs.edit().putString(userKey, rawUser.replace(old, new)).apply()
    }
}
