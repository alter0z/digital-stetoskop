package com.tensorflow.android.repositories

import com.tensorflow.android.services.api.ApiConfig
import okhttp3.MultipartBody
import okhttp3.RequestBody

class DoctorRepository {
    private val client = ApiConfig.getApiService()

    suspend fun getUserById(id: Int) = client.getUserById(id)
    suspend fun sendFile(name: RequestBody, file: MultipartBody.Part) = client.sendSignalFile(name, file)
    suspend fun getAllPatient() = client.getAllPatient()
    suspend fun getUserPredict(id: Int) = client.getPredict(id)
}