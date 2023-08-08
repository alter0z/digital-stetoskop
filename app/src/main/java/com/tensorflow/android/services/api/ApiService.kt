package com.tensorflow.android.services.api

import com.tensorflow.android.models.response.auth.LoginResponse
import com.tensorflow.android.models.response.auth.RegisterResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("login")
    suspend fun login(@Part("email") email: RequestBody, @Part("password") password: RequestBody): LoginResponse
    @Multipart
    @POST("register_pasien")
    suspend fun patientRegister(@Part("nama_lengklap") name: RequestBody,
                                @Part("alamat") address: RequestBody,
                                @Part("gender") gender: RequestBody,
                                @Part("email") email: RequestBody,
                                @Part("password") password: RequestBody): RegisterResponse
    @Multipart
    @POST("register_pasien")
    suspend fun doctorRegister(@Part("nama_lengklap") name: RequestBody,
                               @Part("alamat") address: RequestBody,
                               @Part("gender") gender: RequestBody,
                               @Part("email") email: RequestBody,
                               @Part("password") password: RequestBody,
                               @Part ktp: MultipartBody.Part,
                               @Part sip: MultipartBody.Part): RegisterResponse
}