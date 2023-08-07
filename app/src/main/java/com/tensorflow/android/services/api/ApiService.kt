package com.tensorflow.android.services.api

import com.tensorflow.android.services.response.auth.LoginResponse
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("login")
    suspend fun login(@Part("email") email: RequestBody, @Part("password") password: RequestBody): LoginResponse
}