package com.tensorflow.android.repositories

import com.tensorflow.android.services.api.ApiConfig
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AuthRepository {
    private val client = ApiConfig.getApiService()

    suspend fun login(email: RequestBody, password: RequestBody) = client.login(email, password)
    suspend fun patientRegister(name: RequestBody,
                                address: RequestBody,
                                gender: RequestBody,
                                email: RequestBody,
                                password: RequestBody) = client.patientRegister(name, address, gender, email, password)
    suspend fun doctorRegister(name: RequestBody,
                               address: RequestBody,
                               gender: RequestBody,
                               email: RequestBody,
                               password: RequestBody,
                               ktp: MultipartBody.Part,
                               sip: MultipartBody.Part) = client.doctorRegister(name, address, gender, email, password, ktp, sip)
}