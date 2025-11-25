package com.example.gamevault.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object LoggedUser {
    var email: String? = null
    var username: String? = null
    fun isLoggedIn() = email != null
    fun logout() {
        email = null
        username = null
    }
}

class NeonDBHelper(private val prefs: SharedPreferencesHelper) {
    suspend fun register(email: String, username: String, password: String): Boolean {
        return try {
            val response = RetrofitClient.api.register(User(email, username, password))
            if (response.isSuccessful) {
                // Guardamos localmente también
                prefs.saveCurrentUser(email, username)
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }
    suspend fun login(email: String, password: String): Boolean {
        return try {
            val response = RetrofitClient.api.login(User(email, "", password))
            if (response.isSuccessful) {
                val user = response.body()!!
                LoggedUser.email = user.email
                LoggedUser.username = user.username
                prefs.saveCurrentUser(user.email, user.username)
                true
            } else false
        } catch (e: Exception) { false }
    }

    fun logout() {
        LoggedUser.logout()
        prefs.clearCurrentUser()
    }

    fun getCurrentUser() = LoggedUser.email ?: prefs.getCurrentUser()
    fun getCurrentUsername() = LoggedUser.username ?: prefs.getCurrentUsername()

    // Comentarios online
    suspend fun saveComment(animeId: Int, title: String, content: String): Boolean {
        val email = getCurrentUser() ?: return false
        val username = getCurrentUsername() ?: email
        return try {
            val response = RetrofitClient.api.saveComment(
                Comment(
                    id = null,          // <- importante
                    animeId = animeId,
                    title = title,
                    content = content,
                    email = email,
                    username = username
                )
            )
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun loadUserComments(): List<Comment> {
        val email = getCurrentUser() ?: return emptyList()
        return try {
            val response = RetrofitClient.api.getUserComments(email)
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun loadCommentsByAnime(animeId: Int): List<Comment> {
        return try {
            val response = RetrofitClient.api.getComments(animeId)
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    // NeonDBHelper
    suspend fun deleteComment(commentId: Int): Boolean {
        return try {
            val response = RetrofitClient.api.deleteComment(commentId)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateComment(commentId: Int, title: String, content: String): Boolean {
        return try {
            val response = RetrofitClient.api.updateComment(commentId, CommentUpdate(title, content))
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
