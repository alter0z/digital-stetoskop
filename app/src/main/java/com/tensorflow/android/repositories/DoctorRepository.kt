package com.tensorflow.android.repositories

import com.tensorflow.android.services.api.ApiConfig
import okhttp3.MultipartBody
import okhttp3.RequestBody

class DoctorRepository {
    private val client = ApiConfig.getApiService()

    suspend fun getUserById(id: Int) = client.getUserById(id)
    suspend fun sendFile(id: RequestBody, file: MultipartBody.Part) = client.sendSignalFile(id, file)
    suspend fun getAllPatient() = client.getAllPatient()
    suspend fun getUserPredict(id: Int) = client.getUserPredict(id)
    suspend fun getPredict(id: Int) = client.getPredict(id)
}