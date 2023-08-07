package com.tensorflow.android.repositories

import com.tensorflow.android.services.api.ApiConfig
import okhttp3.RequestBody

class AuthRepository {
    private val client = ApiConfig.getApiService()

    suspend fun login(email: RequestBody, password: RequestBody) = client.login(email, password)
}