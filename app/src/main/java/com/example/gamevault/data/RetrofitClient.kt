package com.example.gamevault.data

import retrofit2.http.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Response

data class User(val email: String, val username: String, val password: String? = null)
data class Comment(
    val id: Int?,        // <- el ID de la base de datos
    val animeId: Int,
    val title: String,
    val content: String,
    val email: String,
    val username: String
)

// DTO para actualizar comentario
data class CommentUpdate(val title: String, val content: String)
interface ApiService {
    @POST("register")
    suspend fun register(@Body user: User): Response<Void>

    @POST("login")
    suspend fun login(@Body user: User): Response<User>

    @POST("comments")
    suspend fun saveComment(@Body comment: Comment): Response<Void>

    @GET("comments/{animeId}")
    suspend fun getComments(@Path("animeId") animeId: Int): Response<List<Comment>>

    @GET("comments/user/{email}")
    suspend fun getUserComments(@Path("email") email: String): Response<List<Comment>>

    @DELETE("comments/{id}")
    suspend fun deleteComment(@Path("id") id: Int): Response<Void>

    @GET("users/{email}")
    suspend fun getUser(@Path("email") email: String): Response<User>

    @PUT("comments/{id}")
    suspend fun updateComment(@Path("id") id: Int, @Body comment: CommentUpdate): Response<Void>
}


object RetrofitClient {
    private const val BASE_URL = "https://gamevaultbackend.onrender.com/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
