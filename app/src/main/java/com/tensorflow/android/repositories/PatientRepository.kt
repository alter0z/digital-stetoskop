package com.tensorflow.android.repositories

import com.tensorflow.android.services.api.ApiConfig

class PatientRepository {
    private val client = ApiConfig.getApiService()

    suspend fun getUserById(id: Int) = client.getUserById(id)
}